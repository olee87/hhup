'use strict';

angular.module('hhupApp').controller('GuestbookCtrl', function ($scope, $location, restService, authenticationService, modalService) {

  authenticationService.checkLoggedIn();

  var admin = authenticationService.isAdmin();

  $scope.newMessage = '';

  var reload = function() {
    restService.guestbook().all().$promise.then(function(posts) {
      $scope.posts = posts;
      console.log(JSON.stringify(posts));
    });
  };

  restService.users().all().$promise.then(function(users) {
    $scope.users = {};
    angular.forEach(users,function(user) {
      $scope.users[user.id] = user;
    });
  });

  $scope.createPost = function() {
    if ($scope.newMessage !== '') {
      restService.guestbook().create({message: $scope.newMessage}).$promise.then(function() {
        $scope.newMessage = '';
        reload();
      });
    }
  };

  $scope.deletePost = function(id) {
    var modal = modalService.question('delete?','do you really want to delete this post?');
    modal.result.then(function(remove) {
      if (remove) {
        restService.guestbook().remove({id: id}, function() {
          modalService.message('done', 'that post has been deleted');
          reload();
        });
      }
    });
  };

  $scope.mayDelete = function(post) {
    var own = authenticationService.userId() === post.authorId;
    return admin || own;
  };

  $scope.profile = function(userId) {
    $location.path('/users/' + userId);
  };

  reload();
});