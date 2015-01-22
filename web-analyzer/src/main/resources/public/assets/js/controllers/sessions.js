/**
 * Created by doki on 01.07.14.
 */

var sessionsControllers = angular.module('sessionsControllers', ['appsessionrec', 'dataview', 'ngResource']);

sessionsControllers.factory("recordControl", function($resource) {
	return $resource("/sessions/:appId/:sessionId/:action");
});

sessionsControllers.factory("settings", function($resource) {
	return $resource("/sessions/settings/:appId");
});

sessionsControllers.factory("automonitoring", function($resource) {
	return $resource("/sessions/automonitoring/:action");
});

sessionsControllers.factory("autoprofiling", function($resource) {
	return $resource("/sessions/autoprofiling/:action");
});

sessionsControllers.factory("flush", function($resource) {
	return $resource("/sessions/flush");
});

sessionsControllers.controller('sessionsController', function($scope, recordControl, automonitoring, autoprofiling, flush, settings){
	$scope.title = 'Measurement sessions'

	$scope.session = null;
	$scope.appId = -1;
	$scope.sessionId = -1;

	$scope.automonitoring = null;
	$scope.autoprofiling = null;
	$scope.monitoring = null;
	$scope.profiling = null;
	$scope.online = false;

	$scope.onDataViewUpdated = function(view, appIds, sessionIds, recIds){
		$scope.session = sessionIds[view.sessionId-1];
		$scope.appId = view.appId;
		$scope.sessionId = view.sessionId;
		$scope.recId = view.recId;
		$scope.online = view.online;
		loadSessionData();
	}

	function loadAutdata(){
		settings.get({}, function(data){
			updateAutosData(data);
		});
	}

	function loadSessionData(){
		settings.get({appId: $scope.appId}, function(data){
			updateSessionData(data);
		});
	}

	function updateAutosData(data){
		$scope.automonitoring = data.automonitoring;
		$scope.autoprofiling = data.autoprofiling;
	}

	function updateSessionData(data){
		$scope.monitoring = data.monitoring;
		$scope.profiling = data.profiling;
	}

	$scope.startAutoProfiling = function(){
		autoprofiling.get({action: "enable"}, function(data){
			updateAutosData(data);
		});
	}

	$scope.stopAutoProfiling = function(){
		autoprofiling.get({action: "disable"}, function(data){
			updateAutosData(data);
		});
	}

	$scope.startAutoMonitoring = function(){
		automonitoring.get({action: "enable"}, function(data){
			updateAutosData(data);
		});
	}

	$scope.stopAutoMonitoring = function(){
		automonitoring.get({action: "disable"}, function(data){
			updateAutosData(data);
		});
	}


	$scope.startProfiling = function(){
		recordControl.get({appId: $scope.appId, action:"startprofiling"}, function(data){
			updateSessionData(data);
		});
	}

	$scope.stopProfiling = function(){
		recordControl.get({appId: $scope.appId, action:"stopprofiling"}, function(data){
			updateSessionData(data);
		});
	}

	$scope.startMonitoring = function(){
		recordControl.get({appId: $scope.appId, action:"startmonitoring"}, function(data){
			updateSessionData(data);
		});
	}

	$scope.stopMonitoring = function(){
		recordControl.get({appId: $scope.appId, action:"stopmonitoring"}, function(data){
			updateSessionData(data);
		});
	}

	$scope.newRecord = function(){
		recordControl.get({appId: $scope.appId, sessionId:$scope.sessionId, action:"newrecord"}, function(data){
		});
	}

	$scope.flush = function(){
		flush.get({}, function(data){});
	}

	loadAutdata();
});
