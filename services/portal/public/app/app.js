'use strict';

/***
  *
  * Main module of application
  *
  */
define([
  'angular',
  'angular-couch-potato',
  'angular-ui-router',
  'angular-animate',
  'angular-loading-bar',
  'angular-bootstrap',
  'smartwidgets',
  'notification'
], function(ng, couchPotato) {

  var app = ng.module('app', [

    'scs.couch-potato',
    'ngAnimate',
    'ui.router',
    'ui.bootstrap',

    'angular-loading-bar',

    //App
    'app.layout',
    'app.dashboard',
    'app.auth',
    'app.graphs',
    'app.widgets',
    'app.calendar',
    'app.chat',
    'app.inbox',
    'app.forms'
  ]);

  couchPotato.configureApp(app);

  app.config(['$provide', '$httpProvider', 'cfpLoadingBarProvider',function($provide, $httpProvider, cfpLoadingBarProvider) {

    //
    cfpLoadingBarProvider.includeSpinner = false;

    //Intercept http calls.
    $provide.factory('ErrorHttpInterceptor', function($q) {
      var errorCounter = 0;
      function notifyError(rejection) {
        $.bigBox({
          title: rejection.status + ' ' + rejection.statusText,
          content: rejection.data,
          color: "#C46A69",
          icon: "fa fa-warning shake animated",
          number: ++errorCounter,
          timeout: 6000
        });
      }

      return {
        //On request failure
        requestError: function(rejection) {
          //show notification
          notifyError(rejection);

          //return the promise rejection.
          return $q.reject(rejection);
        },

        //On response failure
        responseError: function(rejection) {
          //show notification
          notifyError(rejection);
          //return the promise rejection.
          return $q.reject(rejection);
        }
      };
    });

    //Add the interceptor to $httpProvider.
    $httpProvider.interceptors.push('ErrorHttpInterceptor');

  }]);

  app.run(['$couchPotato', '$rootScope', '$state', '$stateParams', 
    function($couchPotato, $rootScope, $state, $stateParams){
      app.lazy = $couchPotato;
      $rootScope.$state = $state;
      $rootScope.$stateParams = $stateParams;    
  }]);

  return app;
});