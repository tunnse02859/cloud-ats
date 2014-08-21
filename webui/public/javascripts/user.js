$(document).ready(function() {
  $("#main").on("click", ".org-user-filter .form-search a.filter", function() {
    var ajaxURL = $(this).attr("ajax-url");
    
    var form = $(this).parent("form");
    form.find(":input").each(function(){
      if (this.value == '') this.disabled = true;
    })
    
    var values = form.serialize();
    form.find(":input:disabled").prop('disabled',false);
    
    var text = $(form).find(":input[value!='']").serialize();
    $.ajax({
      method: "GET",
      url: ajaxURL,
      data: values,
      dataType: "json",
      success: function(data) {
        $(".org-body .org-right table.org-user tr.user").hide();
        $(data.users).each(function() {
          $("#user-" + this).show();
        });
      },
      error: function() {
        location.reload();
      }
    });
  });
  
  $("#main").on("keypress", ".org-user-filter .form-search input", function(e) {
    if (e.which == 13) {
      $(this).parent().find("a.filter").click();
    }
  });
  
  $("#main").on("submit", ".org-user-filter .form-search", function() {
    return false;
  });
  
  $("#main").on("submit", ".org-right form", function() {
    var disabled = $(this).find(':input:disabled').removeAttr('disabled');
  });
});