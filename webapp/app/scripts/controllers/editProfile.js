'use strict';

angular.module('hhupApp').controller('EditProfileCtrl', function($filter, $scope, $location, authenticationService, nationalitiesService, restService, modalService, formatService) {
  $scope.user = authenticationService.user();

  authenticationService.checkLoggedIn();

  $scope.changedUser = {
    csProfile: $scope.user.csProfile,
    hcProfile: $scope.user.hcProfile,
    bwProfile: $scope.user.bwProfile,
    fbProfile: $scope.user.fbProfile,
    phone: $scope.user.phone,
    realName: $scope.user.realName
  };

  $scope.nationalities = {
      available: nationalitiesService.nationalities,
      selected: nationalitiesService.getForCode($scope.user.nationality)
  };

  $scope.languages = {
      available: nationalitiesService.nationalities,
      selected: $scope.user.languages ? $scope.user.languages.map(function(code) {return nationalitiesService.getForCode(code);}) : []
  };

  $scope.home = $scope.user.homeId ? formatService.stripHtml($scope.user.homeId.readable) : $scope.user.homeString;

  $scope.$watch('home', function() {
    $scope.homeDirty = true;
  });

  $scope.send = function() {
    $scope.submitting = true;
    // hometown
    if ($scope.home !== '' && $scope.homeDirty) {
      if (typeof $scope.home === 'object') {
        var placeIdField = 'place_id';
        var addressField = 'adr_address';
        $scope.changedUser.homeId = {
            id: $scope.home[placeIdField],
            readable: $scope.home[addressField]
        };
      } else if (typeof $scope.home === 'string') {
        $scope.changedUser.homeString = $scope.home;
      }
    }

    // nationality
    if ($scope.nationalities.selected) {
      $scope.changedUser.nationality = $scope.nationalities.selected.code;
    }

    // languages
    $scope.changedUser.languages = $scope.languages.selected.map(function(language) { return language.code; });

    $scope.changedUser.userId = $scope.user.userId;

    restService.users().edit($scope.changedUser).$promise.then(function() {
      $scope.submitting = false;
      authenticationService.updateUser($scope.changedUser);
      var modalInstance = modalService.message('success', 'your information has been saved successfully');
      modalInstance.result.then(function() {
        $location.path('/#/myProfile');
      });
    },
    function() {
      $scope.submitting = false;
    });

  };

  $scope.displayName = function (language, search) {
    if (search) {
      return $filter('highlight')(language.natn, search);
    } else {
      return language.natn;
    }
  };

  $scope.cancel = function() {
    $location.path('/#/myProfile');
  };
});