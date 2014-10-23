$(document).ready(function() {
  $("body").on("click", "section.test-filter a.create-new-test", function() {
    $("#createNewTestDialog").modal();
  })
  
  $("#createNewTestDialog").on("hide", function() {
    $(this).find("form")[0].reset();
  })
});