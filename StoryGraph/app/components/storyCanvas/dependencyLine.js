(function (angular, window, undefined) {
    'use strict';

    var eventWidth = 90,
        eventHeight = 60,
        typeFactor = 6;

    function getConnectionDirection(e1, e2) {
        var xDist = e2.x - e1.x,
            yDist = e2.y - e1.y;

        if (xDist === 0 && yDist === 0) {
            return 'above';
        }
        var ratio = Math.abs(xDist / yDist);
        if (ratio < 1) {
            //x is smaller than y - higher/lower
            return yDist > 0 ? 'below' : 'above';
        }
        return xDist > 0 ? 'left' : 'right';
    }

    function typeOffset(type) {
        switch (type) {
            case 'when':
                return 0;
            case 'where':
                return 1;
            case 'what':
                return -1;
            case 'who':
                return 2;
        }
        return 0;
    }

    function getConnectionCords(e1, e2, type) {
        if (!e1 || !e2) {
            return {x: 0, y: 0};
        }
        var x = e1.x, y = e1.y;
        switch (getConnectionDirection(e1, e2)) {
            //e2 is above
            case 'above':
                x += eventWidth / 2 + typeOffset(type) * typeFactor;
                break;
            //e2 is below
            case 'below':
                x += eventWidth / 2 + typeOffset(type) * typeFactor;
                y += eventHeight;
                break;
            //e2 is from the right
            case 'right':
                y += eventHeight / 2 + typeOffset(type) * typeFactor;
                break;
            case 'left':
                x += eventWidth;
                y += eventHeight / 2 + typeOffset(type) * typeFactor;
                break;
        }
        return {x: x, y: y};
    }

    angular.module('sg.storyCanvas')
        .directive('sgEventDependency', ['storyGraphService',
            function (storyGraphService) {
                return {
                    replace: true,
                    scope: {
                        dep: '=sgDependency'
                    },
                    templateUrl: '/components/storyCanvas/dependencyLine.tpl.html',
                    link: function (scope) {
                        var dependency = scope.dep,
                            e1, e2;

                        scope.select = function (event) {
                            var append = event.shiftKey;
                            storyGraphService.selectDependency(dependency, append);
                            event.stopPropagation();
                            event.preventDefault();
                        };

                        scope.getE1 = function () {
                            return getConnectionCords(e1, e2, dependency.type);
                        };
                        scope.getE2 = function () {
                            return getConnectionCords(e2, e1, dependency.type);
                        };
                        scope.$watchGroup(['dep.type', 'dep.selected'], function (values) {
                            var type = values[0],
                                selected = values[1];
                            scope.cssStyle = 'dep-class-' + type;
                            if (selected) {
                                scope.cssStyle += ' selected';
                            }
                        });
                        scope.$watch('dep.fromEventId', function (e1Id) {
                            e1 = storyGraphService.getEventById(e1Id);
                        });
                        scope.$watch('dep.toEventId', function (e2Id) {
                            e2 = storyGraphService.getEventById(e2Id);
                        });

                    }
                };
            }]);
})(angular, window);
