(function (angular, undefined) {
    'use strict';
    angular.module('sg.main').directive('selectCharacters', ['storyGraphService', function (storyGraphService) {
        return {
            scope: {
                container: '=' //either constraint or dependency object
            },
            restrict: 'E',
            templateUrl: '/components/main/edit/select-characters/selectCharacters.tpl.html',
            link: function (scope /*, element, attrs*/) {
                scope.selectAllInGroup = function selectAllInGroup() {
                    if (!scope.container.value) {
                        scope.container.value = [];
                    }
                    var value = scope.container.value,
                        group = scope.container.group.id;
                    angular.forEach(scope.options, function (option) {
                        var idx = value.indexOf(option);
                        if ((option.groups.indexOf(group) > -1) && (idx === -1)) {
                            value.push(option);
                        }
                    });
                };
                scope.deselectAllInGroup = function deselectAllInGroup() {
                    var value = scope.container.value,
                        group = scope.constraint.group.id;
                    angular.forEach(scope.options, function (option) {
                        var idx = value.indexOf(option);
                        if ((option.groups.indexOf(group) > -1) && (idx > -1)) {
                            value.splice(idx, 1);
                        }
                    });
                };

                scope.groupFilter = function groupFilter(option) {
                    if (!scope.container.group) {
                        return true;
                    }
                    var group = scope.container.group.id;
                    return (option.groups.indexOf(group) > -1);
                };

                scope.profile = storyGraphService.profile;
                scope.options = scope.profile.characters;

            }
        };

    }]);
})(angular);
