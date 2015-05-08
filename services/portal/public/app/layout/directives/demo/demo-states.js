define(['layout/module', 'lodash', 'notification'], function(module, _) {
  "use strict";

  module.registerDirective('demoStates', ['$rootScope', function($rootScope) {
    return {
      restrict: 'E',
      replace: true,
      templateUrl: 'app/layout/directives/demo/demo-states.tpl.html',
      scope: true,
      link: function(scope, element, attributes) {
        element.parent().css({
          position: 'relative'
        });

        element.on('click', '#demo-setting', function() {
          element.toggleClass('activate');
        });
      },
      controller: function($scope) {

        var $body = $('body');

        $scope.fixedHeader = localStorage.getItem('sm-fixed-header') == 'true';
        $scope.fixedNavigation = localStorage.getItem('sm-fixed-navigation') == 'true';
        $scope.fixedRibbon = localStorage.getItem('sm-fixed-ribbon') == 'true';
        $scope.fixedPageFooter = localStorage.getItem('sm-fixed-page-footer') == 'true';
        $scope.insideContainer = localStorage.getItem('sm-inside-container') == 'true';
        $scope.menuOnTop = localStorage.getItem('sm-menu-on-top') == 'true';

        $scope.skins = appConfig.skins;
        $scope.smartSkin = localStorage.getItem('sm-skin') || appConfig.smartSkin;

        $scope.setSkin = function(skin) {
          $scope.smartSkin = skin.name;
          $body.removeClass(_.pluck($scope.skins, 'name').join(' '));
          $body.addClass(skin.name);
          localStorage.setItem('sm-skin', skin.name);
          $('#logo img').attr('src', skin.logo);
        }

        if ($scope.smartSkin != 'smart-style-0') {
          $scope.setSkin(_.find($scope.skins, {name: $scope.smartSkin}));
        }

        $scope.factoryReset = function() {
          $.SmartMessageBox({
            title: '<i class="fa fa-refresh" style="color: green"></i> Clear Local Storage',
            content: 'Would you like to RESET all your saved widgets and clear LocalStorage?',
            buttons: '[No][Yes]'
          }, function(ButtonPressed) {
            if (ButtonPressed == 'Yes' && localStorage) {
              localStorage.clear();
              location.reload();
            }
          });
        }

        $scope.$watch('fixedHeader', function(fixedHeader) {

          localStorage.setItem('sm-fixed-header', fixedHeader);
          $body.toggleClass('fixed-header', fixedHeader);
          if (fixedHeader == false) {
            $scope.fixedRibbon = false;
            $scope.fixedNavigation = false;
          }
        });

        $scope.$watch('fixedNavigation', function(fixedNavigation) {
          localStorage.setItem('sm-fixed-navigation', fixedNavigation);
          $body.toggleClass('fixed-navigation', fixedNavigation);
          if (fixedNavigation) {
            $scope.insideContainer = false;
            $scope.fixedHeader = true;
          } else {
            $scope.fixedRibbon = false;
          }
        });

        $scope.$watch('fixedRibbon', function(fixedRibbon) {
          localStorage.setItem('sm-fixed-ribbon', fixedRibbon);
          $body.toggleClass('fixed-ribbon', fixedRibbon);
          if (fixedRibbon) {
            $scope.fixedHeader = true;
            $scope.fixedNavigation = true;
            $scope.insideContainer = false;
          }
        });

        $scope.$watch('fixedPageFooter', function(fixedPageFooter) {
          localStorage.setItem('sm-fixed-page-footer', fixedPageFooter);
          $body.toggleClass('fixed-page-footer', fixedPageFooter);
        });

        $scope.$watch('insideContainer', function(insideContainer) {
          localStorage.setItem('sm-inside-container', insideContainer);
          $body.toggleClass('container', insideContainer);
          if (insideContainer) {
            $scope.fixedRibbon = false;
            $scope.fixedNavigation = false;
          }
        });

        $scope.$watch('menuOnTop', function(menuOnTop) {
          localStorage.setItem('sm-menu-on-top', menuOnTop);
          $body.toggleClass('menu-on-top', menuOnTop);

          if (menuOnTop) $body.removeClass('minified');
        });
      }
    }
  }]);
});