'use strict';

angular.module('hhupApp').service('modalService', function($uibModal) {

  this.error = function(title, text) {
    return $uibModal.open({
      templateUrl: 'views/modal.html',
      controller: 'ModalCtrl',
      resolve: {
        message: function() {return text;},
        title: function() {return title;},
        buttons: function() {return {ok: true};},
        fields: function() {return [];},
        inputMode: function() {return false;}
      }
    });
  };

  this.message = function(title, text) {
    return $uibModal.open({
      templateUrl: 'views/modal.html',
      controller: 'ModalCtrl',
      resolve: {
        message: function() {return text;},
        title: function() {return title;},
        buttons: function() {return {ok: true};},
        fields: function() {return [];},
        inputMode: function() {return false;}
      }
    });
  };

  this.question = function(title, text) {
    return $uibModal.open({
      templateUrl: 'views/modal.html',
      controller: 'ModalCtrl',
      resolve: {
        message: function() {return text;},
        title: function() {return title;},
        buttons: function() {return {yes: true, no: true};},
        fields: function() {return [];},
        inputMode: function() {return false;}
      }
    });
  };

  this.singleTextInput = function(title, placeholder) {
    return this.input(title, [{caption: placeholder, name:'result', type: 'textarea'}]);
  };

  // fields = [{caption: String, name: String, type: String},...]
  this.input = function(title, fields) {
    return $uibModal.open({
      templateUrl: 'views/modal.html',
      controller: 'ModalCtrl',
      resolve: {
        message: function() {return '';},
        title: function() {return title;},
        buttons: function() {return {submit: true, cancel: true};},
        fields: function() {return fields;},
        inputMode: function() {return true;}
      }
    });
  };
});
