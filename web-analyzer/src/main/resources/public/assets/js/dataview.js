var dataview = angular.module('dataview', ['ngResource']);

dataview.service('dataview', function($resource) {

	var appId = -1;
	var sessionId = -1;

	return ({appId: appId, sessionId: sessionId})
});
