'use strict';

angular.module('hhupApp').directive('scrollFollow', function($window) {
  return {
    scope: {},
    link: function(scope, element, attrs) {
      var windowEl = angular.element($window);
      var handler = function() {
        var scroll = windowEl.scrollTop();
        if (attrs.offset) {
          scroll += attrs.offset;
        }
        element.css('top', scroll + 'px');
      };
      windowEl.on('scroll', scope.$apply.bind(scope, handler));
      handler();
    }
  };
});