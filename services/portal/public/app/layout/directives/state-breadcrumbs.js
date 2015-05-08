define(['layout/module'], function (module) {

  'use strict';
                                                 
  module.registerDirective('stateBreadcrumbs', ['$rootScope', '$state', function($rootScope, $state) {
    return {
      restrict: 'E',
      replace: true,
      template: '<ol class="breadcrumb"><li>Home</li></ol>',
      link: function(scope, element) {
        function setBreadcrumbs(breadcrumbs) {
          var html = '<li>Home</li>';
          angular.forEach(breadcrumbs, function(crumb) {
            html += '<li>' + crumb + '</li>';
          });
          element.html(html);
        }

        function fetchBreadcrumbs(stateName, breadcrumbs) {
          var state = $state.get(stateName);
          if (state && state.data && state.data.title && breadcrumbs.indexOf(state.data.title) == -1) {
            breadcrumbs.unshift(state.data.title);
          }

          var parentName = stateName.replace(/.?\w+$/, '');
          if (parentName) {
            return fetchBreadcrumbs(parentName, breadcrumbs);
          } else {
            return breadcrumbs;
          }
        }

        function processState(state) {
          var breadcrumbs;
          if (state.data && state.data.breadcrumbs) {
            breadcrumbs = state.data.breadcrumbs;
          } else {
            breadcrumbs = fetchBreadcrumbs(state.name, []);
          }
          setBreadcrumbs(breadcrumbs);
        }

        processState($state.current);

        $rootScope.$on('$stateChangeStart', function(event, state) {
          processState(state);
        });
      }
    };
  }]);

});