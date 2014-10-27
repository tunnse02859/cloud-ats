$(document).ready(function() {
  
  $("[rel=tooltip]").tooltip();
  
  $("body").on("click", "a.disabled", function() {
    return false;
  });
  
  $("body").on("click", "section.test-filter a.create-new-test", function() {
    $("#createNewTestDialog").modal();
  });
  
  $("#createNewTestDialog").on("hide", function() {
    $(this).find("form")[0].reset();
  });
  
  $("body").on("click", "form.upload .btn.start,.btn.save", function() {
    var action = $(this).attr("data-action");
    var form = $(this).closest("form");
    $(form).attr("action", action);
    $(form).submit();
  })
  
  //project plus
  $("body").on("click", ".table.project a.plus", function() {
    var div = $(this).closest("tr").next("tr").find("div");
    var icon = $(this).find("i.icon");
    
    if ($(div).css('display') == 'none') {
      $(div).slideDown(400);
      $(icon).removeClass("icon-plus");
      $(icon).addClass("icon-minus");
    } else {
      $(div).slideUp(400);
      $(icon).removeClass("icon-minus");
      $(icon).addClass("icon-plus");
    }
  });
  
  //Run project and snapshot by ajax
  $("body").on("click", ".table.project a.btn.run", function() {
    var href = $(this).attr("href");
    $(this).addClass("disabled");
    $.ajax({
      url: href,
      dataType: "html",
      success: function(data) {
        console.log("run a job successfully");
      },
      error: function(e) {
        console.log(e);
      }
    });
    return false;
  });
});