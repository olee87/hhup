'use strict';

angular.module('hhupApp').controller('AdminCtrl', function ($location, $scope, restService, authenticationService, modalService, nationalitiesService) {

  authenticationService.checkLoggedIn();
  if (!authenticationService.isAdmin()) {
    $location.path('/');
  }

  $scope.testMailAddress = '';

  $scope.sendTestMail = function() {
    if ($scope.testMailAddress !== '') {
      console.log('sending test mail request');
      restService.admin().testMail({address: $scope.testMailAddress}, function() {
        modalService.message('ok', 'mail sent');
      }, function(message) {
        modalService.message('ouch', 'that didn\'t work! messagae: ' + message);
      });
    }
  };

  $scope.repo = '';
  $scope.reReadIn = function() {
    if ($scope.repo === '') {
      return;
    }
    restService.admin().reReadIn({type: $scope.repo} , function() {
      modalService.message('ok', 'done');
    }, function() {
      modalService.message('ouch', 'that didn\'t work');
    });
  };

  $scope.loginMessage = {message: '', active: false};

  var firstChange = true;

  restService.loginMessage().get({}, function(loginMessage) {
    $scope.loginMessage = loginMessage;
    $scope.$watch('loginMessage.message', function() {
      if (firstChange) {
        firstChange = false;
      } else {
        $scope.loginMessage.active = ($scope.loginMessage.message !== '');
      }
    });
  });

  $scope.setLoginMessage = function() {
    restService.loginMessage().set($scope.loginMessage, function() {
      modalService.message('ok', 'saved');
    });
  };


  $scope.sendMassMail = function() {
    var modal = modalService.input('send mass mail', [{
          caption: 'subject',
          name:'subject',
          type: 'text'
        }, {
          caption: 'message',
          name:'text',
          type: 'textarea'
      }
    ]);

    modal.result.then(function(mail) {
      mail.activityId = null;
      restService.massMail().send(mail, function() {
        modalService.message('ok', 'your message was sent to all participants of HHUP');
      });
    });
  };

  restService.statistics().get({}, function(statistics) {

    // users by country - pie chart
    $scope.userCountByCountry = [];
    angular.forEach(statistics.userCountByCountry, function(value, key){
      $scope.userCountByCountry.push({key: nationalitiesService.getForCode(key).ctry, y: value});
    });
    $scope.userCountByCountry.sort(function(entryA, entryB) {return entryB.y - entryA.y;});

    // paid users by country - pie chart
    $scope.paidUserCountByCountry = [];
    angular.forEach(statistics.paidUserCountByCountry, function(value, key){
      $scope.paidUserCountByCountry.push({key: nationalitiesService.getForCode(key).ctry, y: value});
    });
    $scope.paidUserCountByCountry.sort(function(entryA, entryB) {return entryB.y - entryA.y;});

    // payment ratio by country - horizontal bar chart
    $scope.paymentRatioByCountry = [];
    angular.forEach(statistics.paymentRatioByCountry, function(value, key){
      $scope.paymentRatioByCountry.push({key: nationalitiesService.getForCode(key).ctry, values: [[0, value * 100]]});
    });
    $scope.paymentRatioByCountry.sort(function(entryA, entryB) {return entryB.values[0][1] - entryA.values[0][1];});

    // signups and payments - line graph
    var signups = { key: '# registrations', values: [] };
    var payments = { key: '# payments', values: [] };
    angular.forEach(statistics.signupAndPaymentTimeLine, function(value){
      signups.values.push([value.date, value.signedUp ]);
      payments.values.push([value.date, value.paid ]);
    });
    signups.values.sort(function(entryA, entryB) {return entryB[0] - entryA[0];});
    payments.values.sort(function(entryA, entryB) {return entryB[0] - entryA[0];});
    $scope.signupsAndPayments = [signups, payments];
  });

  $scope.xFunction = function(){
    return function(d) {
        return d.key;
    };
  };

  $scope.yFunction = function(){
  	return function(d){
  		return d.y;
  	};
  };

  $scope.xAxisFunction = function(){
  	return function(date){
      return new Date(date).toString();
  	};
  };

  $scope.yAxisFunction = function(){
  	return function(count){
      return count;
  	};
  };

  $scope.toolTipContentFunction = function(){
	  return function(key, x, y) {
      return key + ': ' + y + '%';
	  };
  };
});
