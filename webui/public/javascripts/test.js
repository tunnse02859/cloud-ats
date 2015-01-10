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
          console.log("run a job successfully");
        },
        error: function(e) {
          console.log(e);
        }
      });
    }
    
    $(this).addClass("disabled");
    console.log("has just click run button");
    e.preventDefault();
  });
  
  //Click create project finish
  $("body").on("click", ".project.finish", function(e) {
    console.log('test');
    var projectName = $('input[name=name]').val();
    var file = $('input[name=uploaded]').val();
   // var projectNameWizard = $('input[name=test-name]').val();
    
    if(projectName && file && !projectNameWizard){
      $("#pleaseWaitDialog").modal();
    }
  });
  
  //when user press enter , click search button
  $("body").on("keypress", ".test-filter .form-search input.name", function(e) {
    if (e.which == 13) {
      $(this).parent().find('a.filter').click();
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
    var form = $(this).parent('form');
    var values = form.serialize();
    if(projectName){
      $.ajax({
        method: "GET",
        url: ajaxURL,
        data: values,
        dataType: "json",
        success: function(data) {
          if(data.length==0){
            $('.project tbody').find('tr:not(:first)').hide();
          }
          var arr = [];
          $(data).each(function(){
            var projectId = ('project-'+this.id);
            
            arr.push(projectId);
          });
          var arrElement = [];
          $('.project tbody').find('tr').each(function(){
            
            arrElement.push($(this).attr('class'));
           
          });
          var total = [];
          for(var i = 0; i < arr.length; i ++){
           
            var count = 0;
            for(var j = 1; j < arrElement.length; j ++ ){
              $('.'+arrElement[j]).hide();
              if(arr[i] === arrElement[j]){
                total.push(arr[i]);
                
              }
              
            }
            
          }
          for(var k = 0; k < total.length; k ++){
            $('.'+total[k]).show();
          }
         
        },
        error: function() {
          location.reload();
        }
      });
    }
    else {
      $('.project tbody').find('tr').show();
    }
  });
});