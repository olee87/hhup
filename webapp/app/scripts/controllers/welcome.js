'use strict';

angular.module('hhupApp').controller('WelcomeCtrl', function ($scope, $interval, restService) {

  restService.stats().get().$promise.then(function(stats) {
    $scope.stats = stats;

    var interval = $interval(function() {
      var now = new Date();
      var then = new Date(stats.eventBegin);

      if (then.getTime() - now.getTime() < 0) {
        $scope.remainingTime = 'has started already, qick =O';
        return;
      }

      if (new Date(stats.eventEnd).getTime() - now.getTime() < 0) {
        $scope.remainingTime = 'is over X(';
        return;
      }

      var days = Math.floor((then.getTime() - now.getTime() + 1000) / (24 * 60 * 60 * 1000));
      var hours = then.getHours() - now.getHours();
      var minutes = then.getMinutes() - now.getMinutes();
      var seconds = then.getSeconds() - now.getSeconds();

      if (seconds < 0) {seconds += 60; minutes -= 1;}
      if (minutes < 0) {minutes += 60; hours -= 1;}
      if (hours < 0) {hours += 24;}
      // no need to correct the days.

      $scope.remainingTime = 'starts in ' + days + ' days, ' + hours + ' hours, ' + minutes + ' minutes and ' + seconds + ' seconds =)';
    }, 1000);

    $scope.$on('$destroy', function(){
      $interval.cancel(interval);
    });
  });
});