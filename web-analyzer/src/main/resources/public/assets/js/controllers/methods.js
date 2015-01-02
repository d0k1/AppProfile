/**
 * Created by doki on 01.07.14.
 */

var methodsControllers = angular.module('methodsControllers', ['dataview', 'ngResource']);

methodsControllers.factory("methods", function($resource) {
	return $resource("/profiler/:appId/:sessionId/methods");
});

methodsControllers.controller('methodsController', function($scope, dataview, methods){
	$scope.title = 'Methods information'

	$scope.appId = dataview.appId;
	$scope.sessionId = dataview.sessionId;

	$scope.methods = [];
	$scope.currentMethod = null;

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

	$scope.selectMethod = function(method){
		$scope.currentMethod = method;
	}
});
