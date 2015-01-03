/**
 * Created by doki on 01.07.14.
 */

var profilersControllers = angular.module('profilerControllers', ['dataview', 'ngResource']);

profilersControllers.factory("methods", function($resource) {
	return $resource("/profiler/:appId/:sessionId/methods");
});

profilersControllers.factory("analyze", function($resource) {
	return $resource("/profiler/:appId/:sessionId/analyze");
});

profilersControllers.controller('profilerController', function($scope, dataview, methods, analyze){
	$scope.title = 'Profiling information'

	$scope.appId = dataview.appId;
	$scope.sessionId = dataview.sessionId;

	$scope.methods = [];
	$scope.currentMethod = null;

	function processSessionData(){
		analyze.get({appId: $scope.appId, sessionId: $scope.sessionId}, function(data){
		});
	}

	function loadMethods(){
		methods.query({appId: $scope.appId, sessionId: $scope.sessionId}, function(data){
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
});
