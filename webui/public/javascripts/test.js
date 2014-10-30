$(document).ready(function() {
  
  $("[rel=tooltip]").tooltip();
  $("body").on("click", "a.disabled", function(e) {
    e.preventDefault();
    e.cancelBubble = true;
    e.stopPropagation();
    return false;
  });
  
  $("body").on("click", "section.test-filter a.create-new-test", function() {
    $("#createNewTestDialog").modal();
  });
  
  //clear wizard form
  $("#createNewTestDialog").on("hide", function() {
    $(this).find("form")[0].reset();
  });
  
  //clear report
  $("body").on("click", "a.btn.report", function(ev) {
    ev.preventDefault();
    var target = $(this).attr("data-target");
    var modal = $(target);
    var remote = $(modal).attr("data-remote");
    $(modal).load(remote, function() {
      $(modal).modal('show');
    })
  });
  
  //show log
  $("body").on("click", "a.btn.console", function() {
    var target = $(this).attr("data-target");
    var div = $(target);
    if ($(div).css('display') == 'none') {
      $(div).slideDown(400);
    } else {
      $(div).slideUp(400);
    }
  });
  
  $("body").on("click", "form.upload .btn.start,.btn.save", function() {
    var action = $(this).attr("data-action");
    var form = $(this).closest("form");
    $(form).attr("action", action);
    $(form).submit();
  })
  
  //project plus
  $("body").on("click", ".table.project a.plus", function() {
    var div = $(this).closest("tr").next("tr").find("div.snapshots");
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