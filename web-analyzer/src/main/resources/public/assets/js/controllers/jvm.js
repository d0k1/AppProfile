/**
 * Created by doki on 01.07.14.
 */

var jvmControllers = angular.module('jvmControllers', ['services', 'chartjs']);

jvmControllers.controller('jvmController', function($scope){
	$scope.title = 'JVM Monitoring'

	$scope.someData = {}
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
