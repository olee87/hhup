'use strict';

angular.module('hhupApp').controller('ConfirmResetCtrl', function($modalInstance, $scope) {
  $scope.yes = function() {
    $modalInstance.close(true);
  };

  $scope.no = function() {
    $modalInstance.close(false);
  };
});