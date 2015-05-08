define(['modules/graphs/module', 'modules/graphs/directives/flot/flot-config',
  'flot',
  'flot-resize',
  'flot-fillbetween',
  'flot-orderBar',
  'flot-pie',
  'flot-time',
  'flot-tooltip'
], function(module, config) {

  'use strict';

  module.registerDirective('flotBarChart', function() {
    return {
      restrict: 'E',
      replace: true,
      template: '<div class="chart"></div>',
      scope: {
        data: '='
      },
      link: function(scope, element) {
        
      }
    }
  });

})