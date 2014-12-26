/**
 * Created by doki on 01.07.14.
 */

var jvmControllers = angular.module('jvmControllers', ['services', 'chartjs', 'ngResource']);

jvmControllers.factory("allheap", function($resource) {
                         return $resource("/jvm/:sessionId/heap");
                       });

jvmControllers.factory("lastheap", function($resource) {
                         return $resource("/jvm/:sessionId/heap/last/:seconds");
                       });

jvmControllers.controller('jvmController', function($scope, allheap, lastheap){
	$scope.title = 'JVM Monitoring'

	$scope.someData = {datasets:[]}

	allheap.get({ sessionId: 1 }, function(data) {
        for(var sample in data){

        }
      });


	//$scope.someData = {
	//	labels: [
	//		'Apr',
	//		'May',
	//		'Jun'
	//	],
	//	datasets: [
	//		{
	//			data: [1, 7, 15, 19, 31, 40]
	//		},
	//		{
	//			data: [6, 12, 18, 24, 30, 36]
	//		}
	//	]
	//};

	$scope.someOptions = {
		segementStrokeWidth: 20,
		segmentStrokeColor: '#000'
	};
});
