/**
 * Created by doki on 01.07.14.
 */

var jvmControllers = angular.module('jvmControllers', ['services', 'highcharts-ng', 'ngResource']);

jvmControllers.factory("lastcpu", function($resource) {
                         return $resource("/jvm/:sessionId/cpu/last/:seconds");
                       });

jvmControllers.factory("lastheap", function($resource) {
                         return $resource("/jvm/:sessionId/heap/last/:seconds");
                       });

jvmControllers.controller('jvmController', function($scope, lastcpu, lastheap){
	$scope.title = 'JVM Monitoring'

	$scope.cpuChartSeries = [
		{"name": "JVM cpu usage", "data": []},
		{"name": "System cpu usage", "data": []},
	];

	lastcpu.query({ sessionId: 1, seconds: 300 }, function(data) {

		for(var i=data.length-1;i>=0;i--){
			$scope.cpuChartSeries[0].data.push([new Date(data[i].timestamp).toLocaleString(), data[i].process])
			$scope.cpuChartSeries[1].data.push([new Date(data[i].timestamp).toLocaleString(), data[i].system])
		}
	});

	$scope.heapChartSeries = [
		{"name": "Heap used", "data": []},
		{"name": "Heap commited", "data": []},
	];

	lastheap.query({ sessionId: 1, seconds: 300 }, function(data) {

        for(var i=data.length-1;i>=0;i--){
	        $scope.heapChartSeries[0].data.push([new Date(data[i].timestamp).toLocaleString(), Math.round(data[i].heapUsed / (1024.0*1024.0))])
	        $scope.heapChartSeries[1].data.push([new Date(data[i].timestamp).toLocaleString(), Math.round(data[i].heapCommited / (1024.0*1024.0))])
        }
      });

	$scope.heapChartConfig = {
		options: {
			chart: {
				type: 'line'
			},
			plotOptions: {
				series: {
					stacking: ''
				}
			}
		},
		series: $scope.heapChartSeries,
		title: {
			text: 'JVM heap usage'
		},
		credits: {
			enabled: true
		},
		loading: false,
		size: {}
	}

	$scope.cpuChartConfig = {
		options: {
			chart: {
				type: 'line'
			},
			plotOptions: {
				series: {
					stacking: ''
				}
			}
		},
		series: $scope.cpuChartSeries,
		title: {
			text: 'CPU usage'
		},
		credits: {
			enabled: true
		},
		loading: false,
		size: {}
	}

});
