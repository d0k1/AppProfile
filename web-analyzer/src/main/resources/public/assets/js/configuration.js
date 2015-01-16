/**
 * Created by doki on 12.06.14.
 */

var configuration = angular.module('configuration', [])

configuration.constant('urls', {
	sessions:'/',
	profiler:'/profiler',
	methods: '/methods',
	system:'/system',
	jvm:'/jvm'
})
