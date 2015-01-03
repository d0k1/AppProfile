/**
 * Created by doki on 12.06.14.
 */

var configuration = angular.module('configuration', [])

configuration.constant('urls', {
	sessions:'/',
	methods:'/profiler',
	threads:'/system',
	jvm:'/jvm'
})
