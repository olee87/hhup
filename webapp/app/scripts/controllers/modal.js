'use strict';

angular.module('hhupApp').controller('ModalCtrl', function($modalInstance, $scope, message, title, buttons, inputMode, fields) {
  
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
    $modalInstance.close(true);
  };
  $scope.yes = function() {
    $modalInstance.close(true);
  };
  $scope.no = function() {
    $modalInstance.close(false);
  };
  $scope.cancel = function() {
    $modalInstance.dismiss();
  };
  $scope.save = function() {
    $modalInstance.close($scope.result);
  };
});