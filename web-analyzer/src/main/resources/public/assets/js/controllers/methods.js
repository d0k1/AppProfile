/**
 * Created by doki on 17.01.15.
 */

var methodsControllers = angular.module('methodsControllers', ['appsessionrec', 'dataview', 'ngResource']);

methodsControllers.factory("stat", function($resource) {
	return $resource("/methods/:appId/:sessionId/:recId");
});

methodsControllers.controller('methodsController', ['$scope', 'dataview', 'stat', function($scope, dataview, stat) {

	$scope.title = "Methods"
	$scope.totalCount = 0;
	$scope.totalTime = 0;
	$scope.threads = [];

	function loadMethods() {
		$scope.recId = dataview.recId;
		stat.query({appId:dataview.appId, sessionId:dataview.sessionId, recId: dataview.recId}, function(data){
			$scope.methods = [];
			$scope.totalCount = 0;
			$scope.totalTime = 0;
			$scope.threads = [];
			for (var i = 0; i < data.length; i++) {
				$scope.totalCount = $scope.totalCount+1;
				$scope.totalTime = $scope.totalTime + data[i].totalTime;

				var threads = data[i].threads;
				for(var j=0;j<threads.length;j++) {
					if($scope.threads.indexOf(threads[j])<0){
						$scope.threads.push(threads[j]);
					}
				}

				$scope.methods.push(data[i]);
			}
		});
	}

	$scope.onDataViewUpdated = function(){
		loadMethods();
	}
}]);
