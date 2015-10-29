'use strict';

angular.module('hhupApp').controller('ConfirmRecoverPasswordCtrl', function ($scope, $routeParams, $location, restService, modalService) {

  $scope.newPassword = '';
  $scope.newPasswordConfirm = '';
  $scope.invalid = true;

  var validate = function() {
    $scope.invalid = ($scope.newPassword === '' || $scope.newPassword !== $scope.newPasswordConfirm);
  };

  $scope.$watch('newPassword', validate);
  $scope.$watch('newPasswordConfirm', validate);

  $scope.send = function() {
    restService.users().confirmRecoverPassword({password: $scope.newPassword, token: $routeParams.code}, function() {
      var modal = modalService.message('password changed', 'Your password has been changed successfully. You may now log in with your new password.');
      modal.result.then(function() {
        $location.path('/');
      });
    },
    function (error) {
      if (error.status === 400) {
        modalService.message('error', 'The link you clicked is no longer valid, please request a new one if you still need to recover your password.');
      }
    });
  };
});