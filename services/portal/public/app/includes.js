define([
  //layout
  'layout/module',
  'layout/actions/minify-menu',
  'layout/actions/toggle-menu',
  'layout/actions/full-screen',
  'layout/actions/reset-widgets',

  'layout/directives/smart-context',
  'layout/directives/smart-include',
  'layout/directives/smart-menu',
  'layout/directives/search-autocomplete',
  'layout/directives/smart-router-animation-wrap',
  'layout/directives/smart-page-title',
  'layout/directives/smart-device-detect',
  'layout/directives/smart-fast-click',
  'layout/directives/smart-layout',
  'layout/directives/smart-fit-app-view',
  'layout/directives/state-breadcrumbs',
  'layout/directives/big-breadcrumbs',
  'layout/directives/href-void',
  'layout/directives/demo/demo-states',

  'layout/service/smart-css',
  
  'modules/widgets/directives/widget-grid',
  'modules/widgets/directives/jarvis-widget',

  //dashboard
  'dashboard/module',

  //graphs
  'modules/graphs/module',

  //widgets
  'modules/widgets/module',

  //forms
  'modules/forms/module',

  //account
  'auth/module',
  'auth/controllers/login-controller',
  'auth/services/authentication-service',
  'auth/models/User',

  //components

  //activities
  'components/activities/activities-controller',
  'components/activities/activities-service',
  'components/activities/activities-dropdown-toggle-directive',

  //calendar
  'components/calendar/module',
  'components/calendar/models/calendar-event',
  'components/calendar/directives/full-calendar',

  'components/shortcut/toggle-shortcut',

  //chat
  'components/chat/module',

  //language
  'components/language/Language',
  'components/language/language-selector',
  'components/language/language-controller',

  //projects
  'components/projects/Projects',
  'components/projects/recent-projects',

  //todo
  'components/todo/todo-container',
  'components/todo/models/todo',
  'components/todo/directives/todo-list',

  //inbox
  'components/inbox/module',
  'components/inbox/models/inbox-config',
  'components/inbox/models/inbox-message'

], function() {
  'use strict';
});