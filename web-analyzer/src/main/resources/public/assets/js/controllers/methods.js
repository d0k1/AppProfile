/**
 * Created by doki on 17.01.15.
 */

var methodsControllers = angular.module('methodsControllers', ['appsessionrec', 'dataview', 'ngResource']);

methodsControllers.controller('methodsController', ['$scope', 'dataview', function($scope, dataview) {

	$scope.title = "Hot methods"

}]);
