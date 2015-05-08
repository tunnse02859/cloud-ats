//
window.name = "NG_DEFER_BOOTSTRAP!";

define([
  'require',
  'jquery',
  'angular',
  'domReady',

  'bootstrap',
  'app',
  'appConfig',
  'modules-includes'
], function(require, $, ng, domReady) {
  'use strict';

  domReady(function(document) {
    ng.bootstrap(document, ['app']);
    ng.resumeBootstrap();
  });
});