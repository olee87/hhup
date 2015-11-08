'use strict';

/* jshint unused: false*/
var app = angular.module('hhupApp');

app.factory('Account', function ($resource) {
        return $resource('/rest/user', {}, {});
    });

app.factory('Session', function () {
        this.create = function (username, realName, email, authorities, id) {
          this.username = username;
          this.realName = realName;
          this.email = email;
          this.authorities = authorities;
          this.id = id;
        };
        this.invalidate = function () {
          this.username = null;
          this.realName = null;
          this.email = null;
          this.authorities = null;
          this.id = null;
        };
        return this;
    });

app.factory('authenticationService', function ($rootScope, $http, $location, authService, Session, Account) {
  var isFunction = function(functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
  };
        return {
            login: function (param) {
                var data ='username=' + param.username +'&password=' + param.password +'&hhup.rememberMe=' + param.rememberMe +'&submit=Login';
                $http.post('/rest/login', data, {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    ignoreAuthModule: 'ignoreAuthModule'
                }).success(function (data, status, headers, config) {
                    Account.get(function(data) {
                        Session.create(data.username, data.realName, data.email, data.authorities, data.id);
                        $rootScope.account = Session;
                        $rootScope.user = data;
                        authService.loginConfirmed(data);
                    });
                }).error(function (data, status, headers, config) {
                    $rootScope.authenticationError = true;
                    Session.invalidate();
                    authService.loginCancelled();
                });
            },
            logout: function () {
                $rootScope.authenticationError = false;
                $rootScope.authenticated = false;
                $rootScope.account = null;
                $rootScope.user = null;

                $http.get('/rest/logout');
                Session.invalidate();
                authService.loginCancelled();
            },
            userId: function() {
              return $rootScope.user ? $rootScope.user.id : false;
            },
            user: function() {
              return $rootScope.user;
            },
            isAdmin: function() {
              return $rootScope.user ? $rootScope.user.authorities.indexOf('ADMIN') >= 0 : false;
            },
            isLoggedIn: function() {
              return $rootScope.user ? true: false;
            },
            autoLogin: function() {
              Account.get(function(data) {
                $rootScope.user = data;
                authService.loginConfirmed(data);
              });
            },
            checkLoggedIn: function() {
              if (!$rootScope.user) {
                $location.path('/');
              }
            },
            updateUser: function(updatedUser) {
              $rootScope.user = updatedUser;
            }
        };
    });

app.factory('authService', function($rootScope, httpBuffer) {
  return {
      /**
       * Call this function to indicate that authentication was successfull and trigger a
       * retry of all deferred requests.
       * @param data an optional argument to pass on to $broadcast which may be useful for
       * example if you need to pass through details of the user that was logged in
       */
      loginConfirmed: function(data, configUpdater) {
          var updater = configUpdater || function(config) {return config;};
          $rootScope.$broadcast('event:auth-loginConfirmed', data);
          httpBuffer.retryAll(updater);
      },

      /**
       * Call this function to indicate that authentication should not proceed.
       * All deferred requests will be abandoned or rejected (if reason is provided).
       * @param data an optional argument to pass on to $broadcast.
       * @param reason if provided, the requests are rejected; abandoned otherwise.
       */
      loginCancelled: function(data, reason) {
          httpBuffer.rejectAll(reason);
          $rootScope.$broadcast('event:auth-loginCancelled', data);
      }
  };
});

/**
* Private module, a utility, required internally by 'http-auth-interceptor'.
*/

app.factory('httpBuffer', function($injector) {
  /** Holds all the requests, so they can be re-requested in future. */
  var buffer = [];

  /** Service initialized later because of circular dependency problem. */
  var $http;

  function retryHttpRequest(config, deferred) {
      function successCallback(response) {
          deferred.resolve(response);
      }
      function errorCallback(response) {
          deferred.reject(response);
      }
      $http = $http || $injector.get('$http');
      $http(config).then(successCallback, errorCallback);
  }

  return {
      /**
       * Appends HTTP request configuration object with deferred response attached to buffer.
       */
      append: function(config, deferred) {
          buffer.push({
              config: config,
              deferred: deferred
          });
      },

      /**
       * Abandon or reject (if reason provided) all the buffered requests.
       */
      rejectAll: function(reason) {
          if (reason) {
              for (var i = 0; i < buffer.length; ++i) {
                  buffer[i].deferred.reject(reason);
              }
          }
          buffer = [];
      },

      /**
       * Retries all the buffered requests clears the buffer.
       */
      retryAll: function(updater) {
          for (var i = 0; i < buffer.length; ++i) {
              retryHttpRequest(updater(buffer[i].config), buffer[i].deferred);
          }
          buffer = [];
      }
  };
});
