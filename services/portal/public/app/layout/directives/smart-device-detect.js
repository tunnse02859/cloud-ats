define(['layout/module'], function(module) {
  'use strict';

  module.registerDirective('smartDeviceDetect', function() {
    return {
      restrict: 'A',
      compile: function(element, attributes) {
        element.removeAttr('smart-device-detect data-smart-device-detect');
        var isMobile = (/iphone|ipad|ipod|android|blackberry|mini|windows\sce|palm/i.test(navigator.userAgent.toLowerCase()));
        
        element.toggleClass('desktop-detected', !isMobile);
        element.toggleClass('mobile-detected', isMobile);
      }
    }
  });
});