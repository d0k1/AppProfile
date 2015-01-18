/**
 * Created by doki on 17.01.15.
 */

var methodsControllers = angular.module('methodsControllers', ['appsessionrec', 'dataview', 'ngResource']);

methodsControllers.factory("stat", function($resource) {
	return $resource("/methods/:appId/:sessionId/:recId");
});

methodsControllers.controller('methodsController', ['$scope', 'dataview', 'stat', function($scope, dataview, stat) {

	$scope.title = "Hot methods"
	function loadMethods() {
		$scope.recId = dataview.recId;
		stat.query({appId:dataview.appId, sessionId:dataview.sessionId, recId: dataview.recId}, function(data){
			window.alert('Result:'+data);
		});
	}

	$scope.onDataViewUpdated = function(){
		loadMethods();
	}
}]);
