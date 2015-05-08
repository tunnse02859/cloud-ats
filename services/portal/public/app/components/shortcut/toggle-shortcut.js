define(['auth/module'], function(module) {
  "use strict";

  module.registerDirective('toggleShortcut', ['$timeout', function($timeout) {

    var initDomEvents = function(element) {

      var shortcut_dropdown = $('#shortcut');

      //Shortcut animate hide
      function shortcut_button_hide() {
        shortcut_dropdown.animate({
          height: 'hide'
        }, 300, 'easeOutCirc');
        $('body').removeClass('shortcut-on');
      }

      //Shortcut animate show
      function shortcut_button_show() {
        shortcut_dropdown.animate({
          height: 'show'
        }, 200, 'easeOutCirc');
        $('body').addClass('shortcut-on');
      }

      element.on('click', function() {
        if (shortcut_dropdown.is(':visible')) {
          shortcut_button_hide();
        } else {
          shortcut_button_show();
        }
      });

      shortcut_dropdown.find('a').click(function(e) {
        e.preventDefault();
        window.location = $(this).attr('href');
        setTimeout(shortcut_button_hide, 300);
      });

      //Shortcut buttons goes away if mouse if clicked outside of the area
      $(document).mouseup(function(e) {
        if (shortcut_dropdown && !shortcut_dropdown.is(e.target) && shortcut_dropdown.has(e.target)) {
          shortcut_button_hide();
        }
      });
    }

    return {
      restrict: 'EA',
      link: function(scope, element) {
        $timeout(function() {
          initDomEvents(element);
        });
      }
    }
  }]);
});