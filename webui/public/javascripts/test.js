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
  $("body").on("click", ".table.project a.btn.run", function(e) {
    
    $("pre.pre-scrollable").text("");
    var href = $(this).attr("href");

    //prevent click many times on disabled button
    if (!$(this).hasClass("disabled")) {
      $.ajax({
        url: href,
        dataType: "html",
        async: true,
        success: function(data) {
        },
        error: function(e) {
        }
      });
    }
    
    $(this).addClass("disabled");
    e.preventDefault();
  });
  
  //Click create project finish
  $("body").on("click", ".project.finish", function(e) {
    var projectName = $('input[name=name]').val();
    var file = $('input[name=uploaded]').val();
    if(projectName && file){
      $("#pleaseWaitDialog").modal();
    }
  });
  
  //when user press enter , click search button
  $("body").on("keypress", ".test-filter .form-search input.name", function(e) {
    if ($(this).val() && e.which == 13) {
      $(this).parent().find('a.filter').click();
    } else if (! $(this).val() && e.which == 13) {
      var href = $(this).parent().find("a.filter").attr('ajax-url');
      window.location.href= href;
    }
  });
  
  //prevent submit form search
  $('body').on('submit',".form-search",function() {
    return false;
  });
  
  //send ajax request with data in form search
  $('body').on('click','.form-search a.filter', function() {
    
    var projectName = $('input[name=name]').val();
    var ajaxURL = $(this).parent().find('a.filter').attr("ajax-url");
    if(projectName){
      window.location= ajaxURL+"?name="+projectName;
      
    } else {
      //$('.project tbody').find('tr').show();
    }
  });
  $("body").on("click",".pagination.project ul li a.pageNumberProject", function() {
   
    var length = $(this).parent().parent().find('li').length;
    var activePage = $(this).parent().parent().find('li.active a.pageNumberProject').text();
    var current = $(this).text();
    if(activePage == current){
      return false;
    }
    var href = $(this).attr("ajax-url");
    window.location.href = href;
   
  });
  
  $("body").on("click",".pagination.project ul li a.pageNumberProjectFilter", function () {
    var href = $(this).attr("ajax-url");
    var name = $("form").find("input[name=name]").val();
    var link;
    if(name){
      link = href+"&name="+name;
      
    } else {
      link = href;
    } 
    window.location.href= link;
  });
  // click button previous
  $("#main").on("click", ".pagination.project ul li a.prev", function () {
    
    
    var current = $(this).parent().parent().find(".active");
    var currentText = $(current).text();
    if(currentText == 1 ){
      return false;
    }
    $(current).prev().find('a').click();
    
  });
  
  // click button next
 $("#main").on("click", ".pagination.project ul li a.next", function () {
    var current = $(this).parent().parent().find(".active");
    var currentText = $(current).text();
    var lastPage =  $(this).parent().parent().find('li').length;
    
    if(currentText == lastPage-2 || lastPage == 3){
      return false;
    }
    
    $(current).next().find('a').click();
    
  });
 
});