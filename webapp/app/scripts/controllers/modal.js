'use strict';

angular.module('hhupApp').controller('ModalCtrl', function($uibModalInstance, $scope, message, title, buttons, inputMode, fields) {
  
  $scope.message = message;
  $scope.title = title;
  $scope.buttons = buttons;
  $scope.enter = {text: ''};
  $scope.fields = fields;
  $scope.result = {};
  $scope.inputMode = inputMode;

  angular.forEach(fields, function(field) {
    $scope.result[field.name] = '';
  });

  $scope.ok = function() {
    $uibModalInstance.close(true);
  };
  $scope.yes = function() {
    $uibModalInstance.close(true);
  };
  $scope.no = function() {
    $uibModalInstance.close(false);
  };
  $scope.cancel = function() {
    $uibModalInstance.dismiss();
  };
  $scope.save = function() {
    $uibModalInstance.close($scope.result);
  };
});