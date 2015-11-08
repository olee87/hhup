'use strict';

angular.module('hhupApp').controller('ConfirmResetCtrl', function($uibModalInstance, $scope) {
  $scope.yes = function() {
    $uibModalInstance.close(true);
  };

  $scope.no = function() {
    $uibModalInstance.close(false);
  };
});