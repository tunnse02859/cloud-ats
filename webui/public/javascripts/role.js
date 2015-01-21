function checkCheckbox(){
  var countCheckbox = $("td :checkbox:checked").length;
  var name = $('input[name=name]').val();
  if(name != null && name != '' && countCheckbox ===0){
    $('.alert').show();
    return false;
  }
  return true;
}
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
  
  // click filter link
  $("#main").on("click", ".org-role-filter .form-search a.filter", function() {
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
        updateByAjax(data);
       /* $(".org-body .org-right table.org-role tr.role").hide();
        $(data.roles).each(function() {
          $(".role-" + this).show();
        });*/
      },
      error: function() {
        location.reload();
      }
    });
  });
  
  // if press enter button after input name , click filter link button
  $("#main").on("keypress", ".org-role-filter .form-search input", function(e) {
    if (e.which == 13) {
      $(this).parent().find("a.filter").click();
    }
  });
  
  $("#main").on("keypress", ".org-right .form-horizontal input", function(e) {
    if (e.which == 13) {
      return false;
    }
  });
  
  $("#main").on("submit", ".org-role-filter .form-search", function() {
    return false;
  });
  $('#main').on('click','.form-actions .btn', function () {
    
  });
  
  // click page number
  $("#main").on("click",".pagination ul li a.pageNumberFilterRole", function () {
    var activePage = $(this).parent().parent().find('li.active a.pageNumberFilterRole').text();
    var current = $(this).text();
    
    if(activePage == current){
     
      return false;
    }
    
    
    var ajaxURL = $(this).attr("ajax-url");
  
    var form = $(this).parent("form");
    form.find(":input").each(function(){
      if (this.value == '') this.disabled = true;
    })
    var name = $('form').find("input[name=name]").val();
    
    var values = form.serialize();
    form.find(":input:disabled").prop('disabled',false);
    
    var text = $(form).find(":input[value!='']").serialize();
    $.ajax({
      method: "GET",
      url: ajaxURL+"&name="+name,
      data: values,
      dataType: "json",
      success: function(data) {
    
        updateByAjax(data);
        
      },
      error: function() {
        location.reload();
      }
    });
  });
  
})