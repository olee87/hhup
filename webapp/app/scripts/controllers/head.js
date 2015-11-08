'use strict';

angular.module('hhupApp').controller('HeadCtrl', function ($rootScope, $scope, $uibModal, $location, authenticationService, restService, modalService) {

  authenticationService.autoLogin();

  restService.activities().all().$promise.then(function(activities) {
    $scope.activities = activities;
  });

  $scope.$on('event:auth-loginConfirmed', function(event, data) {
    $rootScope.user = data;
    $scope.user = data;
    $scope.admin = data.authorities.indexOf('ADMIN') >= 0;
    restService.loginMessage().get({}, function(loginMessage) {
      if (loginMessage.active) {
        modalService.message('important', loginMessage.message);
      }
    });
  });

  $scope.$on('event:auth-loginCancelled', function() {
    $scope.user = null;
    $scope.admin = false;
    $location.path('/');
  });

  $scope.login = function() {
    $uibModal.open({
      templateUrl: 'views/login.html',
      controller: 'LoginCtrl'
    });
  };

  $scope.logout = function() {
    authenticationService.logout();
  };

  $scope.guestbook = function() {
    modalService.message('nope', 'this is not implemented yet');
  };
  
  $scope.orderDate = function(activity) {
    return new Date(activity.startDate);
  };
});