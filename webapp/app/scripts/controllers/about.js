'use strict';

angular.module('hhupApp').controller('AboutCtrl', function ($scope, authenticationService) {
  $scope.loggedIn = authenticationService.userId() ? true : false;
});