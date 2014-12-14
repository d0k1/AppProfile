/**
 * Created by doki on 12.06.14.
 */

var services = angular.module('services', []);

services.service('dispatch', function($http, $q, $log){
	return ({execute: execute})

	function execute(actionClass, actionData, authToken, okCallbak){

		var action = {
			actionClass:actionClass,
			actionData: JSON.stringify(actionData),
			authToken: authToken
		}


		var request = $http({
			method: "post",
			url: "/api/action/v1",
			data: angular.toJson(action)
		});

		request.then( success, fail).then(okCallbak, function(data){alert('Код '+data.status+'. Ошибка при выполнении запроса '+actionClass+' данные '+JSON.stringify(actionData))});
	}

	function execute(actionClass, actionData, authToken, okCallbak, failCallback){

		var action = {
			actionClass:actionClass,
			actionData: JSON.stringify(actionData),
			authToken: authToken
		}


		var request = $http({
			method: "post",
			url: "/api/action/v1",
			data: angular.toJson(action)
		});

		request.then( success, fail).then(okCallbak, function(data){alert('Код '+data.status+'. Ошибка при выполнении запроса '+actionClass+' данные '+JSON.stringify(actionData)); failCallback(data);});
	}

	function success(response) {
		return( response.data );
	}

	function fail(response){
		$log.error("Status "+response.status+" Data "+response.data);

		return( $q.reject( response ) );
	}
})
