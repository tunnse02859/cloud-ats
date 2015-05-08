define(['app'], function(app) {
  'use strict';

  return app.registerFactory('Todo', ['$resource', function($resource) {
    return $resource('api/todos.json', {id : '@_id'});
  }]);
})