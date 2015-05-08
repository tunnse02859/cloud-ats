define([
  'angular',
  'angular-couch-potato',
  'angular-ui-router'
], function(ng, couchPotato) {
  "use strict";

  var module = ng.module('app.layout', ['ui.router']);

  couchPotato.configureApp(module);

  module.config(['$stateProvider', '$couchPotatoProvider', '$urlRouterProvider',
    function ($stateProvider, $couchPotatoProvider, $urlRouterProvider) {
    $stateProvider
      .state('app', {
        abstract: true,
        views: {
          'root': {
            templateUrl: 'app/layout/layout.tpl.html',
            resolve: {
              deps: $couchPotatoProvider.resolveDependencies([
                'auth/directives/login-info',
                'modules/graphs/directives/inline/sparkline-container',
                'components/chat/api/chat-api',
                'components/inbox/directives/unread-messages-count'
              ])
            }
          }
        }
      });
    $urlRouterProvider.otherwise('/login');
  }]);

  module.run(['$couchPotato', function($couchPotato) {
    module.lazy = $couchPotato;
  }]);

  return module;
})