define(['modules/graphs/module', 'easy-pie'], function(module) {
  'use strict';

  return module.registerDirective('easyPieChartContainer', function() {
    return {
      restrict: 'A',
      link: function(scope, element) {
        if ($.fn.easyPieChart) {
          $('.easy-pie-chart').each(function() {
            var $this = $(this),
                barColor = $this.css('color') || $this.data('pie-color'),
                trackColor = $this.data('pie-track-color') || 'rgba(0,0,0,0.04)',
                size = parseInt($this.data('pie-size')) || 25;

            $this.easyPieChart({
              barColor: barColor,
              trackColor: trackColor,
              scaleColor: false,
              lineCap: 'butt',
              lineWidth: parseInt(size / 8.5),
              animate: 1500,
              rotate: -90,
              size: size,
              onStep: function(from, to, percent) {
                $(this.el).find('.percent').text(Math.round(percent));
              }
            });

            $this = null;
          });
        }
      }
    }
  });
});