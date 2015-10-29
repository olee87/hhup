'use strict';

describe('Controller: SignupCtrl', function () {

  // load the controller's module
  beforeEach(module('hhupApp'));

  var AboutCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AboutCtrl = $controller('SignupCtrl', {
      $scope: scope
    });
  }));

  it('should be tested more thoroughly', function () {
    expect(true).toBe(true);
  });
});