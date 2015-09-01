(function (angular, undefined) {
  'use strict';
  angular.module('annotatorApp').directive('saveBtn', ['$document', '$window', '$timeout',
    function ($document, $window, $timeout) {
      return {
        scope : {
          saveBtn : '=',
          saveBtnFilename : '=?'
        },
        restrict: 'AC',
        link: function (scope, element /*, attrs*/) {
          scope.getFilename = function () {
            return scope.saveBtnFilename || 'annotatedSet.json';
          };
          function doClick() {
            var charset = 'utf-8';
            var exportString = angular.toJson(scope.saveBtn, true);
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
