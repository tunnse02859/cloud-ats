define(['app'], function(app) {
  "use strict";

  return app.directive('recentProjects', ['Project', function(Project){
    return {
      restrict: 'EA', // E = Element, A = Attribute, C = Class, M = Comment
      replace: true,
      templateUrl: 'app/components/projects/recent-projects.tpl.html',
      scope: true,
      link: function(scope, element) {
        Project.list.then(function(response) {
          scope.projects = response.data;
        });

        scope.clearProjects = function() {
          scope.projects = [];
        }
      }
    };
  }]);
});