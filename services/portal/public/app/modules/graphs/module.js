define([
  'angular', 
  'angular-couch-potato', 
  'angular-ui-router'
], function(ng, couchPotato) {
  'use strict';

  var module = ng.module('app.graphs', ['ui.router']);

  couchPotato.configureApp(module);

  module.config(['$stateProvider', '$couchPotatoProvider', function($stateProvider, $couchPotatoProvider) {
    $stateProvider
      .state('app.graphs', {
        abstract: true,
        data: {
          title: 'Graphs'
        }
      })
      .state('app.graphs.flot', {
        url: '/graphs/flot',
        data: {
          title: 'Flot Charts'
        },
        views: {
          'content@app': {
            controller: 'FlotCtrl',
            templateUrl: 'app/modules/graphs/views/flot-chart.tpl.html',
            resolve: {
              deps: $couchPotatoProvider.resolveDependencies([
                'modules/graphs/controllers/flot-controller'
              ])
            }
          }
        }
      });
  }]);

  module.run(['$couchPotato', function($couchPotato) {
    module.lazy = $couchPotato;
  }]);

  return module;
});