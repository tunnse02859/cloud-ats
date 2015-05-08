define(['layout/module'], function(module) {
  "use strict";

  var availabelTags = [
          "ActionScript",
          "AppleScript",
          "Asp",
          "BASIC",
          "C",
          "C++",
          "Clojure",
          "COBOL",
          "ColdFusion",
          "Erlang",
          "Fortran",
          "Groovy",
          "Haskell",
          "Java",
          "JavaScript",
          "Lisp",
          "Perl",
          "PHP",
          "Python",
          "Ruby",
          "Scala",
          "Scheme"];

  module.registerDirective('searchAutocomplete', function() {
    return {
      restrict: 'A',
      link: function(scope, element, attr) {
        var $element = $(element);
        $element.find('>input[type="text"]').autocomplete({
          source: availabelTags
        })
      }
    }
  });  
});