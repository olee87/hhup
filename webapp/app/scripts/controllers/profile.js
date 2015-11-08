'use strict';

angular.module('hhupApp').controller('ProfileCtrl', function($scope, $routeParams, $route,  $location, restService, nationalitiesService, authenticationService, modalService) {
  $scope.strip = function(html) {
    return html.replace(/<(?:.|\n)*?>/gm, '');
  };

  authenticationService.checkLoggedIn();

  $scope.admin = authenticationService.isAdmin();

  restService.users().profile({userId: $routeParams.userId}).$promise.then(function(user) {
    $scope.user = user;
    $scope.isOwnProfile = (user.id === authenticationService.userId());
    if (user.nationality) {
      $scope.nationality = nationalitiesService.getForCode(user.nationality);
    }
    $scope.userIsAdmin = user.authorities.indexOf('ADMIN') >= 0;
    $scope.languages = user.languages ? user.languages.map(function(code) {return nationalitiesService.getForCode(code);}) : [];
  });

  restService.activities().forUser({userId: $routeParams.userId}).$promise.then(function(activities) {
    $scope.activities = activities;
  });

  if ($scope.admin) {

    var reloadNotes = function() {
      restService.notes().forUser({userId: $routeParams.userId}, function(notes) {
        $scope.authors = {};
        $scope.notes = notes;
        angular.forEach(notes, function(note) {
          restService.users().profile({userId: note.authorId}, function(author) {
            $scope.authors[note.authorId] = author.username;
          });
        });
      });
    };

    reloadNotes();

    restService.history().get({userId: $routeParams.userId, maxItems: 100}, function(userHistory) {
      $scope.userHistory = userHistory;
    });

    $scope.createNote = function () {
      var modal = modalService.singleTextInput('create a note', 'enter your note here');

      modal.result.then(function (note) {
        // create the written note through rest
        var request = {userId: $routeParams.userId, text: note.result};
        restService.notes().create(request, function() {
          // reload the notes for this user
          reloadNotes();
        });
      });
    };

    $scope.removeNote = function(noteId) {
      var modal = modalService.question('delete?', 'do you really want to delete this note?');
      modal.result.then(function(remove) {
        if (remove) {
          restService.notes().remove({noteId: noteId}, function() {
            reloadNotes();
          });
        }
      });
    };

    $scope.activate = function(active) {
      console.log('setting active: ' + active);
      var titleQuestion, bodyQuestion, bodyAnswer;
      if (active) {
        titleQuestion = 'activate user';
        bodyQuestion = 'do you really want to set ' + $scope.user.username + '\' status to active (bypass activation process)?';
        bodyAnswer = $scope.user.username + ' is now activated';
      } else {
        titleQuestion = 'deactivate user';
        bodyQuestion = 'do you really want to set ' + $scope.user.username + ' inactive? They will not be able to login anymore!';
        bodyAnswer = $scope.user.username + ' is now deactivated';
      }
      var modal = modalService.question(titleQuestion, bodyQuestion);
      modal.result.then(function(doActivate) {
        if (doActivate) {
          restService.admin().setActive({userId: $scope.user.id, active: active}, function() {
            var modal1 = modalService.message('success', bodyAnswer);
            modal1.result.then(function() {
              $route.reload();
            });
          });
        }
      });
    };

    $scope.paid = function(paid) {
      console.log('setting paid: ' + paid);
      var titleQuestion, bodyQuestion, bodyAnswer;
      if (paid) {
        titleQuestion = 'confirm payment';
        bodyQuestion = 'do you really want to confirm the payment for ' + $scope.user.username + '?';
        bodyAnswer = $scope.user.username + ' is now marked as paid';
      } else {
        titleQuestion = 'remove payment confirmation';
        bodyQuestion = 'do you really want to remove the payment confirmation for ' + $scope.user.username + '?';
        bodyAnswer = $scope.user.username + ' is no longer marked as paid';
      }
      var modal = modalService.question(titleQuestion, bodyQuestion);
      modal.result.then(function(doConfirmPayment) {
        if (doConfirmPayment) {
          restService.admin().confirmPayment({userId: $scope.user.id, paid: paid}, function() {
            var modal1 = modalService.message('success', bodyAnswer);
            modal1.result.then(function() {
              $route.reload();
            });
          });
        }
      });
    };

    $scope.makeAdmin = function(admin) {
      console.log('making admin: ' + admin);
      var titleQuestion, bodyQuestion, bodyAnswer;
      if (admin) {
        titleQuestion = 'make admin';
        bodyQuestion = 'do you really want to make ' + $scope.user.username + ' an admin?';
        bodyAnswer = $scope.user.username + ' is now an admin';
      } else {
        titleQuestion = 'unmake admin';
        bodyQuestion = 'do you really want to take admin privileges from ' + $scope.user.username + '?';
        bodyAnswer = $scope.user.username + ' is no longer an admin';
      }
      var modal = modalService.question(titleQuestion, bodyQuestion);
      modal.result.then(function(doMakeAdmin) {
        if (doMakeAdmin) {
          restService.admin().makeAdmin({userId: $scope.user.id, makeAdmin: admin}, function() {
            var modal1 = modalService.message('success', bodyAnswer);
            modal1.result.then(function() {
              $route.reload();
            });
          });
        }
      });
    };
    
    $scope.deleteUser = function() {
      var titleQuestion = 'delete ' + $scope.user.username;
      var bodyQuestion = 'Do you really want to delete ' + $scope.user.username + '\'s account? This is <b>irreversible</b>!';
      var bodyAnswer = $scope.user.username + ' has been deleted';
      var modal = modalService.question(titleQuestion, bodyQuestion);
      modal.result.then(function(doMakeAdmin) {
        if (doMakeAdmin) {
          console.log('deleting ' + $scope.user.username);
          restService.admin().deleteUser({userId: $scope.user.id}, function() {
            var modal1 = modalService.message('success', bodyAnswer);
            modal1.result.then(function() {
              $route.reload();
            });
          });
        }
      });
    };
  }

  // actions menu stuff
  $scope.status = {
    isopen: false
  };

  $scope.toggleDropdown = function($event) {
    $event.preventDefault();
    $event.stopPropagation();
    $scope.status.isopen = !$scope.status.isopen;
  };

  $scope.setPassword = function() {
    modalService.message('nope', 'this is not yet implemented');
  };

  $scope.editProfile = function() {
    $location.path('/myProfile/edit');
  };

  $scope.pay = function() {
    modalService.message('nope', 'this is not yet implemented');
  };

  $scope.orderDate = function(activity) {
    return new Date(activity.startDate);
  };
});