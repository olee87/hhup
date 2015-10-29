'use strict';

angular.module('hhupApp').directive('nationality', function($compile, nationalitiesService) {
  return {
    restrict : 'A',
    link : function(scope, elem, attrs) {
      var nationality = nationalitiesService.getForCode(attrs.nationality);
      if (nationality) {
        var newSpan = angular.element('<span class="' + nationality.flag + '" tooltip="' + nationality.natn + '"></span>');
        $compile(newSpan)(scope);
        elem.append(newSpan);
      }
    }
  };
});