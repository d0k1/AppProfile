var appSessionRecModule = angular.module('appsessionrec', ['ngResource', 'dataview'])

appSessionRecModule.factory("appIds", function($resource) {
	return $resource("/sessions/apps");
});

appSessionRecModule.factory("sessionIds", function($resource) {
	return $resource("/sessions/:appId");
});

appSessionRecModule.factory("recIds", function($resource) {
	return $resource("/sessions/:appId/:sessionId");
});

appSessionRecModule.directive('appsessionrec', function(dataview){

	return {
		restrict: 'E',
		templateUrl: '/assets/templates/directives/appsessionrec.html',
		replace: true,
		scope: { onDataViewUpdated : "="},
		controller: ['$scope', 'appIds', 'sessionIds', 'recIds', function($scope, appIds, sessionIds, recIds){
			$scope.apps = [];
			$scope.sessions = [];
			$scope.recs = [];

			$scope.appId = dataview.appId;
			$scope.sessionId = dataview.sessionId;
			$scope.recId = dataview.recId;

			$scope.loadApps = function(){
				appIds.query(function(data){
					for(var i=0;i<data.length;i++){
						$scope.apps.push(data[i]);
					}
				});
			}

			$scope.loadSessions = function(){
				dataview.appId = $scope.appId;
				sessionIds.query({appId: $scope.appId}, function(data){
					for(var i=0;i<data.length;i++){
						$scope.sessions.push(data[i]);
					}
					$scope.updateGlobals();
				});
			}

			$scope.loadRecords=function(){
				dataview.sessionId = $scope.sessionId;
				recIds.query({appId: $scope.appId, sessionId:$scope.sessionId}, function(data){
					for(var i=0;i<data.length;i++){
						$scope.recs.push(data[i]);
					}
					$scope.updateGlobals();
				});
			}

			$scope.updateGlobals = function(){
				dataview.recId = $scope.recId;
				if($scope.onDataViewUpdated!=null) {
					$scope.onDataViewUpdated(dataview, $scope.apps, $scope.sessions, $scope.recs);
				}
			}

		}],
		link: function($scope, iElement, iAttrs, ctrl){
			$scope.loadApps();

			if($scope.appId>0){
				$scope.loadSessions();
			}
			if($scope.sessionId>0){
				$scope.loadRecords();
			}
		}
	}
});
