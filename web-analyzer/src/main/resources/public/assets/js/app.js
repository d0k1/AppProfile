/**
 * Created by doki on 10.06.14.
 */
var app = angular.module('bondApp', ['ngRoute', 'sessionsControllers', 'jvmControllers', 'threadsControllers', 'methodsControllers', 'configuration', 'services']);

app.config(['$routeProvider',
	function($routeProvider) {
		$routeProvider.
			when('/methods', {
				templateUrl: '/assets/templates/methods.html',
				controller: 'methodsController'
			}).
			when('/threads', {
				templateUrl: '/assets/templates/threads.html',
				controller: 'threadsController'
			}).
			when('/jvm', {
				templateUrl: '/assets/templates/jvm.html',
				controller: 'jvmController'
			}).
			when('/', {
				templateUrl: '/assets/templates/sessions.html',
				controller: 'sessionsController'
			}).otherwise ({
			redirectTo: '/'
		});
	}]);

app.run(function ($rootScope, urls) {
	$rootScope.urls = urls
});

app.controller('menuController', function($scope, urls){
	$scope.item = 0
	$scope.urls = urls
});

