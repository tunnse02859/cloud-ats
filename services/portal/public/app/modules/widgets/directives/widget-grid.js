define(['modules/widgets/module', 'lodash'], function(module, _) {
  'use strict';

  module.registerDirective('widgetGrid', ['$rootScope', '$compile', '$q', '$state', '$timeout',
    function($rootScope, $compile, $q, $state, $timeout) {
      var jarvisWidgetDefaults = {
        grid: 'article',
        widgets: '.jarviswidget',
        localStorage: true,
        deleteSettingsKey: '#deletesettingskey-options',
        settingsKeyLabel: 'Reset settings?',
        deletePositionKey: '#deletepositionkey-options',
        positionKeyLabel: 'Reset position?',
        sortalbe: true,
        buttonsHidden: false,
        
        //toggle button
        toggleButton: true,
        toggleClass: 'fa fa-minus | fa fa-plus',
        toggleSpeed: 200,
        onToggle: function() {},
        
        //delete button
        deleteButton: true,
        deleteMsg: 'Warning: This action cannot be undone!',
        deleteClass: 'fa fa-times',
        deleteSpeed: 200,
        onDelete: function() {},

        //edit button
        editButton: true,
        editPlaceHolder: '.jarviswidget-edibox',
        editClass: 'fa fa-cog | fa fa-save',
        editSpeed: 200,
        onEdit: function() {},

        //color button
        colorButton: true,
        //full screen
        fullscreenButton: true,
        fullscreenClass: 'fa fa-expand | fa fa-compress',
        fullscreenDiff: 3,
        onFullscreen: function() {},

        //custome btn
        customButton: false,
        customClass: 'folder-10 | next-10',
        customStart: function() {
          alert('Hello you, this is a custom button....');
        },
        customEnd: function() {
          alert('Bye, till next time....');
        },

        //order
        buttonOrder: '%refresh% %custom% %edit% %toggle% %fullscreen% %delete%',
        opacity: 1.0,
        dragHandle: '> header',
        placeholderClass: 'jarviswidget-placeholder',
        indicator: true,
        indicatorTime: 600,
        ajax: true,
        timestampPlaceHolder: '.jarviswidget-timestamp',
        timestampFormat: 'Last update: %m%/%d%/%y% %h%:%i%:%s%',
        refreshButton: true,
        refreshButtonClass: 'fa fa-refresh',
        labelError: 'Sorry but there was an error:',
        lableUpdated: 'Last Update:',
        lableRefresh: 'Refresh',
        labelDelete: 'Delete widget:',
        afterLoad: function() {},
        rtl: false,
        onChange: function() {},
        onSave: function() {},
        ajaxnav: true
      }


      var initDropdowns = function(widgetIds) {
        angular.forEach(widgetIds, function(wid){
          $('#' + wid + ' [data-toggle="dropdown"]').each(function() {
            var $parent = $(this).parent();
            $(this).removeAttr('data-toggle');
            if (!$parent.attr('dropdown')) {
              $(this).removeAttr('href');
              $parent.attr('dropdown', '');
              var compiled = $compile($parent)($parent.scope());
              $parent.replaceWith(compiled);
            }
          });
        });
      }

      var dispatchedWidgetIds = [];
      var setupWaiting = false;
      var debug = 1;

      var setupWidgets = function(element, widgetIds) {
        if (!setupWaiting) {
          if (_.intersection(widgetIds, dispatchedWidgetIds).length != widgetIds.length) {
            dispatchedWidgetIds = _.union(widgetIds, dispatchedWidgetIds);
            element.data('jarvisWidget') && element.data('jarvisWidget').destroy();
            element.jarvisWidgets(jarvisWidgetDefaults);
            initDropdowns(widgetIds);
          }
        } else {
          setupWaiting = true;
          $timeout(function() {
            setupWaiting = false;
            setupWidgets(element, widgetIds);
          }, 200);
        }
      }

      var destroyWidgets = function(element, widgetIds) {
        element.data('jarvisWidget') && element.data('jarvisWidget').destroy();
        dispatchedWidgetIds = _.xor(dispatchedWidgetIds, widgetIds);
      }

      var jarvisWidgetAddedOff, $viewContentLoadedOff, $stateChangeStartOff;

      return {
        restrict: 'A',
        compile: function(element) {
          element.removeAttr('widget-grid data-widget-grid');

          var widgetIds = [];

          $viewContentLoadedOff = $rootScope.$on('$viewContentLoaded', function(event, data) {
            $timeout(function() {
              setupWidgets(element, widgetIds);
            }, 100);
          });

          jarvisWidgetAddedOff = $rootScope.$on('jarvisWidgetAdded', function(event, widget) {

            if (widgetIds.indexOf(widget.attr('id')) == -1) {
              widgetIds.push(widget.attr('id'));
              $timeout(function() {
                setupWidgets(element, widgetIds);
              }, 100);
            }
          });

          $stateChangeStartOff = $rootScope.$on('$stateChangeStart',
            function(event, toState, toParams, fromState, fromParams) {
              jarvisWidgetAddedOff();
              $viewContentLoadedOff();
              $stateChangeStartOff();
              destroyWidgets(element, widgetIds);
            });
        }
      }

    }]
  );
})