$(document).ready(function() {
  
  $("[rel=tooltip]").tooltip();
  
  /* Wizard
  ================================================== */
  $('.cloud.wizard').on('change', function (e, data) {
    if (data.direction === 'next') {
      $('.cloud.btn.prev').removeAttr('disabled');
    }
    if (data.direction === 'previous') {
      $('.cloud.btn.next').show();
      $('.cloud.btn.finish').hide();
    }
    if (data.step === 3 && data.direction === 'next') {
    	var container = $(".step-content #step4");
    	var checkedService =$('input[name=vmCloudService]:checked').val();
		if(checkedService=="apiCloudStack"){
		  var cloudstackApiUrl = $("input[name='cloudstack-api-url']").val();
	      var cloudstackApiKey = $("input[name='cloudstack-api-key']").val();
	      var cloudstackApiSecret = $("input[name='cloudstack-api-secret']").val();
	      var data = "cloudstack-api-url="+cloudstackApiUrl+"&cloudstack-api-key="+cloudstackApiKey+"&cloudstack-api-secret="+cloudstackApiSecret;	      
	      $.ajax({
	        url: "/portal/vm/so",
	        dataType: "html",
	        data: data,
	        async: false,
	        success: function(data) {
	          $(container).html(data);
	        }
	      });	    		
	      }else if(checkedService=="apiAzure"){	    	  
	    		
	      }else if(checkedService=="apiAmazon"){
	    		
	      }else{
    		
	      } 
	    $('.cloud.btn.next').hide();
	    $('.cloud.btn.finish').show();  
    }
  });
  
  $('.cloud.wizard').on('changed', function (e, data) {
    var item = $('.cloud.wizard').wizard('selectedItem');
    if (item.step === 1) {
      $('.cloud.btn.prev').attr("disabled", "disabled");
    }
    if (item.step !== 4) {
      $('.cloud.btn.next').show();
      $('.cloud.btn.finish').hide();
    }
  });
  
  $('.cloud.btn.finish').on('finished', function (e, data) {
  });
  
  $('.cloud.btn.prev').on('click', function () {
    var item = $('.cloud.wizard').wizard('selectedItem');
    if (item.step === 2) {
      $(this).attr("disabled", "disabled");
    }
    
    $('.cloud.wizard').wizard('previous');
  });
  
  $('.cloud.btn.next').on('click', function () {
    $('.cloud.wizard').wizard('next', 'foo');
  });  
  
  
  $('input[name=vmCloudService]:radio').on('change', function (e, data) { 	  
	  var activeDiv =$(this).val();	   
	  var apiDivContainer=$("#container_div_apivm");
	  $(".apivm").remove();
	  var data = "service="+activeDiv;	      
      $.ajax({
        url: "/portal/vm/vmapi",
        dataType: "html",
        data: data,
        async: false,
        success: function(data) {        	
          $(apiDivContainer).html(data);
        }
      });	  
  });
  

  /* Cloud VM list
   ===================================================== */
  $("body").on("click", ".cloud-vm a.plus", function() {
    var div = $(this).closest("tr").next("tr").find("div.vm-info");
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
  /* Click Terminal */
  $("body").on("click", ".cloud-vm a.tab-terminal", function() {
    var ajaxURL = $(this).attr("ajax-url");
    var terminal = $(this).closest("div.vm-info").find("div.vm-terminal");
    if ($(terminal).find("iframe").length == 0) {
      $.ajax({
        url: ajaxURL,
        dataType: "html",
        success: function(data) {
          $(terminal).html(data);
        },
        error: function(e, data) {
          console.log(e);
          console.log(data);
        }
      });
    }
  });
  
  /* Click VM Properties Update button*/
  $("body").on("click", ".cloud-vm button.update", function() {
    var data = $(this).attr("data");
    var form = $("#" + data);
    
    $(form).find("input").show();
    $(form).find("span.label").hide();
    
    $(this).hide();
    $(form).find("button.submit").show();
    $(form).find("button.cancel").show();
    
    $(form).find("input[type=password]").val("");
  });
  
  /* Click VM Properties Cancel button */
  $("body").on("click", ".cloud-vm button.cancel", function() {
    var data = $(this).attr("data");
    var form = $("#" + data);
    
    $(form).find("input").hide();
    $(form).find("span.label").show();
    
    $(this).hide();
    $(form).find("button.submit").hide();
    $(form).find("button.update").show();
    
    $(form).find("span.label").each(function() {
      $(this).next("input").val($(this).text());
    });
  })
  
  /* Click VM Properties Submit Button */
  $("body").on("click", ".cloud-vm button.submit", function(e) {
    var ajaxURL = $(this).attr("ajax-url");
    var form = $("#" + $(this).attr("data"));
    var vmId = $(form).find("input[name='vmId']").val();
    $.ajax({
      url: ajaxURL,
      method: "POST",
      dataType: "json",
      data: $(form).serialize(),
      success: function(data) {
        $("tr.vm-status-" + vmId).replaceWith(data.vmStatus);
        $("tr.vm-properties-" + vmId).replaceWith(data.vmProperties);
      }
    });
  });
  
  /* start vm */
  $("body").on("click", ".cloud-vm .vm-list .btn.stop,.btn.start,.btn.restore, .btn.destroy", function() {
    var href = $(this).attr("href");
    var destroy = $(this).hasClass("destroy");
    var id = $(this).attr("data-target");
    var status = $(".vm-status-" + id);
    var properties = $(".vm-properties-" + id);
    $("#pleaseWaitDialog").modal();
    $.ajax({
      url: href,
      dataType: "html",
      success: function(data) {
        console.log("success");
        if (destroy) {
          $(status).remove();
          $(properties).remove();
        }
        $("#pleaseWaitDialog").modal('hide');
      },
      error: function(error) {
        console.log(error);
        $("#pleaseWaitDialog").modal('hide');
      }
    });
    return false;
  });
  
  /* create vm */
  $("body").on("click", ".cloud-vm a.create-vm", function() {
    var href = $(this).attr("href");
    var table = $(".cloud-vm .vm-list")
    var button = $(this);
    $(button).addClass("disabled");
    $("#pleaseWaitDialog").modal();
    $.ajax({
      url: href,
      dataType: "html",
      success: function(data) {
        $(button).removeClass("disabled");
        $(table).append(data);
        $("#pleaseWaitDialog").modal('hide');
      }
    })
    return false;
  });
  
  /* save offering */
  $("body").on("click", ".cloud-offering a.save-offering", function() {
    console.log(this);
    var href = $(this).attr("href");
    var form = $(this).parent().parent();
    var formData = $(form).serialize();
    $.ajax({
      url: href,
      method: "POST",
      data: formData,
      dataType: "html",
      success: function(data) {
        location.reload();
      }
    });
    return false;
  });
  
  /* Validation
  =====================================================
  $("input,select,textarea").not("[type=submit]").jqBootstrapValidation({
   submitSuccess: function(form, event) {
     if ($(form).hasClass("vm-properties")) {
       event.stopPropagation();
       event.preventDefault();
       var action = $(form).attr("action");
       var parent = $(form).parent();
       $.ajax({
         url: action,
         method: "POST",
         dataType: "html",
         async: false,
         data: $(form).serialize(),
         success: function(data) {
           $(parent).html(data);
         }
       });
     }
   }
 });
  */
});