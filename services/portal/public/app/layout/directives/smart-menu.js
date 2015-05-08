define(['layout/module', 'jquery'], function(module) {
  "use strict";

  //Custom jquery
  (function($) {

    $.fn.smartCollapToggle = function() {
      return this.each(function() {

        var $body = $('body');
        var $this = $(this);

        if ($body.hasClass('menu-on-top')) {

        } else {

          //
          // if($body.hasClass('mobile-view-activated')) {
            // $this.toggleClass('open');
          // }

          //toggle open
          $this.toggleClass('open');

          //for minified menu collapse only second level
          if($body.hasClass('minified')) {
            if($this.closest('nav ul ul').length) {
              $this.find('>a .collapse-sign .fa').toggleClass('fa-minus-square-o fa-plus-square-o');

              //TODO: config from appConfig.menu_speed || 200
              $this.find('ul:first').slideToggle(200);
            } 
          } else {
            //toggle expand item
            $this.find('>a .collapse-sign .fa').toggleClass('fa-minus-square-o fa-plus-square-o');

            //TODO: config from appConfig.menu_speed || 200
            $this.find('ul:first').slideToggle(200);
          }
        }

      });
    };

  })(jQuery);

  module.registerDirective('smartMenu', ['$state', '$rootScope', function($state, $rootScope) {

    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var $body = $('body');
        var $collapsible = element.find('li[data-menu-collapse]');

        $collapsible.each(function(indx, li) {
          var $li = $(li);

          $li.on('click', '>a', function(e) {
            //collapse all open siblings
            $li.siblings('.open').smartCollapToggle();

            //toggle element
            $li.smartCollapToggle();

            //add active marker to collapsed element if it has active childs
            if (!$li.hasClass('open') && $li.find('li.active').length > 0) {
              $li.addClass('active');
            }

            e.preventDefault();
          });

          $li.find('>a').append('<b class="collapse-sign"><em class="fa fa-plus-square-o"></em></b>');

          //initialization toggle
          if($li.find('li.active').length > 0) {
            $li.smartCollapToggle();
            $li.find('li.active').parents('li').addClass('active');
          }

        });
      }
    };

  }]);
});