'use strict';

angular.module('hhupApp').controller('PayCtrl', function ($filter, $scope, $sce, authenticationService, restService) {

  authenticationService.checkLoggedIn();

  $scope.userId = authenticationService.userId();
  $scope.currentUser = authenticationService.user();
  $scope.availableUsers = [];
  $scope.user = {};

  if (!$scope.currentUser.paid) {
    $scope.user.selected = [$scope.currentUser];
  } else {
    $scope.user.selected = [];
    $scope.userIsPaid = true;
    $scope.payMultiple = true;
  }

  restService.paypal().get({}, function(config) {
    $scope.paypalConfig = config;

    $scope.paypal = {
      quantity: 0,
      amount: $scope.paypalConfig.costPerUser,
      custom: {
        payingUser: $scope.userId,
        paidFor: []
      }
    };

    $scope.$watch('user.selected', function(selected) {
      console.log('users changed');
      $scope.paypal.custom.paidFor = [];
      angular.forEach(selected, function(user) {
        $scope.paypal.custom.paidFor.push(user.id);
      });
      $scope.paypal.quantity = selected.length;
    });

    restService.users().all().$promise.then(function(users) {
      $scope.availableUsers = users;
    });
  });

  $scope.displayName = function (user, search) {
    var result='';
    if (typeof user === 'string') {
      result =  user;
    } else {
      if (user.realName) {
        result = user.realName + ' (' + user.username + ')';
      } else {
        result = user.username;
      }
    }
    if (search) {
      return $filter('highlight')(result, search);
    } else {
      return result;
    }
  };

  $scope.itemName = function (user) {
    
    if (user.id === $scope.userId) {
      return 'yourself';
    }
    
    return user.realName ? user.realName : user.username;
  };

  $scope.tooltip = function(user) {
    return (user.realName ? '<div>real name: ' + user.realName + '</div>' : '') + '<div>username: ' + user.username + '</div>';
  };

  $scope.selectedUserString = function() {

    var users = $scope.user.selected;
    var length = users.length;
    if (length === 1) {
      return users[0].username;
    }

    var result = '';
    if (length > 2) {
      angular.forEach(users.slice(0, length-2), function(user) {
        result += user.username + ', ';
      });
    }
    result += users[length-2].username + ' and ' + users[length-1].username;
    return result;
  };
});