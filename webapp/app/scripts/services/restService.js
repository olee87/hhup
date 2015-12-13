'use strict';

angular.module('hhupApp').service('restService', function($resource) {

  this.users = function() {
    return $resource('/rest/users/', {}, {
      save: {
        method: 'POST',
        url: '/rest/register/'
      },
      all: {
        method: 'GET',
        url: '/rest/users/',
        isArray: true
      },
      profile: {
        method: 'GET',
        url: '/rest/users/:userId'
      },
      edit: {
        method: 'POST',
        url: '/rest/editUser/'
      },
      recoverPassword : {
        method: 'POST',
        url: '/rest/recoverPassword/'
      },
      confirmRecoverPassword : {
        method: 'POST',
        url: '/rest/confirmRecoverPassword/'
      },
      confirm : {
        method: 'GET',
        url: '/rest/activateUser/'
      },
      nameTaken : {
        method: 'GET',
        url: '/rest/usernameTaken/:name',
        isArray: true
      }
    });
  };

  this.activities = function() {
    return $resource('/rest/activities/', {}, {
      save: {
        method: 'POST',
        url: '/rest/admin/createActivity'
      },
      all: {
        method: 'GET',
        url: '/rest/activities/',
        isArray: true
      },
      forUser: {
        method: 'GET',
        url: '/rest/activities/forUser/:userId',
        isArray: true
      },
      get: {
        method: 'GET',
        url: '/rest/activities/:activityId'
      },
      edit: {
        method: 'POST',
        url: '/rest/admin/editActivity'
      },
      join: {
        method: 'POST',
        url: '/rest/activities/join'
      },
      leave: {
        method: 'POST',
        url: '/rest/activities/leave'
      },
      conflicts: {
        method: 'GET',
        url: '/rest/activities/conflicts/:activityId',
        isArray: true
      },
      remove: {
        method: 'DELETE',
        url: '/rest/admin/removeActivity/:id'
      }
    });
  };

  this.couchMarket = function() {
    return $resource('/rest/couchmarket', {}, {
      all: {
        method: 'GET',
        url: '/rest/couchmarket',
        isArray: true
      },
      create: {
        method: 'POST',
        url: '/rest/couchmarket'
      },
      post: {
        method: 'GET',
        url: '/rest/couchmarket/:id'
      },
      remove: {
        method: 'DELETE',
        url: '/rest/couchmarket/:id'
      },
      edit: {
        method: 'POST',
        url: '/rest/couchmarket/edit'
      }
    });
  };

  this.guestbook = function() {
    return $resource('/rest/guestbook', {}, {
      all: {
        method: 'GET',
        url: '/rest/guestbook/all',
        isArray: true
      },
      create: {
        method: 'POST',
        url: '/rest/guestbook/create'
      },
      remove: {
        method: 'DELETE',
        url: '/rest/guestbook/:id'
      }
    });
  };

  this.history = function() {
    return $resource('/rest/history', {}, {
      get: {
        method: 'GET',
        url: '/rest/history',
        isArray: true
      }
    });
  };

  this.notes = function() {
    return $resource('/rest/notes', {}, {
      forUser: {
        method: 'GET',
        url: '/rest/notes',
        isArray: true
      },
      usersWithNotes: {
        method: 'GET',
        url: '/rest/notes/usersWithNotes',
        isArray: true
      },
      create: {
        method: 'POST',
        url: '/rest/notes'
      },
      remove: {
        method: 'DELETE',
        url: '/rest/notes'
      }
    });
  };

  this.about = function() {
    return $resource('/rest/about', {}, {
      all: {
        method: 'GET',
        url: '/rest/about',
        isArray: true
      },
      createOrEdit: {
        method: 'POST',
        url: '/rest/about'
      },
      remove: {
        method: 'DELETE',
        url: '/rest/about'
      }
    });
  };

  this.admin = function() {
    return $resource('/rest/admin/', {}, {
      makeAdmin: {
        method: 'POST',
        url: '/rest/admin/makeAdmin'
      },
      deleteUser: {
        method: 'POST',
        url: '/rest/admin/deleteUser'
      },
      setActive: {
        method: 'POST',
        url: '/rest/admin/setActive'
      },
      confirmPayment: {
        method: 'POST',
        url: '/rest/admin/confirmPayment'
      },
      checkin: {
        method: 'POST',
        url: '/rest/admin/checkin'
      },
      reReadIn: {
        method: 'POST',
        url: '/rest/admin/reReadIn'
      },
      testMail: {
        method: 'POST',
        url: '/rest/admin/testMail'
      }
    });
  };

  this.stats = function() {
    return $resource('/rest/public/', {}, {
      get: {
        method: 'GET',
        url: '/rest/public/stats'
      }
    });
  };

  this.statistics = function() {
    return $resource('/rest/', {}, {
      get: {
        method: 'GET',
        url: '/rest/statistics'
      }
    });
  };

  this.paypal = function() {
    return $resource('/rest/paypalConfig', {}, {
      get: {
        method: 'GET',
        url: '/rest/paypalConfig'
      }
    });
  };

  this.massMail = function() {
    return $resource('/rest/admin/massMail', {}, {
      // request = {activityId: UUID (optional), text: String, subject: String}
      send: {
        method: 'POST',
        url: '/rest/admin/massMail'
      }
    });
  };

  this.loginMessage = function() {
    return $resource('/rest', {}, {
      // request = {message: String, active: Boolean}
      set: {
        method: 'POST',
        url: '/rest/admin/loginMessage'
      },
      // request = {}
      // response = {active: boolean, message: String}
      get: {
        method: 'GET',
        url: '/rest/loginMessage'
      }
    });
  };
});
