'use strict';

angular.module('hhupApp').controller('UsersCtrl', function(authenticationService, $filter, $scope, $location, restService, modalService) {

  $scope.isLoggedIn = authenticationService.isLoggedIn();
  $scope.admin = authenticationService.isAdmin();
  $scope.search = '';

  $scope.isAdmin = function(user) {
    return user.authorities.indexOf('ADMIN') >= 0;
  };

  if ($scope.admin) {
    restService.notes().usersWithNotes({}, function(ids) {
      $scope.usersWithNotes = ids;
    });
  } else {
    $scope.userWithNotes = [];
  }

  $scope.hasNote = function(user) {
    return $scope.usersWithNotes.indexOf(user.id) >= 0;
  };

  restService.users().all().$promise.then(function(users) {
    $scope.users = users;

    if (!$scope.admin) {
      $scope.users = $filter('filter')(users, {activated:true});
    }
  });

  $scope.profile = function(user) {
    if ($scope.isLoggedIn) {
      $location.path('/users/' + user.id);
    } else {
      modalService.message('please log in', 'please log in to view profiles');
    }
  };

  $scope.strip = function(html) {
    return html.replace(/<(?:.|\n)*?>/gm, '');
  };

  $scope.filterUsers = function(user) {
    var usernameHit = user.username.toLowerCase().indexOf($scope.search.toLowerCase()) > -1;
    var realNameHit = user.realName && user.realName.toLowerCase().indexOf($scope.search.toLowerCase()) > -1;
    return usernameHit || realNameHit;
  };

  $scope.unMarkPaid = function(user) {
    var modal = modalService.question('UN-mark paid', 'do you really want to UN-mark ' + user.username + ' as paid (set status to NOT paid)?');
    modal.result.then(function(doUnmark) {
      if (doUnmark) {
        restService.admin().confirmPayment({userId: user.id, paid: false}).$promise.then(function() {
          user.paid = false;
        });
      }
    });
  };

  $scope.markPaid = function(user) {
    var modal = modalService.question('mark paid', 'do you want to mark ' + user.username + ' as paid?');
    modal.result.then(function(doMark) {
      if (doMark) {
        restService.admin().confirmPayment({userId: user.id, paid: true}).$promise.then(function() {
          user.paid = true;
        });
      }
    });
  };

  $scope.checkin = function(user) {
    if (!user.paid) {
      modalService.error('not paid', user.username + ' cannot be checked in because they are not marked as paid. please confirm the payment first!');
    } else {
      var modal = modalService.question('check in', 'do you want to check ' + user.username + ' in?');
      modal.result.then(function(doCheckin) {
        if (doCheckin) {
          restService.admin().checkin({userId: user.id, checkin: true}).$promise.then(function() {
            user.checkedIn = true;
          });
        }
      });
    }
  };

  $scope.unCheckin = function(user) {
    var modal = modalService.question('UN-check in ', 'do you really want to UN-mark ' + user.username + ' as checked in (set status to NOT checked in)?');
    modal.result.then(function(doUnCheckin) {
      if (doUnCheckin) {
        restService.admin().checkin({userId: user.id, checkin: false}).$promise.then(function() {
          user.checkedIn = false;
        });
      }
    });
  };

  $scope.sort = function(sorting) {
    $scope.sorting = sorting;
    if ($scope.users) {
      switch (sorting) {
        case 'username-asc' : $scope.users = $filter('orderBy')($scope.users, 'username', false); break;
        case 'username-desc' : $scope.users = $filter('orderBy')($scope.users, 'username', true); break;
        case 'realName-asc' : $scope.users = $filter('orderBy')($scope.users, 'realName', false); break;
        case 'realName-desc' : $scope.users = $filter('orderBy')($scope.users, 'realName', true); break;
        case 'paid-asc' : $scope.users = $filter('orderBy')($scope.users, 'paid', false); break;
        case 'paid-desc' : $scope.users = $filter('orderBy')($scope.users, 'paid', true); break;
        case 'checkedIn-asc' : $scope.users = $filter('orderBy')($scope.users, 'checkedIn', false); break;
        case 'checkedIn-desc' : $scope.users = $filter('orderBy')($scope.users, 'checkedIn', true); break;
      }
    }
  };

  $scope.total = function() {
    return $scope.users ? $scope.users.length : 0;
  };

  $scope.paid = function() {
    return $scope.users ? $filter('filter')($scope.users, {paid:true}).length : 0;
  };
});
