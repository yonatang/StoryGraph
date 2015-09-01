(function (angular, window, undefined) {
    'use strict';
    var module = angular.module('sg.main', [
        'sg.storyCanvas',
        'sg.services',
        'monospaced.elastic',
        'ngRoute'
    ]);
    module.controller('MainController', ['$scope', '$window', 'storyGraphService', 'editEvent', 'buildData',
        function ($scope, $window, storyGraphService, editEvent, buildData) {
            var ctrl = this;
            ctrl.buildData = buildData;
            console.log('buildData', buildData);

            var CONSTRAINT_TYPES = [
                {name: 'where'}, {name: 'when'}, {name: 'who'}
            ];

            ctrl.constraintTypes = CONSTRAINT_TYPES;
            ctrl.newDependencyData = {};
            ctrl.eventToRemove = null;
            ctrl.dependencyToRemove = null;
            ctrl.events = storyGraphService.events;
            ctrl.dependencies = storyGraphService.dependencies;
            ctrl.state = storyGraphService.state;
            ctrl.profile = storyGraphService.profile;

            ctrl.canDelete = function () {
                return ctrl.state.numSelected > 0;
            };
            ctrl.canEdit = function () {
                return ctrl.state.numSelected === 1;
            };
            ctrl.edit = function () {
                if (ctrl.state.selectedEvents.length === 1) {
                    editEvent(ctrl.state.selectedEvents[0]);
                } else {
                    //TODO editDependency(ctrl.state.selectedDependencies[0]);
                }
            };
            ctrl.addEvent = function () {
                storyGraphService.addNewEvent(10 + Math.random() * 100, 10 + Math.random() * 100);
                ctrl.newEventData = {};
            };
            ctrl.addDependency = function () {
                var newDependencyData = ctrl.newDependencyData;
                storyGraphService.addDependency(newDependencyData.w.id, newDependencyData.v.id,
                    newDependencyData.type.name, newDependencyData.data);
                ctrl.newDependencyData = {};
            };
            ctrl.removeDependency = function () {
                var dependencyToRemove = ctrl.dependencyToRemove;
                storyGraphService.removeDependency(dependencyToRemove);
                ctrl.edgeToRemove = null;
            };
            ctrl.removeSelected = function () {
                storyGraphService.removeEvent(storyGraphService.state.selectedEvents);
                storyGraphService.removeDependency(storyGraphService.state.selectedDependencies);
            };
            ctrl.fileSelected = function (files/*, event*/) {
                if (!files || files.length === 0) {
                    return false;
                }
                var reader = new FileReader();
                reader.onloadend = function (evt) {
                    var json = evt.target.result;
                    $scope.$apply(function () {
                        try {
                            var graph = angular.fromJson(json);
                            storyGraphService.import(graph);
                        } catch (e) {
                            $window.alert('Error while parsing the json file!\n' + e);
                        }
                    });

                };
                reader.onerror = function () {
                    $window.alert('Error while loading the file');
                };
                reader.readAsText(files[0]);
            };

            ctrl.helpText =
                '<div class="help-text">' +
                '<li>Use <kbd>SHIFT</kbd> for multiselect</li>' +
                '<li>Double click on canvas to add an event</li>' +
                '<li>Double click on an event to edit it</li>' +
                '<li>Click on canvas to deselect all</li>' +
                '</div>';
        }])
        .config(['$routeProvider', function ($routeProvider) {
            $routeProvider
                .when('/', {
                    templateUrl: '/components/main/main.html',
                    controller: 'MainController as ctrl'
                })
                .otherwise({
                    redirectTo: '/'
                });
        }])
        .constant('buildData', window.build);

}(angular, window));
