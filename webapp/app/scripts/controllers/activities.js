'use strict';

angular.module('hhupApp').controller('ActivitiesCtrl', function ($scope, $location, $filter, restService, formatService, authenticationService) {

  $scope.markerControl = {};

  $scope.loggedIn = authenticationService.user() ? true : false;

  if ($scope.loggedIn) {
    $scope.admin = authenticationService.isAdmin();
  }

  restService.activities().all().$promise.then(function(activities) {
    $scope.locations = [];
    $scope.activitiesByDate = {};
    var idCounter = 1;
    angular.forEach(activities, function(activity) {
      activity.timeString = $filter('date')(activity.startDate, 'HH:mm') + ' - ' + $filter('date')(activity.endDate, 'HH:mm');
      activity.location.id = idCounter;
      $scope.markerControl[idCounter] = {};
      $scope.locations.push(activity.location);

      // locate the 'leading' image
      if (activity.images && activity.images.length > 0) {
        for (var i in activity.images) {
          if (activity.images[i].leading) {
            activity.leadingImage = activity.images[i].file;
          }
        }
      }

      // group the activities by their day
      var dateGroupString = $filter('date')(activity.startDate, 'yyyy-MM-dd');
      if (!$scope.activitiesByDate[dateGroupString]) {
        $scope.activitiesByDate[dateGroupString] = [];
      }
      $scope.activitiesByDate[dateGroupString].push(activity);

      idCounter ++;
    });
  });

  $scope.orderDate = function(activity) {
    return new Date(activity.startDate);
  };

  $scope.map = {
    center: {
      latitude: 53.55,
      longitude: 10
    },
    zoom: 13,
    control: {}
  };

  // make marker bounce when the mouse hovers over the activity tile
  $scope.bounce = function(id, bounce) {
    var marker = $scope.markerControl[id].getGMarkers()[0];
    if (bounce) {
      /* jshint ignore:start */
      marker.setAnimation(google.maps.Animation.BOUNCE);
      // re-center the map
      $scope.map.control.getGMap().panTo(marker.getPosition());
      /* jshint ignore:end */
    } else {
      marker.setAnimation(null);
    }
  };

  // highlight activity tile when mouse hovers over map marker
  $scope.markerEvents = {
    mouseover: function (event) { highlight(event.key, true); },
    mouseout: function (event) { highlight(event.key, false); }
  };

  $scope.showActivity = function(id) {
    $location.path('/activities/' + id);
  };

  var highlight = function(id, highlight) {
    for (var dateIndex in $scope.activitiesByDate) {
      var date = $scope.activitiesByDate[dateIndex];
      for (var activityIndex in date) {
        var activity = date[activityIndex];
        if (activity.location) {
          if (activity.location.id === id) {
            // mark / unmark this activity
            activity.highlight = highlight;
            break;
          }
        }
      }
    }
    $scope.$apply();
  };
  
  $scope.fullWidth = function(activities, activity) {
    if (activities.length < 2 || activity.fullSize) {
      return '';
    } else {
      return 'col-sm-6';
    }
  };
});