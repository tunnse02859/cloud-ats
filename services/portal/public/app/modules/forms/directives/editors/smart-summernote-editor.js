define(['modules/forms/module', 'summernote'], function(module) {

  'use strict';

  module.registerDirective('smartSummernoteEditor', function() {
    return {
      restrict: 'A',
      compile: function(element, attributes) {
        element.removeAttr('smart-summernote-editor data-smart-summernote-editor');

        var options = {
          focus: true,
          tabsize: 2
        };

        if (attributes.height) {
          options.height = attributes.height;
        }

        element.summernote(options);
      }
    }
  })
});