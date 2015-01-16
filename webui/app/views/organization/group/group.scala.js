@import controllers.organization._

$(document).ready(function(){
  
  window.onpopstate = function(event) {
    if(event && event.state) {
      location.reload();
    }
  };
  var len = $(".pagination ul").find('li').length;
  if(len==3){
    $('.pagination ul li:last-child').addClass("disabled");
  }
  $(".pagination ul li:nth-child(2)").addClass("active");
  $('.pagination ul li:first-child').addClass("disabled");
  
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
        updateByAjax(data);
        $('.pagination ul li:nth-child(2)').addClass('active');
      },
      error: function() {
        location.reload();
      }
    });
    return false;
  });
  
  
  //Link on table and breadcrumb
  $("#main").on("click", ".org-body .org-right table.org-group td.group-name a,.org-breadcrumb a, tr.group td.group-ancestor a, tr.user td.user-role a", function() {
    
    var href = $(this).attr('href');
    window.history.pushState('obj', 'newtitle', href);
    
    var ajaxURL = $(this).attr("ajax-url");
    $.ajax({
      method: "GET",
      url: ajaxURL,
      dataType: "json",
      success: function(data) {
        updateByAjax(data);
      },
      error: function() {
        location.reload();
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
    
    //$(hiden_name).val(name);
    
    $.ajax({
      method: "GET",
      url: ajaxURL,
      data: values,
      dataType: "json",
      success: function(data) {
        updateByAjax(data);
        
        var hidden_name = $(".pagination input:hidden[name=hidden_name]");
        $(hidden_name).val(data.name);
        var pagination = $(".pagination ul li");
        var len = $(pagination).length;
        if(len==2){
          $(pagination).addClass("disabled");
        }
        else {
          $(".pagination ul li:nth-child(2)").addClass("active");
        }
      },
      error: function() {
        location.reload();
      }
    });
  });
  
  $("#main").on("keypress", ".org-group-filter .form-search input", function(e) {
    if (e.which == 13) {
      $(this).parent().find("a.filter").click();
    }
  });
  
  $("#main").on("click", ".org-group-filter a.creategroup", function() {
    
    var href = $(this).attr('href');
    window.history.pushState('obj', 'newtitle', href);
    
    var ajaxURL = $(this).attr("ajax-url");
    $.ajax({
      method: "GET",
      url: ajaxURL,
      dataType: "html",
      success: function(data) {
        $(".org-body .org-right").html(data);
      },
      error: function() {
        location.reload();
      }
    });
    return false;
  });
  
  $("#main").on("submit", ".org-group-new", function() {
    return false;
  });
  
  $("#main").on("click", ".org-group-new input.create", function() {
  
     var name = $('input[name=name]').val();
     var desc = $('textarea[name=desc]').val();
     
     if(name != null  && name != ''){
       var ajaxURL = $(this).attr("ajax-url");
       var form = $("form.org-group-new");
       var disabled = form.find(':input:disabled').removeAttr('disabled');
       var formValue = $(form).serialize();
       disabled.attr('disabled','disabled');
       
       $.ajax({
         method: "GET",
         url: ajaxURL,
         dataType: "json",
         data: formValue,
         success: function(data){
           updateByAjax(data);
         },
         error: function() {
           location.reload();
         }
       })
     }
  });
  
  // click button previous
  $("#main").on("click", ".pagination ul li a.prev", function () {
    var current = $(this).parent().parent().find(".active");
    var currentText = $(current).text();
    if(currentText == 1 ){
      return false;
    }
    $(current).prev().find('a').click();
    
   // return false;
  });
  
  // click button next
 $("#main").on("click", ".pagination ul li a.next", function () {
    var current = $(this).parent().parent().find(".active");
    var currentText = $(current).text();
    var lastPage =  $(this).parent().parent().find('li').length;
    
    if(currentText == lastPage-2 || lastPage == 3){
      return false;
    }
    $(current).next().find('a').click();
    // return false;
  });
 
 // click page number
  $("#main").on("click", ".pagination ul li a.pageNumber", function (e) {
    var length = $(this).parent().parent().find('li').length;
    var activePage = $(this).parent().parent().find('li.active a.pageNumber').text();
    var current = $(e.target).text();
    if(activePage != current){
      $(this).parent().parent().find('li').removeClass('active');
    }
    else {
      return false;
    }
    $(this).parent().addClass("active");
    var ajaxURL = $(this).attr("ajax-url");
    var id = $(this).attr("id");
    
    if(current == length-2){
      $('.pagination ul li:last-child').addClass("disabled");
    }
    else {
      $('.pagination ul li:last-child').removeClass("disabled");
    }
    if(current == 1){
      $('.pagination ul li:first-child').addClass("disabled");
    }
    else {
      $('.pagination ul li:first-child').removeClass("disabled");
    }
    if (current == activePage) {
      return false;
    }
    paginationData(id,current,ajaxURL);
   
  });
  
  // function to send ajax request when user click page number
  var paginationData = function (id, current, ajaxURL) {
    
    $.ajax({
        
        method: "GET",
        url: ajaxURL,
        dataType: "json",
        data: {"id" : id ,"current" : current},
        success: function(data) {
          var oldElement = $('.table tbody').find('tr:not(:first)');
          $(oldElement).remove();
          
          $(data).each(function() {
            var html = 
            "<tr class='group' id='group-"+this.id+"'>"+
              "<td class='group-name'>"+
                  "<a rel='tooltip' href='@routes.Organization.index()?nav=group&group="+this.id+"'"+
                  "ajax-url='@routes.Organization.body()?nav=group&group="+this.id+"'>"+this.name+"</a>" +
              "</td>"+
              
              "<td class='group-ancestor'>/ <a href='@routes.Organization.index()?nav=group&group="+id+"'"+
              "ajax-url='@routes.Organization.body()?nav=group&group="+id+"'>"+this.ancensor+"</a></td>"+
              "<td><span class='badge badge-primary'>"+this.childrenSize+"</span></td>"+
              "<td><span class='badge badge-cyan'>"+this.level+"</span></td>"+
              "<td><span class='badge badge-pink'>"+this.userSize+"</span></td>"+
              "<td><span class='badge badge-pink'>"+this.roleSize+"</span></td>"+
              "<td><span class='badge badge-pink'>"+this.featureSize+"</span></td>"+
              "<td>"+
                
                "<a href='@routes.GroupAction.editGroup("forgroup")?group=" +this.id +"' class='btn btn-mini btn-blue'>Update</a> "+
                "<a href='@routes.GroupAction.deleteGroup("forgroup")?group="+this.id+"' class='btn btn-mini btn-red'>Delete</a>"+
                
              "</td>"+
             "</tr>";
            
            var currentElement = $('.table tbody').append(html);
          });
        },error: function () {
          
          location.reload();
        }
     });
   }
  
  // function to handle click number page after filter
   $("#main").on("click",".pagination ul li a.pageNumberFilter", function () {
     var activePage = $(this).parent().parent().find('li.active a.pageNumberFilter').text();
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
     var level = $('form').find("input[name=level]").val();
     var values = form.serialize();
     form.find(":input:disabled").prop('disabled',false);
     
     var text = $(form).find(":input[value!='']").serialize();
     $.ajax({
       method: "GET",
       url: ajaxURL+"&name="+name+"&level="+level+"",
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
});