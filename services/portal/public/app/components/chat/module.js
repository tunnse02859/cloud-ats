define(['angular',
  'angular-couch-potato',
  'angular-sanitize'
  ], function(ng, couchPotato) {
    'use strict';

    var module = ng.module('app.chat', ['ngSanitize']);

    couchPotato.configureApp(module);

    module.run(function($couchPotato) {
      module.lazy = $couchPotato;
    });

    return module;
  });