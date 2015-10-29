'use strict';

angular.module('hhupApp').service('formatService', function($filter) {
  
  this.startEndDateString = function(startString, endString) {
    var start = new Date(startString);
    var end = new Date(endString);
    var isSameDay = (start.getDate() === end.getDate() &&
            start.getMonth() === end.getMonth() &&
            start.getFullYear() === end.getFullYear());

    var timeString = $filter('date')(start, 'EEE, dd.MM. HH:mm');
    if (isSameDay) {
      timeString += ' - ' + $filter('date')(end, 'HH:mm');
    } else {
      timeString += ' - ' + $filter('date')(end, 'EEE, dd.MM. HH:mm');
    }
    return timeString;
  };

  this.stripHtml = function(html) {
    console.log('start replacing');
    var result = html.replace(/<(?:.|\n)*?>/gm, '');
    console.log('start replacing');
    return result;
  };
});