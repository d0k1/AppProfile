/**
 * Created by doki on 01.07.14.
 */

var jvmControllers = angular.module('jvmControllers', ['dataview', 'highcharts-ng', 'ngResource']);

jvmControllers.factory("lastcpu", function($resource) {
                         return $resource("/jvm/:appId/:sessionId/cpu/last/:seconds");
                       });

jvmControllers.factory("lastheap", function($resource) {
                         return $resource("/jvm/:appId/:sessionId/heap/last/:seconds");
                       });

jvmControllers.factory("cpu", function($resource) {
	return $resource("/jvm/:appId/:sessionId/cpu/:max/:min");
});

jvmControllers.factory("heap", function($resource) {
	return $resource("/jvm/:appId/:sessionId/heap/:max/:min");
});

jvmControllers.controller('jvmController', function($scope, lastcpu, lastheap, $interval, cpu, heap, dataview){
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

	$scope.appId = dataview.appId;
	$scope.sessionId = dataview.sessionId;

	$scope.timestampMin = -1;
	$scope.timestampMax = -1;

	$scope.lastSeconds = 30;
	$scope.timeStepSeconds = 15;

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

	function loadCpuData(data){
		var data0 = []
		var data1 = []

		if(data.length>0){
			$scope.timestampMax = data[0].timestamp;
			$scope.timestampMin = data[data.length - 1].timestamp;
			$scope.minTime = new Date($scope.timestampMin).toString("hh:mm:ss dd.mm.yyyy")
			$scope.maxTime = new Date($scope.timestampMax).toString("hh:mm:ss dd.mm.yyyy");
		}

		for(var i=data.length-1;i>=0;i--){
			data0.push([new Date(data[i].timestamp).toLocaleString(), data[i].process])
			data1.push([new Date(data[i].timestamp).toLocaleString(), data[i].system])
		}
		$scope.cpuChartSeries[0].data = data0;
		$scope.cpuChartSeries[1].data = data1;

	}

	function loadHeapData(data){
		var data0 = []
		var data1 = []

		for(var i=data.length-1;i>=0;i--){
			data0.push([new Date(data[i].timestamp).toLocaleString(), Math.round(data[i].heapUsed / (1024.0*1024.0))])
			data1.push([new Date(data[i].timestamp).toLocaleString(), Math.round(data[i].heapCommited / (1024.0*1024.0))])
		}
		$scope.heapChartSeries[0].data = data0;
		$scope.heapChartSeries[1].data = data1;
	}

	function loadData(){
		lastcpu.query({appId:$scope.appId, sessionId: $scope.sessionId, seconds: $scope.lastSeconds }, function(data) {
			loadCpuData(data)
		});

		lastheap.query({appId:$scope.appId, sessionId: $scope.sessionId, seconds: $scope.lastSeconds }, function(data) {
			loadHeapData(data)
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

	//loadApps();
	//$scope.loadSessions = function(){loadSessions()}
	//$scope.loadData = function(){loadData()};

	if($scope.appId>0 && $scope.sessionId>0){
		loadData();
	}

	$scope.lastData = function(){
		loadData();
	}

	$scope.prev = function(){
		$interval.cancel();
		cpu.query({appId:$scope.appId, sessionId: $scope.sessionId, max: $scope.timestampMax-$scope.timeStepSeconds*1000, min:$scope.timestampMin-$scope.timeStepSeconds*1000}, function(data) {
			loadCpuData(data)
		});

		heap.query({appId:$scope.appId, sessionId: $scope.sessionId, max: $scope.timestampMax-$scope.timeStepSeconds*1000, min:$scope.timestampMin-$scope.timeStepSeconds*1000}, function(data) {
			loadHeapData(data)
		});
	}

	$scope.next = function(){
		$interval.cancel();
		cpu.query({appId:$scope.appId, sessionId: $scope.sessionId, max: $scope.timestampMax+$scope.timeStepSeconds*1000, min:$scope.timestampMin+$scope.timeStepSeconds*1000}, function(data) {
			loadCpuData(data)
		});

		heap.query({appId:$scope.appId, sessionId: $scope.sessionId, max: $scope.timestampMax+$scope.timeStepSeconds*1000, min:$scope.timestampMin+$scope.timeStepSeconds*1000}, function(data) {
			loadHeapData(data)
		});
	}

	$scope.start = function(){
		$interval(function(){loadData()}, 1000, 0);
	}

	$scope.stop = function(){
		$interval.cancel();
	}


	//loadData($scope);
});
