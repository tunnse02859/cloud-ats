define(['layout/module'], function(module) {

  'use strict';

  module.registerDirective('smartContext', 
    ['$rootScope', '$state', '$cookies', '$window', 'AuthenticationService', 
    function($rootScope, $state, $cookies, $window, AuthenticationService) {
      return {
        restrict: 'A',
        compile: function (element, attributes) {
          element.removeAttr('smart-context data-smart-context');

          var listener = function(event, toState, toParams, fromState, fromParams) {

            var requireLogin = toState.data !== undefined && toState.data.requireLogin;

            if (requireLogin) {

              if ($cookies.get('authToken') === undefined) {
                $window.sessionStorage.setItem('context', null);
                $state.go('login');
                event.preventDefault();
                return;
              }

              var current = $window.sessionStorage.getItem('context');

              //call to service to get current context with authToken if session is not setted
              if (current === null) {
                AuthenticationService.context().then(function(context) {

                  if (context.user === undefined && context.tenant === undefined) {
                    $state.go('login');
                    event.preventDefault();
                    return;
                  } else {
                    $window.sessionStorage.setItem('context', JSON.stringify(context));
                  }
                });
              }
            }
          };

          $rootScope.$on('$stateChangeStart', listener);
        }
      }
    }]
  );

});