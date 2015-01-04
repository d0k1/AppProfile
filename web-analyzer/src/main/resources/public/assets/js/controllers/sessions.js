/**
 * Created by doki on 01.07.14.
 */

var sessionsControllers = angular.module('sessionsControllers', ['appsessionrec', 'dataview', 'ngResource']);

sessionsControllers.factory("appIds", function($resource) {
	return $resource("/sessions/apps");
});

sessionsControllers.factory("sessionIds", function($resource) {
	return $resource("/sessions/:appId");
});

sessionsControllers.controller('sessionsController', function($scope, dataview, appIds, sessionIds){
	$scope.title = 'Measurement sessions'

	$scope.session = null;

	$scope.onDataViewUpdated = function(view, appIds, sessionIds, recIds){
		$scope.session = sessionIds[view.sessionId-1];
	}

});
