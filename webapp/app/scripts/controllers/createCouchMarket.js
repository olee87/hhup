'use strict';

angular.module('hhupApp').controller('CreateCouchMarketPostCtrl', function ($scope, $routeParams, $location, restService, modalService, authenticationService) {

  authenticationService.checkLoggedIn();

  $scope.post = {
    text: '',
    type: '',
    count: -1
  };

  if ($routeParams.id) {
    restService.couchMarket().post({id: $routeParams.id}).$promise.then(function(post) {
      $scope.post = post;
    });
  }

  $scope.send = function() {
    if ($scope.post.id) {
      // editing existing
      restService.couchMarket().edit($scope.post).$promise.then(function() {
        success();
      });
    } else {
      // creating new
      restService.couchMarket().create($scope.post).$promise.then(function() {
        success();
      });
    }
  };

  var success = function() {
    var modal = modalService.message('thanks!', 'your post has been saved');
    modal.result.then(function() {
      $location.path('/couchmarket');
    });
  };
});