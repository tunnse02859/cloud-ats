var updateByAjax = function(data) {
  $(".org-breadcrumb .breadcrumb").html(data.breadcrumb);
  $("#main-navbar .current-group").html(data.navbar);
  $(".org-body .org-right").html(data.body);
  $(".org-body .org-left").html(data.leftmenu);
  
  $.ajax({
    method: "GET",
    url: "/portal/o/f/u/Organization",
    dataType: "html",
    success: function(data) {
      $("#left-panel").replaceWith(data);
    },
    error: function() {
      location.reload();
    }
  })
}

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
        /*$(".org-body .org-right table.org-user tr.user").hide();
        $(data.users).each(function() {
          $("#user-" + this).show();
        });*/
        updateByAjax(data);
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
  
  $("#main").on("click", ".pagination ul li a.pageNumberFilterUser", function() {
    var activePage = $(this).parent().parent().find('li.active a.pageNumberFilterUser').text();
    var current = $(this).text();
    
    if(activePage == current){
     
      return false;
    }
    
    
    var ajaxURL = $(this).attr("ajax-url");
  
    var form = $(this).parent("form");
    form.find(":input").each(function(){
      if (this.value == '') this.disabled = true;
    })
    var email = $('form').find("input[name=email]").val();
    
    var values = form.serialize();
    form.find(":input:disabled").prop('disabled',false);
    $.ajax({
        type: "GET",
        url: ajaxURL+"&email="+email,
        dataType: "json",
        success: function (data) {
          updateByAjax(data);
        },
        error: function() {
          location.reload();
        }
    });
    
  });
});