/**
 * Created by doki on 10.06.14.
 */
var app = angular.module('bondApp', ['ngRoute', 'appsessionrec', 'sessionsControllers', 'jvmControllers', 'systemControllers', 'profilerControllers', 'methodsControllers', 'configuration', 'dataview']);

app.config(['$routeProvider',
	function($routeProvider) {
		$routeProvider.
			when('/profiler', {
				templateUrl: '/assets/templates/profiler.html',
				controller: 'profilerController'
			}).
			when('/methods', {
				templateUrl: '/assets/templates/methods.html',
				controller: 'methodsController'
			}).
			when('/system', {
				templateUrl: '/assets/templates/system.html',
				controller: 'systemController'
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

app.controller('menuController', function($scope, urls, $location, $route){
	$scope.item = 0
	$scope.urls = urls

	var url = $location.url();
	if(url.indexOf(urls.sessions)==0){
		$scope.item = 0;
	}
	if(url.indexOf(urls.profiler)==0){
		$scope.item = 1;
	}
	if(url.indexOf(urls.methods)==0){
		$scope.item = 2;
	}
	if(url.indexOf(urls.system)==0){
		$scope.item = 3;
	}
	if(url.indexOf(urls.jvm)==0){
		$scope.item = 4;
	}
});

