define(['layout/module'], function(module) {
  "use strict";

  return module.registerDirective('fullScreen', function() {
    return {
      restrict: 'A',
      link: function(scope, element) {
        var $body = $('body');
        var toggleFullScreen = function(e) {
          if(!$body.hasClass('full-screen')) {
            $body.addClass('full-screen');
            if (document.documentElement.requestFullscreen) {
              document.documentElement.requestFullscreen();
            } else if (document.documentElement.mozRequestFullScreen) {
              document.documentElement.mozRequestFullScreen();
            } else if (document.documentElement.webkitRequestFullscreen) {
              document.documentElement.webkitRequestFullscreen();
            } else if (document.documentElement.msRequestFullscreen) {
              document.documentElement.msRequestFullscreen();
            }
          } else {
            $body.removeClass('full-screen');
            if (document.exitFullscreen) {
              document.exitFullscreen();
            } else if (document.mozCancelFullScreen) {
              document.mozCancelFullScreen();
            } else if (document.webkitRequestFullscreen) {
              document.webkitRequestFullscreen();
            }
          }
        };

        element.on('click', toggleFullScreen);
      }
    }
  });
});