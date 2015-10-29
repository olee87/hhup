'use strict';

angular.module('hhupApp', [
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.bootstrap',
    'angular-ladda',
    'ui.select',
    'ui.bootstrap.datetimepicker',
    'google.places',
    'uiGmapgoogle-maps',
    'lr.upload',
    'nvd3ChartDirectives'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl'
      })
      .when('/signup', {
        templateUrl: 'views/signup.html',
        controller: 'SignupCtrl'
      })
      .when('/users', {
        templateUrl: 'views/users.html',
        controller: 'UsersCtrl'
      })
      .when('/users/:userId', {
        templateUrl: 'views/profile.html',
        controller: 'ProfileCtrl'
      })
      .when('/recoverPassword', {
        templateUrl: 'views/recoverPassword.html',
        controller: 'RecoverPasswordCtrl'
      })
      .when('/myProfile/edit', {
        templateUrl: 'views/editProfile.html',
        controller: 'EditProfileCtrl'
      })
      .when('/activities', {
        templateUrl: 'views/activities.html',
        controller: 'ActivitiesCtrl'
      })
      .when('/activities/:activityId', {
        templateUrl: 'views/activity.html',
        controller: 'ActivityCtrl'
      })
      .when('/createActivity', {
        templateUrl: 'views/createActivity.html',
        controller: 'CreateActivityCtrl'
      })
      .when('/confirm/:code', {
        templateUrl: 'views/confirmUser.html',
        controller: 'ConfirmUserCtrl'
      })
      .when('/recoverPassword/:code', {
        templateUrl: 'views/confirmRecoverPassword.html',
        controller: 'ConfirmRecoverPasswordCtrl'
      })
      .when('/couchmarket', {
        templateUrl: 'views/couchmarket.html',
        controller: 'CouchMarketCtrl'
      })
      .when('/couchmarket/create', {
        templateUrl: 'views/createCouchmarketPost.html',
        controller: 'CreateCouchMarketPostCtrl'
      })
      .when('/pay', {
        templateUrl: 'views/payClosed.html'
        // controller: 'PayCtrl'
      })
      .when('/guestbook', {
        templateUrl: 'views/guestbook.html',
        controller: 'GuestbookCtrl'
      })
      .when('/admin', {
        templateUrl: 'views/admin.html',
        controller: 'AdminCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
