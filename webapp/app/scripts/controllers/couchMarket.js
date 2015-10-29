'use strict';

angular.module('hhupApp').controller('CouchMarketCtrl', function($scope, $location, $route, restService, authenticationService, modalService) {

  authenticationService.checkLoggedIn();
  
  $scope.admin = authenticationService.isAdmin();

  restService.couchMarket().all().$promise.then(function(posts) {
    $scope.posts = posts;
  });

  $scope.showType = '';

  restService.users().all().$promise.then(function(users) {
    $scope.users = {};
    angular.forEach(users,function(user) {
      $scope.users[user.id] = user;
    });
  });

  $scope.createPost = function() {
    $location.path('/couchmarket/create');
  };

  $scope.edit = function(post) {
    $location.path('/couchmarket/create').search({id: post.id});
  };

  $scope.deletePost = function(post) {
    var modal = modalService.question('delete?','do you really want to delete this post?');
    modal.result.then(function(remove) {
      if (remove) {
        restService.couchMarket().remove({id: post.id}).$promise.then(function() {
          $route.reload();
        });
      }
    });
  };

  $scope.own = function(post) {
    return authenticationService.userId() === post.userId;
  };
  
  $scope.profile = function(userId) {
    $location.path('/users/' + userId);
  };
  
  $scope.lowercase = function(string) {
    return string.toLowerCase();
  };
});