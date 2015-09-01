(function (angular, window, undefined) {
    'use strict';

    /**
     * @ngdoc overview
     * @name sg
     * @description
     * # sg
     *
     * Main module of the application.
     */
    angular
        .module('sg', [
            'sg.main',
            'sg.model',
            'ngAnimate',
            'ngCookies',
            'ngResource',

            'ngSanitize',
            'ngTouch',
            'ui.bootstrap',
            'checklist-model',
            'angularFileUpload'
        ]);
    //
    //.config(function ($routeProvider) {
    //    $routeProvider
    //        .when('/', {
    //            templateUrl: 'views/main.html',
    //            controller: 'MainCtrl'
    //        })
    //        .when('/about', {
    //            templateUrl: 'views/about.html',
    //            controller: 'AboutCtrl'
    //        })
    //        .otherwise({
    //            redirectTo: '/'
    //        });
    //});
})(angular, window);

