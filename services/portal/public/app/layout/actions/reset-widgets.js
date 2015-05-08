define(['layout/module'], function(module) {
  "use strict";

  module.registerDirective('resetWidgets', ['$state', function($state) {
    return {
      restrict: 'A',
      link: function(scope, element) {
        element.on('click', function() {
          $.SmartMessageBox({
            title: "<i class='fa fa-refresh' style='color:green'></i> Clear Local Storage",
            content: "Would you like to REST all your saved widgets and clear LocalStorage?",
            buttons: '[No][Yes]'
          }, function(ButtonPressed) {
            if (ButtonPressed == 'Yes' && localStorage) {
              localStorage.clear();
              location.reload();
            }
          });
        });
      }
    }
  }]);
});