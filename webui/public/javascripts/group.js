$(document).ready(function(){
  //Link on left-menu
  $("#main").on("click", ".org-body .org-left a", function() {
    $(".org-body .org-left li").removeClass('active');
    $(".org-body .org-left span.badge").removeClass("badge-primary")
    
    var href = $(this).attr('href');
    window.history.pushState('obj', 'newtitle', href);
    $(this).parent().addClass('active');
    $(this).find('span.badge').addClass('badge-primary');
    
    var ajaxURL = $(this).attr("ajax-url");
    $.ajax({
      method: "GET",
      url: ajaxURL,
      dataType: "json",
      success: function(data) {
        $(".org-breadcrumb .breadcrumb").html(data.breadcrumb);
        $(".org-body .org-right").html(data.body);
        $(".org-body .org-left").html(data.leftmenu);
      }
    });
    return false;
  });
  
  
  //Link on table and breadcrumb
  $("#main").on("click", ".org-body .org-right table.org-group td.group-name a,.org-breadcrumb a", function() {
    
    var href = $(this).attr('href');
    window.history.pushState('obj', 'newtitle', href);
    
    var ajaxURL = $(this).attr("ajax-url");
    $.ajax({
      method: "GET",
      url: ajaxURL,
      dataType: "json",
      success: function(data) {
        $(".org-breadcrumb .breadcrumb").html(data.breadcrumb);
        $(".org-body .org-right").html(data.body);
        $(".org-body .org-left").html(data.leftmenu);
      }
    });
    return false;
  });
  
  $("#main").on("click", ".org-group-filter .form-search a.filter", function() {
    
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
        $(".org-body .org-right table.org-group tr.group").hide();
        $(data.groups).each(function() {
          $("#group-" + this).show();
        });
      }
    });
  });
});