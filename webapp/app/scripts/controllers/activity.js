'use strict';

angular.module('hhupApp').controller('ActivityCtrl', function ($scope, $routeParams, $filter, $location, restService, modalService, authenticationService, formatService) {

  $scope.participants = [];
  $scope.contacts = [];

  $scope.loggedIn = authenticationService.user() ? true : false;

  if ($scope.loggedIn) {
    $scope.admin = authenticationService.isAdmin();
  }

  restService.activities().get({activityId: $routeParams.activityId}).$promise.then(function(activity) {
    $scope.activity = activity;

    while (activity.description.indexOf('\n') > -1) {
      activity.description = activity.description.replace('\n', '<br>');
    }

    $scope.map.center = {
        longitude: activity.location.longitude,
        latitude: activity.location.latitude
    };

    $scope.timeString = formatService.startEndDateString(activity.startDate, activity.endDate);
    $scope.limitationString = activity.limited ? '(max. ' + activity.limit + ')' : '(unlimited)';
    $scope.limitReached = activity.limited ? activity.participants.length >= activity.limit : false;

    if ($scope.loggedIn) {
      angular.forEach(activity.contact, function(contactId) {
        restService.users().profile({userId: contactId}).$promise.then(function(contact) {
          $scope.contacts.push(contact);
        });
      });
    }

    $scope.joined = false;

    if ($scope.loggedIn) {
      angular.forEach(activity.participants, function(participantId) {
        restService.users().profile({userId: participantId}).$promise.then(function(participant) {
          $scope.participants.push(participant);
          if (participant.id === authenticationService.userId()) {
            $scope.joined = true;
          }
        });
      });
    }
  });

  restService.activities().conflicts({activityId: $routeParams.activityId}).$promise.then(function(conflicts) {
    $scope.conflicts = conflicts;
  });

  $scope.map = {
    center: {
      latitude: 53.55,
      longitude: 10
    },
    zoom: 12
  };
  
  $scope.name = function(user) {
    if (user.realName) {
      return user.realName + ' (' + user.username + ')';
    } else {
      return user.username;
    }
  };
  
  $scope.profile = function(user) {
    $location.path('/users/' + user.id);
  };

  $scope.joinActivity = function() {
    if ($scope.conflicts.length > 1 || ($scope.conflicts.length === 1 && $scope.conflicts[0].id !== $scope.activity.id)) {
      var message = 'you can not join this activity, because the following activities you joined take place at the same time: <ul>';
      angular.forEach($scope.conflicts, function(conflict) {message += '<li>' + conflict.title + '</li>';});
      message += '</ul>';
      modalService.error('conflict', message);
      return;
    }
    $scope.joining = true;
    var params = {id: $scope.activity.id};
    restService.activities().join(params).$promise.then(function() {
      $scope.participants.push(authenticationService.user());
      $scope.joined = true;
      $scope.joining = false;
    });
  };

  $scope.leaveActivity = function() {
    $scope.joining = true;
    var params = {id: $scope.activity.id};
    restService.activities().leave(params, function() {
      for (var index in $scope.participants) {
        if ($scope.participants[index].id === authenticationService.userId()) {
          $scope.participants.splice(index, 1);
          break;
        }
      }
      $scope.joined = false;
      $scope.joining = false;
    });
  };

  // admin stuff
  if ($scope.admin) {
    $scope.removeActivity = function() {
      var modal = modalService.question('delete activity', 'do you really want to delete this activity? The participation list will be gone as well. This cannot be undone.');
      modal.result.then(function(remove) {
        if (remove) {
          restService.activities().remove({id: $scope.activity.id}, function() {
            $location.path('/activities');
          });
        }
      });
    };

    $scope.editActivity = function() {
      $location.path('/createActivity').search({id: $scope.activity.id});
    };

    $scope.sendMassMail = function() {
      var modal = modalService.input('send mass mail', [{
            caption: 'subject',
            name:'subject',
            type: 'text'
          }, {
            caption: 'message',
            name:'text',
            type: 'textarea'
        }
      ]);

      modal.result.then(function(mail) {
        mail.activityId = $routeParams.activityId;
        restService.massMail().send(mail, function() {
          modalService.message('ok', 'your message was sent to all participants of this activity');
        });
      });
    };
  }
});