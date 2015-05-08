define(['components/inbox/module'], function(module) {
  
  'use strict';

  return module.registerFactory('InboxConfig', ['$http', function($http) {
    return $http.get('api/inbox.json');
  }]);
})