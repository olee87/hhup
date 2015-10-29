'use strict';

angular.module('hhupApp').controller('ConfirmUserCtrl', function($scope, $routeParams, restService) {
  $scope.confirmation = 'pending';
  restService.users().confirm({code: $routeParams.code}).$promise.then(function() {
    $scope.confirmation = 'successful';
  }, function() {
    $scope.confirmation = 'unsuccessful';
  });
});