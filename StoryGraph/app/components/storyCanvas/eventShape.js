(function (angular, undefined) {
    'use strict';

    angular.module('sg.storyCanvas')
        .directive('sgEventShape', ['storyGraphService', 'dragging', 'editEvent',
            function (storyGraphService, dragging, editEvent) {
                return {
                    replace: true,
                    scope: {
                        sgEvent: '=sgEvent'
                    },
                    templateUrl: '/components/storyCanvas/eventShape.tpl.html',
                    link: function (scope, element) {
                        scope.radius = 30;
                        element.css('cursor', 'pointer');
                        var xClick, yClick;
                        scope.dblClick = function (event, sgEvent) {
                            editEvent(sgEvent);
                            event.stopPropagation();
                            event.preventDefault();
                        };
                        scope.nodeMouseDown = function (event, sgEvent) {
                            dragging.startDrag(event, {
                                dragStarted: function (x, y) {
                                    var offset = element.offset();
                                    xClick = x - offset.left;
                                    yClick = y - offset.top;
                                },
                                dragging: function (x, y, evt) {
                                    scope.sgEvent.x = evt.offsetX - xClick;
                                    scope.sgEvent.y = evt.offsetY - yClick;
                                },
                                clicked: function () {
                                    var append = event.shiftKey;
                                    storyGraphService.selectEvent(sgEvent, append);
                                    event.stopPropagation();
                                    event.preventDefault();
                                },
                                dragEnded: function () {
                                }
                            });
                        };
                    }
                };
            }]);
})(angular);
