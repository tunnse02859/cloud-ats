define(['components/calendar/module'], function(calendar) {
  'use strict';

  calendar.registerFactory('CalendarEvent', ['$resource', function($resource) {
    return $resource('api/events.json', {_id: '@id'});
  }]);
})