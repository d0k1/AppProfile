/**
 * Created by doki on 01.07.14.
 */

var jvmControllers = angular.module('jvmControllers', ['services', 'highcharts-ng', 'ngResource']);

jvmControllers.factory("appIds", function($resource) {
	return $resource("/sessions/apps");
});

jvmControllers.factory("sessionIds", function($resource) {
	return $resource("/sessions/:appId");
});


jvmControllers.factory("lastcpu", function($resource) {
                         return $resource("/jvm/:appId/:sessionId/cpu/last/:seconds");
                       });

jvmControllers.factory("lastheap", function($resource) {
                         return $resource("/jvm/:appId/:sessionId/heap/last/:seconds");
                       });

jvmControllers.controller('jvmController', function($scope, appIds, sessionIds, lastcpu, lastheap, $interval){
	$scope.title = 'JVM Monitoring'

	$scope.cpuChartSeries = [
		{"name": "JVM cpu usage", "data": []},
		{"name": "System cpu usage", "data": []},
	];

	$scope.heapChartSeries = [
		{"name": "Heap used", "data": []},
		{"name": "Heap commited", "data": []},
	];

	$scope.apps = [];
	$scope.sessions = [];

	$scope.appId = -1;
	$scope.sessionId = -1;

	function loadApps(){
		appIds.query(function(data){
			for(var i=0;i<data.length;i++){
				$scope.apps.push(data[i]);
			}
		});
	}

	function loadSessions(){
		sessionIds.query({appId: $scope.appId}, function(data){
			for(var i=0;i<data.length;i++){
				$scope.sessions.push(data[i]);
			}
		});
	}

	function loadData(){
		lastcpu.query({appId:$scope.appId, sessionId: $scope.sessionId, seconds: 60 }, function(data) {
			var data0 = []
			var data1 = []
			for(var i=data.length-1;i>=0;i--){
				data0.push([new Date(data[i].timestamp).toLocaleString(), data[i].process])
				data1.push([new Date(data[i].timestamp).toLocaleString(), data[i].system])
			}
			$scope.cpuChartSeries[0].data = data0;
			$scope.cpuChartSeries[1].data = data1;
		});

		lastheap.query({appId:$scope.appId, sessionId: $scope.sessionId, seconds: 60 }, function(data) {
			var data0 = []
			var data1 = []

			for(var i=data.length-1;i>=0;i--){
				data0.push([new Date(data[i].timestamp).toLocaleString(), Math.round(data[i].heapUsed / (1024.0*1024.0))])
				data1.push([new Date(data[i].timestamp).toLocaleString(), Math.round(data[i].heapCommited / (1024.0*1024.0))])
			}
			$scope.heapChartSeries[0].data = data0;
			$scope.heapChartSeries[1].data = data1;
		});
	}

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

	loadApps();
	$scope.loadSessions = function(){loadSessions()}
	$scope.loadData = function(){loadData()};

	$scope.start = function(){
		$interval(function(){loadData()}, 1000, 0);
	}

	$scope.stop = function(){
		$interval.cancel();
	}


	//loadData($scope);
});
