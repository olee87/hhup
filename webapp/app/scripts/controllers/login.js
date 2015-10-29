'use strict';

angular.module('hhupApp').controller('LoginCtrl', function($modalInstance, $location, $scope, authenticationService) {

  $scope.error = null;
  $scope.login = {
    rememberMe: true,
    username: '',
    password: ''
  };

  $scope.checkEnable = function() {
    if ($scope.login.username === undefined || $scope.login.password === undefined ||
        $scope.login.username === '' || $scope.login.password === '') {
      $scope.login.disabled = true;
    } else {
      $scope.login.disabled = false;
    }
  };

  $scope.doLogin = function () {
    $scope.sending = true;
    authenticationService.login($scope.login);

    $scope.$on('event:auth-loginConfirmed', function() {
      $scope.sending = false;
      $modalInstance.close();
    });

    $scope.$on('event:auth-loginCancelled', function() {
      setTimeout(function(){
          $scope.sending = false;
          $scope.login.password = '';
          $scope.error = 'username / password combination does not exist!';
      }, 2000);
    });
  };

  $scope.cancel = function() {
    $modalInstance.dismiss();
  };
  
  $scope.deleteError = function() {
    $scope.error = null;
  };

  $scope.$watch('login.password', function() {
    $scope.checkEnable();
  });

  $scope.$watch('login.username', function() {
    $scope.checkEnable();
  });
  
  $scope.forgotPassword = function() {
    $modalInstance.close();
    $location.path('/recoverPassword');
  };
});