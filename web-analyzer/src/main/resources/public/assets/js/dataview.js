var dataview = angular.module('dataview', []);

dataview.service('dataview', function() {

	var appId = -1;
	var sessionId = -1;
	var recId = -1;

	return ({appId: appId, sessionId: sessionId, recId: recId})
});
