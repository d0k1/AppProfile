angular.module('eventservice', [], function($provide) {
	$provide.factory('eventBus', ['$rootScope', function($rootScope) {
		var eventBus = {};
		eventBus.emitMsg = function(msg) {
			$rootScope.$emit(msg);
		};
		eventBus.onMsg = function(msg, scope, func) {
			var unbind = $rootScope.$on(msg, func);
			scope.$on('$destroy', unbind);
		};
		return eventBus;
	}]);
});
