'use strict';

angular.module('hhupApp').controller('CreateActivityCtrl', function ($scope, $filter, $location, $routeParams, restService, modalService, authenticationService) {

  if (!authenticationService.isAdmin()) {
    $location.path('/');
  }

  $scope.title = '';
  $scope.description = '';
  $scope.limit = -1;
  $scope.limited = false;
  $scope.startDate = '';
  $scope.endDate = '';
  $scope.location = undefined;
  $scope.contact = [];
  $scope.images = [];
  $scope.fullSize = false;
  $scope.canCollide = false;

  $scope.pageTitle = 'create a new activity';
  $scope.editing = false;

  $scope.submitting = false;
  $scope.valid = false;

  $scope.markerOptions = {
    /* jshint ignore:start */
    animation : google.maps.Animation.DROP
    /* jshint ignore:end */
  };

  $scope.map = {
      center: {
        latitude: 53.55,
        longitude: 10
      },
      zoom: 12,
      events: {
        click: function (mapModel, eventName, originalEventArgs) {
          // 'this' is the directive's scope

          var e = originalEventArgs[0];
          var lat = e.latLng.lat(),
              lon = e.latLng.lng();
          $scope.marker = {
            id: 0,
            coords: {
              latitude: lat,
              longitude: lon
            }
          };
          $scope.location = {
            latitude: lat,
            longitude: lon
          };
          //scope apply required because this event handler is outside of the angular domain
          $scope.$apply();
        },
      }
  };

  $scope.marker = undefined;

  $scope.$watch('startDate', function(date) {
    if (date.getMonth) {
      $scope.startDateString = $filter('date')(date, 'EEEE dd.MM.yy HH:mm');
    } else {
      $scope.startDateString = undefined;
    }
  });

  $scope.$watch('limit', function(limit) {
    if (isNaN(limit) || limit < 1) {
      $scope.limited = false;
    } else {
      $scope.limited = true;
    }
  });

  $scope.$watch('limited', function(limited) {
    if (!limited) {
      $scope.limit = undefined;
    } else {
      if (isNaN($scope.limit) || $scope.limit < 1) {
        $scope.limit = 1;
      }
    }
  });

  $scope.$watch('endDate', function(date) {
    if (date.getMonth) {
      $scope.endDateString = $filter('date')(date, 'EEEE dd.MM.yy HH:mm');
    } else {
      $scope.endDateString = undefined;
    }
  });

  $scope.availableUsers = [];
  
  $scope.user.selected = [];
  $scope.$watch('user.selected', function(selected) {
    $scope.contact = [];
    angular.forEach(selected, function(user) {
      $scope.contact.push(user.id);
    });
  });

  restService.users().all().$promise.then(function(users) {
    $scope.availableUsers = users;
    var contactUsers = [];
    angular.forEach($scope.contact, function(contact) {
      angular.forEach($scope.availableUsers, function(user) {
        if (user.id === contact) {
          contactUsers.push(user);
          return;
        }
      });
    });
    $scope.user.selected = contactUsers;
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
    return user.realName ? user.realName : user.username;
  };

  $scope.tooltip = function(user) {
    return (user.realName ? '<div>real name: ' + user.realName + '</div>' : '') + '<div>username: ' + user.username + '</div>';
  };

  $scope.cancel = function() {
    $location.path('/activities');
  };

  $scope.send = function() {
    $scope.submitting = true;
    var activity = {
      title: $scope.title,
      description: $scope.description,
      limited: $scope.limited,
      startDate: $scope.startDate,
      endDate: $scope.endDate,
      location: $scope.location,
      contact: $scope.contact,
      images: $scope.images,
      fullSize: $scope.fullSize,
      canCollide: $scope.canCollide
    };
    if ($scope.limited) {
      activity.limit = $scope.limit;
    }
    var promise;
    if ($scope.editing) {
      activity.id = $scope.id;
      activity.participants = $scope.participants;
      promise = restService.activities().edit(activity).$promise;
    } else {
      promise = restService.activities().save(activity).$promise;
    }
    promise.then(function() {
      $scope.submitting = false;
      var modal = modalService.message('activity saved', 'the activity data has been saved successfully');
      modal.result.then(function() {
        $location.path('/activities');
      });
    }, function() {
      $scope.submitting = false;
      modalService.error('activity not saved', 'somthing went wrong while saving the activity data');
    });
  };
  
  $scope.imageUploadStarted = function() {
    $scope.submitting = true;
  };
  
  $scope.imageUploadSuccess = function(response) {
    $scope.submitting = false;
    var file = response.headers('imageFile');
    $scope.images.push({file: file, leading: false});
    if ($scope.images.length === 1) {
      $scope.makeLeading(file);
    }
  };
  
  $scope.imageUploadError = function(response) {
    $scope.submitting = false;
    modalService.error('image upload failed', JSON.stringify(response));
  };
  
  $scope.makeLeading = function(filename) {
    angular.forEach($scope.images, function(image) {
      image.leading = image.file === filename;
    });
  };
  
  $scope.removeImage = function(image) {
    var index = $scope.images.indexOf(image);
    if (index > -1) {
      $scope.images.splice(index, 1);
    }
    
    if (image.leading && $scope.images.length > 0) {
      $scope.makeLeading($scope.images[0].file);
    }
  };

  $scope.$watch('title', function() {
    $scope.valid = validate();
  });

  $scope.$watch('description', function() {
    $scope.valid = validate();
  });

  $scope.$watch('startDateString', function() {
    $scope.valid = validate();
  });

  $scope.$watch('endDateString', function() {
    $scope.valid = validate();
  });

  $scope.$watch('location', function() {
    $scope.valid = validate();
  });

  $scope.$watch('contact', function() {
    $scope.valid = validate();
  });

  var validate = function() {
    if ($scope.title.length === 0) {
      return false;
    }
    if ($scope.description.length === 0) {
      return false;
    }
    if (!$scope.startDateString || $scope.startDateString.length === 0) {
      return false;
    }
    if (!$scope.endDateString || $scope.endDateString.length === 0) {
      return false;
    }
    if (!$scope.location) {
      return false;
    }
    return true;
  };

  if ($routeParams.id) {
    restService.activities().get({activityId: $routeParams.id}, function(activity) {
      $scope.pageTitle = 'edit activity';
      $scope.editing = true;
      $scope.title = activity.title;
      $scope.description = activity.description;
      $scope.startDate = new Date(activity.startDate);
      $scope.endDate = new Date(activity.endDate);
      $scope.location = activity.location;
      $scope.fullSize = activity.fullSize;
      $scope.canCollide = activity.canCollide;
      $scope.marker = {
          id: 0,
          coords: activity.location
        };
      if (activity.limited) {
        $scope.limit = activity.limit;
        $scope.limited = true;
      }
      var contactUsers = [];
      angular.forEach(activity.contact, function(contact) {
        angular.forEach($scope.availableUsers, function(user) {
          if (user.id === contact) {
            contactUsers.push(user);
            return;
          }
        });
      });
      $scope.user.selected = contactUsers;
      $scope.id = activity.id;
      $scope.participants = activity.participants;
      $scope.images = activity.images ? activity.images : [];
    });
  }
});