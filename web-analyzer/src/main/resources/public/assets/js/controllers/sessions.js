/**
 * Created by doki on 01.07.14.
 */

var sessionsControllers = angular.module('sessionsControllers', ['dataview', 'ngResource']);

sessionsControllers.factory("appIds", function($resource) {
	return $resource("/sessions/apps");
});

sessionsControllers.factory("sessionIds", function($resource) {
	return $resource("/sessions/:appId");
});

sessionsControllers.controller('sessionsController', function($scope, dataview, appIds, sessionIds){
	$scope.title = 'Measurement sessions'

	$scope.apps = [];
	$scope.sessions = [];

	$scope.appId = dataview.appId;
	$scope.sessionId = dataview.sessionId;

	function loadApps(){
		appIds.query(function(data){
			for(var i=0;i<data.length;i++){
				$scope.apps.push(data[i]);
			}
		});
	}

	function loadSessions(){
		sessionIds.query({appId: $scope.appId}, function(data){
			for(var i=0;i<data.length;i++){
				$scope.sessions.push(data[i]);
			}
		});
	}

	loadApps();
	if($scope.appId>0){
		loadSessions();
	}

	$scope.loadSessions = function(){
		dataview.appId = $scope.appId;
		loadSessions();
	}

	$scope.updateGlobals = function(){
		dataview.sessionId = $scope.sessionId;
	}
});
