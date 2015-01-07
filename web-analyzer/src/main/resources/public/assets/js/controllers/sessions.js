/**
 * Created by doki on 01.07.14.
 */

var sessionsControllers = angular.module('sessionsControllers', ['appsessionrec', 'dataview', 'ngResource']);

sessionsControllers.factory("recordControl", function($resource) {
	return $resource("/sessions/:appId/:sessionId/:action");
});

sessionsControllers.factory("automonitoring", function($resource) {
	return $resource("/sessions/automonitoring/:action");
});

sessionsControllers.factory("autoprofiling", function($resource) {
	return $resource("/sessions/autoprofiling/:action");
});

sessionsControllers.controller('sessionsController', function($scope, recordControl, automonitoring, autoprofiling){
	$scope.title = 'Measurement sessions'

	$scope.session = null;
	$scope.appId = -1;
	$scope.sessionId = -1;

	$scope.onDataViewUpdated = function(view, appIds, sessionIds, recIds){
		$scope.session = sessionIds[view.sessionId-1];
		$scope.appId = view.appId;
		$scope.sessionId = view.sessionId;
		$scope.recId = view.recId;
	}

	$scope.startAutoProfiling = function(){
		autoprofiling.get({action: "enable"}, function(data){
		});
	}

	$scope.stopAutoProfiling = function(){
		autoprofiling.get({action: "disable"}, function(data){
		});
	}

	$scope.startAutoMonitoring = function(){
		automonitoring.get({action: "enable"}, function(data){
		});
	}

	$scope.stopAutoMonitoring = function(){
		automonitoring.get({action: "disable"}, function(data){
		});
	}


	$scope.startProfiling = function(){
		recordControl.get({appId: $scope.appId, action:"startprofiling"}, function(data){
		});
	}

	$scope.stopProfiling = function(){
		recordControl.get({appId: $scope.appId, action:"startmonitoring"}, function(data){
		});
	}

	$scope.startMonitoring = function(){
		recordControl.get({appId: $scope.appId, action:"startprofiling"}, function(data){
		});
	}

	$scope.stopMonitoring = function(){
		recordControl.get({appId: $scope.appId, action:"stopmonitoring"}, function(data){
		});
	}

	$scope.stopRecord = function(){
		recordControl.get({appId: $scope.appId, action:"stoprecord"}, function(data){
		});
	}

	$scope.newRecord = function(){
		recordControl.get({appId: $scope.appId, sessionId:$scope.sessionId, action:"newrecord"}, function(data){
		});
	}

});
