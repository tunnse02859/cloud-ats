define(['layout/module', 'lodash'], function(module, _) {
  "use strict";

  module.registerFactory('SmartCss', ['$rootScope', '$timeout', function($rootScope, $timeout) {
    var sheet = (function(){
      //Create the <style> tag
      var style = document.createElement('style');

      //WebKit hack
      style.appendChild(document.createTextNode(""));

      //Add style element to the page
      document.head.appendChild(style);

      return style.sheet;
    })();

    var _styles = {};

    var SmartCss = {
      writeRule: function(selector) {
        SmartCss.deleteRuleFor(selector);
        if (_.has(_styles, selector)) {
          var css = selector + '{ ' + _.map(_styles[selector], function(value, key) {
            return key + ':' + value + ';'
          }).join(' ') + '}';

          //workaround for firefox
          var isFirefox = typeof InstallTrigger !== 'undefined';

          if (isFirefox) {
            sheet.insertRule(css, sheet.cssRules.length);
          } else {
            sheet.insertRule(css);
          }
        }
      },
      add: function(selector, property, value, delay) {
        if (!_.has(_styles, selector)) {
          _styles[selector] = {};
        }

        if (value == undefined || value == null || value == '') {
          delete _styles[selector][property];
        } else {
          _styles[selector][property] = value;
        }

        if(_.keys(_styles[selector]).length == 0) {
          delete _styles[selector];
        }

        if (!delay) {
          delay = 0;
        }

        $timeout(function() {
          SmartCss.writeRule(selector);
        }, delay); 
      },
      remove: function(selector, property, delay) {
        SmartCss.add(selector, property, null, delay);
      },
      deleteRuleFor: function(selector) {
        _(sheet.rules).forEach(function(rule, idx) {
          if (rule.selectorText == selector) {
            sheet.deleteRule(idx);
          }
        });
      },
      appViewSize: null
    };

    $rootScope.$on('$smartContentResize', function(event, data) {
      SmartCss.appViewSize = data;
    });

    return SmartCss;
  }]);
});