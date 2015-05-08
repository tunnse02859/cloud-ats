define(['components/calendar/module', 'fullcalendar'], function(module) {

  'use strict';

  module.registerDirective('fullCalendar', ['CalendarEvent', '$log', '$timeout',
    function(CalendarEvent, $log, $timeout) {
      return {
        restrict: 'E',
        replace: true,
        templateUrl: 'app/components/calendar/directives/full-calendar.tpl.html',
        scope: {
          events: '=events'
        },
        link: function(scope, element) {
          var $calendar = $('#calendar');

          var calendar = null;

          function initCalendar() {
            calendar = $calendar.fullCalendar({
              editable: true,
              draggable: true,
              selectable: false,
              selectHelper: true,
              unselectAuto: false,
              disableResizing: false,
              droppable: true,

              header: {
                left: 'title',
                center: 'prev, next, today',
                right: 'month, agendaWeek, agendaDay'
              },
              drop: function(date, allDay) {
                var originalEventObject = $(this).data('eventObject');

                var copiedEventObject = $.extend({}, originalEventObject);

                copiedEventObject.start = date;
                copiedEventObject.allDay = allDay;

                $('#calendar').fullCalendar('renderEvent', copiedEventObject, true);

                if ($('#drop-remove').is(':checked')) {
                  var index = $(this).scope().$index;
                  $('#external-events').scope().eventsExternal.splice(index, 1);
                  $(this).remove();
                }
              },
              select: function(start, end, allDay) {
                var title = prompt('Event Title:');
                if (title) {
                  calendar.fullCalendar('renderEvent', {
                    title: title,
                    start: start,
                    end: end,
                    allDay: allDay
                  }, true);
                }
                calendar.fullCalendar('unselect');
              },
              events: function(start, end, timezone, callback) {
                callback(scope.events);
              },
              eventRender: function(event, element, icon) {
                if (!event.description == "") {
                  element.find('.fc-event-title').append('<br/><span class="ultra-light">' + event.description + '</span>');
                }
                if (!event.icon == "") {
                  element.find('.fc-event-title').append('<i class="air air-top-right fa ' + event.icon + '"></i>');
                }
              }
            });

            $('.fc-header-right, .fc-header-center', $calendar).hide();
          }

          initCalendar();

          scope.$watch('events', function(newValue, oldValue) {
            $calendar.fullCalendar('refetchEvents');
          }, true);

          scope.next = function() {
            $('.fc-button-next', $calendar).click();
          };
          scope.prev = function() {
            $('.fc-button-prev', $calendar).click();
          };
          scope.today = function() {
            $('.fc-button-today', $calendar).click();
          };
          scope.changeView = function(period) {
            $calendar.fullCalendar('changeView', period);
          };
        }
      }
    }]);

});