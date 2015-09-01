(function (angular, $, undefined) {
    'use strict';

    angular.module('sg.main')
        .controller('AddDependencyController', ['$modalInstance', 'storyGraphService', 'sgEvent', 'profile',
            'TimeDependency', 'LocationDependency', 'CharacterDependency', 'ThingDependency',
            function ($modalInstance, storyGraphService, sgEvent, profile, TimeDependency,
                      LocationDependency, CharacterDependency, ThingDependency) {
                var ctrl = this;
                ctrl.events = storyGraphService.events;
                ctrl.sgEvent = sgEvent;
                ctrl.targetEvents = [];
                ctrl.type = null;
                var DependencyObject = null;
                ctrl.operators = null;
                ctrl.operator = null;
                ctrl.depValue = {};
                ctrl.changeType = function () {
                    switch (ctrl.type) {
                        case 'who':
                            DependencyObject = CharacterDependency;
                            break;
                        case 'where':
                            DependencyObject = LocationDependency;
                            break;
                        case 'when':
                            DependencyObject = TimeDependency;
                            break;
                        case 'what':
                            DependencyObject = ThingDependency;
                            break;
                        default:
                            DependencyObject = null;
                    }
                    ctrl.operators = DependencyObject.operators;
                    ctrl.operator = null;
                    ctrl.depValue = {};
                    ctrl.extraN = null;
                };
                ctrl.cancel = function () {
                    return $modalInstance.dismiss();
                };
                ctrl.isFormInvalid = function () {
                    return ctrl.targetEvents.length === 0;
                };
                ctrl.ok = function () {
                    var dependencies = [];
                    angular.forEach(ctrl.targetEvents, function (eventId) {
                        var dependency = new DependencyObject(ctrl.sgEvent.id, eventId);
                        dependency.operator = dependency.operators[ctrl.operator.id];
                        $.extend(dependency, ctrl.depValue);
                        if (dependency.operator.extraN) {
                            dependency.extraN = ctrl.extraN;
                        }
                        dependencies.push(dependency);
                    });
                    return $modalInstance.close(dependencies);
                };
            }])
        .factory('addEditDependency', ['$modal', function ($modal) {
            return function (sgEvent, profile) {
                var modalInstance = $modal.open({
                    templateUrl: '/components/main/edit/dependency/addDependency.tpl.html',
                    controller: 'AddDependencyController as ctrl',
                    backdrop: true,
                    size: 'sm',
                    resolve: {
                        sgEvent: function () {
                            return sgEvent;
                        },
                        profile: function () {
                            return profile;
                        }
                    }
                });
                return modalInstance;
            };
        }]);
})(angular, jQuery);
