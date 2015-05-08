define(['app'], function(app) {
  "use strict";

  return app.controller('ActivitiesCtrl', ['$scope', '$log', '$filter', 'activityService', ActivitiesCtrl]);

  function ActivitiesCtrl($scope, $log, $filter, activityService) {
    $scope.activeTab = 'default';
    $scope.currentActivityItems = [];

    //Getting different type of activities
    activityService.get(function(data) {

      $scope.activities = data.activities;

    });

    $scope.isActive = function(tab) {
      return $scope.activeTab === tab;
    };

    $scope.setTab = function(activityType) {
      $scope.activeTab = activityType;

      activityService.getbytype(activityType, function(data) {
        $scope.currentActivityItems = data.data;
      });

      $filter('filter')($scope.activities.types, function(value, index) {
        if(value.name === activityType) {
          value.length = 0;
        }
      });
    }
  }
})