define([
  'angular',
  'angular-couch-potato',
  'angular-ui-router',
  'angular-cookies'
], function(ng, couchPotato) {
  "use strict";

  var module = ng.module('app.auth', ['ui.router', 'ngCookies']);

  couchPotato.configureApp(module);

  module.config(['$stateProvider', '$couchPotatoProvider', function($stateProvider, $couchPotatoProvider) {
    $stateProvider.state('login', {
      url: '/login',
      views: {
        root: {
          templateUrl: 'app/auth/views/login.html'
        }
      },
      data: {
        title: 'Login',
        htmlId: 'extr-page'
      },
      resolve: {
        deps: $couchPotatoProvider.resolveDependencies([
          'auth/controllers/login-controller'
        ])
      }
    })
  }]);

  module.run(['$couchPotato', function($couchPotato){
    module.lazy = $couchPotato;
  }]);

  return module;
});
