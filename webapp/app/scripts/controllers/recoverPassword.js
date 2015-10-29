'use strict';

angular.module('hhupApp').controller('RecoverPasswordCtrl', function ($scope, $location, restService, modalService, validationService) {
  $scope.usernameOrEmail = '';

  var success = function() {
    var modal = modalService.message('password recovery requested', 'We have sent an e-mail to your account. Please click the link provided in it to recover your password.');
    modal.result.then(function() {
      $location.path('/');
    });
  };

  $scope.send = function() {
    if (validationService.isInvalidEmail($scope.usernameOrEmail)) {
      // its not an email address so it must be a username
      restService.users().recoverPassword({username: $scope.usernameOrEmail}, function() {success();});
    } else {
      // its an email address
      restService.users().recoverPassword({email: $scope.usernameOrEmail}, function() {success();});
    }
  };
});