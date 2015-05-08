define([
  'angular',
  'angular-couch-potato',
  'angular-ui-router',
  'angular-resource'
], function(ng, couchPotato) {
  "use strict";

  var module = ng.module('app.dashboard', ['ui.router', 'ngResource']);

  module.config(function ($stateProvider, $couchPotatoProvider) {
    $stateProvider
      .state('app.dashboard', {
        url: '/dashboard',
        views: {
          "content@app": {
            controller: 'DashboardCtrl',
            templateUrl: 'app/dashboard/dashboard.html',
            resolve: {
              deps: $couchPotatoProvider.resolveDependencies([
                'dashboard/dashboard-controller',
                'modules/graphs/directives/inline/sparkline-container',
                'modules/graphs/directives/inline/easy-pie-chart-container',
                'modules/graphs/directives/vectormap/vector-map',
                'modules/graphs/directives/flot/flot-basic',
                'components/chat/directives/chat-widget',
                'components/chat/directives/chat-users',
              ])
            }
          }
        },
        data: {
          title: 'Dashboard'
        }
      })
  });

  couchPotato.configureApp(module);

  module.run(function($couchPotato) {
    module.lazy = $couchPotato;
  });

  return module;
});