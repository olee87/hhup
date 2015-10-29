'use strict';

angular.module('hhupApp').service('validationService', function() {

  this.isInvalidUsername = function(user) {
    // empty username is not validated
    if (user === '') {
      return false;
    }
    // username must have at least 3 chars
    if (user.length < 3) {
      return 'your username must contain at least 3 characters';
    }
    // username must not have more than 20 chars
    if (user.length > 20) {
      return 'your username may contain 20 characters at most';
    }
    // only letters, digits and . _ are allowed
    if (!user.match('^[a-zA-Z0-9_.]+$')) {
      return 'your username may only contain letters, digits and . _';
    }
    return false;
  };

  this.isInvalidPassword = function(password) {
    // empty password is not validated
    if (password === '') {
      return false;
    }
    // everything's okay
    return false;
  };

  this.isInvalidPasswordConfirm = function(password, passwordConfirm) {
    // empty password confirmation is not validated
    if (passwordConfirm === '') {
      return false;
    }
    // confirmation has to match password
    if (password !== passwordConfirm) {
      return 'password does not match confirmation';
    }
    // everything's okay
    return false;
  };

  this.isInvalidEmail = function(email) {
    // empty password is not validated
    if (email === '') {
      return false;
    }
    // it has to be in the format of an email address
    if (!email.match('^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$')) {
      return 'invalid email address';
    }
    // everything's okay
    return false;
  };
});
