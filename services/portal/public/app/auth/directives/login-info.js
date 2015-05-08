define(['auth/module'], function(module) {
  "use strict";

  return module.registerDirective('loginInfo', ['CurrentUser', function(CurrentUser){
    return {
      restrict: 'A',
      templateUrl: 'app/auth/directives/login-info.tpl.html',
      link: function($scope, $element) {
        CurrentUser.initialized.then(function() {
          $scope.currentUser = CurrentUser;
        });
      }
    };
  }]);
});
