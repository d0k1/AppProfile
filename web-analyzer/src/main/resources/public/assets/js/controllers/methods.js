/**
 * Created by doki on 17.01.15.
 */

var methodsControllers = angular.module('methodsControllers', ['appsessionrec', 'dataview', 'ngResource']);

methodsControllers.factory("report", function($resource) {
	return $resource("/methods/:appId/:sessionId/:recId/report");
});
methodsControllers.factory("analyze", function($resource) {
	return $resource("/methods/:appId/:sessionId/:recId/analyze");
});

methodsControllers.controller('methodsController', ['$scope', 'dataview', 'report', 'analyze', function($scope, dataview, report, analyze) {

	$scope.title = "Methods"
	$scope.totalCount = 0;
	$scope.totalTime = 0;

	function processSessionData(){
		analyze.get({appId: dataview.appId, sessionId: dataview.sessionId, recId: dataview.recId}, function(data){
			loadMethods();
		});
	}

	function loadMethods() {
		$scope.recId = dataview.recId;
		report.query({appId:dataview.appId, sessionId:dataview.sessionId, recId: dataview.recId}, function(data){
			$scope.methods = [];
			$scope.totalCount = 0;
			$scope.totalTime = 0;
			for (var i = 0; i < data.length; i++) {
				$scope.totalCount = $scope.totalCount+1;
				$scope.totalTime = $scope.totalTime + data[i].totalTime;
				$scope.methods.push(data[i]);
			}
		});
	}

	$scope.onDataViewUpdated = function(){
		loadMethods();
	}

	$scope.analyze = function(){
		processSessionData();
	}

}]);
