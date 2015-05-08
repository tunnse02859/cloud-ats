define(['app'], function(app) {
  "use strict";

  app.directive('languageSelector', function(Language) {
    return {
      restrict: 'EA',
      replace: true,
      templateUrl: 'app/components/language/language-selector.tpl.html',
      scope: true
    };
  });
});