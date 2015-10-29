'use strict';

angular.module('hhupApp').controller('SignupCtrl',
  function($scope, $document, $location, $filter, $timeout, modalService, nationalitiesService, validationService, restService) {

    $scope.emptyUser = {
      username: '',
      password: '',
      email: '',
      realName: '',
      nationality: '',
      home: '',
      csProfile: '',
      hcProfile: '',
      bwProfile: '',
      fbProfile: '',
      languages: [],
      phone: ''
    };

    $scope.nationalities = {
        available : $filter('orderBy')(nationalitiesService.nationalities, 'natn'),
        selected: undefined
    };
    $scope.languages = {
        available : $filter('orderBy')(nationalitiesService.nationalities, 'natn'),
        selected: []
    };

    // validations
    var checkComplete = function() {
      if ($scope.user.username === '' ||
        $scope.invalid.username !== false ||
        $scope.user.password === '' ||
        $scope.invalid.password !== false ||
        $scope.invalid.passwordConfirm !== false ||
        $scope.user.email === '' ||
        $scope.invalid.email !== false) {

        $scope.submitDisable = true;
      } else {
        $scope.submitDisable = false;
      }
    };

    $scope.invalid = {};

    var delayedTakenCheck = null;
    var checkUsernameTaken = function() {
      restService.users().nameTaken({name: $scope.user.username}).$promise.then(function(taken) {
        if (taken[0] === true) {
          $scope.invalid.username = 'sorry, that name is taken already';
          delayedTakenCheck = null;
          checkComplete();
        }
      });
    };

    $scope.$watch('user.username', function(newUsername) {
      $scope.invalid.username = validationService.isInvalidUsername(newUsername);

      if (newUsername.length > 2) {
        if (delayedTakenCheck !== null) {
          $timeout.cancel(delayedTakenCheck);
        }
        delayedTakenCheck = $timeout(checkUsernameTaken, 250);
      }
      
      checkComplete();
    });

    $scope.$watch('user.password', function(newPassword) {
      $scope.invalid.password = validationService.isInvalidPassword(newPassword);
      checkComplete();
    });

    $scope.$watch('passwordConfirm', function(newPwConfirm) {
      $scope.invalid.passwordConfirm = validationService.isInvalidPasswordConfirm($scope.user.password, newPwConfirm);
      checkComplete();
    });

    $scope.$watch('user.email', function(newEmail) {
      $scope.invalid.email = validationService.isInvalidEmail(newEmail);
      checkComplete();
    });

    $scope.$watch('languages.selected', function(langs) {
      $scope.user.languages = [];
      angular.forEach(langs, function(language) {
        $scope.user.languages.push(language.code);
      });
    });

    $scope.submit = function() {

      $scope.submitting = true;
      $scope.disable = true;
      var user = {};

      // username
      user.username = $scope.user.username;

      // password
      user.password = $scope.user.password;

      // email
      user.email = $scope.user.email;

      // hometown
      if ($scope.home !== '') {
        if (typeof $scope.home === 'object') {
          var placeIdField = 'place_id';
          var addressField = 'adr_address';
          user.homeId = {
              id: $scope.home[placeIdField],
              readable: $scope.home[addressField]
          };
        } else if (typeof $scope.home === 'string') {
          user.homeString = $scope.home;
        }
      }

      // real name
      if ($scope.user.realName !== '') {
        user.realName = $scope.user.realName;
      }

      // nationality
      if ($scope.nationalities.selected) {
        user.nationality = $scope.nationalities.selected.code;
      }

      // CS profile
      if ($scope.user.csProfile !== '') {
        user.csProfile = $scope.user.csProfile;
      }

      // HC profile
      if ($scope.user.hcProfile !== '') {
        user.hcProfile = $scope.user.hcProfile;
      }

      // BW profile
      if ($scope.user.bwProfile !== '') {
        user.bwProfile = $scope.user.bwProfile;
      }

      // FB profile
      if ($scope.user.fbProfile !== '') {
        user.fbProfile = $scope.user.fbProfile;
      }

      // phone
      if ($scope.user.phone !== '') {
        user.phone = $scope.user.phone;
      }

      // languages
      if ($scope.user.languages.length > 0) {
        user.languages = $scope.user.languages;
      }

      // send to rest interface
      restService.users().save(user).$promise.then(function() {
        console.log('submit request returned');
        $scope.submitting = false;
        var modalInstance = modalService.message('please confirm your registration', 'Your registration was successful. Please go to your eMail account and confirm it by clicking the link that we just sent you.');
        modalInstance.result.then(function() {
          $location.path('/');
        });
      }, function(error) {
        console.log(error);
        if (error.status === 304) {
          modalService.error('error', 'the username is already taken, please choose a different one');
        }
        $scope.submitting = false;
        $scope.disable = false;
      });
    };

    $scope.reset = function() {
      $scope.user = JSON.parse(JSON.stringify($scope.emptyUser));
      $scope.passwordConfirm = '';
      $scope.nationality = '';
      $scope.home = '';
      $scope.submitDisable = true;
    };

    $scope.manualReset = function() {
      var modalInstance = modalService.question('reset', 'do you really want to reset all fields?');
      modalInstance.result.then(function(reset) {
        if (reset) {
          $scope.reset();
        }
      });
    };

    $scope.displayName = function (language, search) {
      if (search) {
        return $filter('highlight')(language.natn, search);
      } else {
        return language.natn;
      }
    };

    $scope.reset();
  });