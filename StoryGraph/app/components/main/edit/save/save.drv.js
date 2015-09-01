(function (angular, undefined) {
    'use strict';
    angular.module('sg.main').directive('saveBtn', ['$document', '$window', '$timeout', 'storyGraphService',
        function ($document, $window, $timeout, storyGraphService) {
            return {
                restrict: 'AC',
                link: function (scope, element /*, attrs*/) {
                    scope.getFilename = function () {
                        return 'story.json';
                    };
                    function doClick() {
                        var charset = 'utf-8';
                        var exportString = angular.toJson(storyGraphService.export(), true);
                        var blob = new Blob([exportString], {
                            type: 'text/json;charset=' + charset + ';'
                        });

                        if ($window.navigator.msSaveOrOpenBlob) {
                            $window.navigator.msSaveBlob(blob, scope.getFilename());
                        } else {

                            var downloadLink = angular.element('<a></a>');
                            downloadLink.attr('href', window.URL.createObjectURL(blob));
                            downloadLink.attr('download', scope.getFilename());
                            downloadLink.attr('target', '_blank');

                            $document.find('body').append(downloadLink);
                            $timeout(function () {
                                downloadLink[0].click();
                                downloadLink.remove();
                            }, null);
                        }
                    }

                    element.bind('click', function (/*e*/) {
                        scope.$apply(function () {
                            doClick();
                        });
                    });
                }
            };
        }
    ]);
})(angular);
