/**
 * Created by doki on 01.07.14.
 */

var profilersControllers = angular.module('profilerControllers', ['appsessionrec','dataview', 'ngResource']);

profilersControllers.factory("methods", function($resource) {
	return $resource("/profiler/:appId/:sessionId/:recId/methods");
});

profilersControllers.factory("analyze", function($resource) {
	return $resource("/profiler/:appId/:sessionId/:recId/analyze");
});

profilersControllers.controller('profilerController', function($scope, dataview, methods, analyze){
	$scope.title = 'Profiling information'

	$scope.methods = [];
	$scope.currentMethod = null;

	function processSessionData(){
		analyze.get({appId: dataview.appId, sessionId: dataview.sessionId, recId: dataview.recId}, function(data){
		});
	}

	function loadMethods(){
		$scope.recId = dataview.recId;

		methods.query({appId: dataview.appId, sessionId: dataview.sessionId, recId: dataview.recId}, function(data){
			$scope.methods = []
			for(var i=0;i<data.length;i++){
				$scope.methods.push(data[i]);
			}
		});
	}

	if($scope.appId>0 && $scope.sessionId>0){
		loadMethods();
	}

	$scope.analyze = function(){
		processSessionData();
	}

	$scope.selectMethod = function(method){
		$scope.currentMethod = method;
	}

	$scope.onDataViewUpdated = function(view, appIds, sessionIds, recIds){
		loadMethods();
	}
});
