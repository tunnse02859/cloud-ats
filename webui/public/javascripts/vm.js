$(document).ready(function() {
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
    if (data.step === 2 && data.direction === 'next') {
      $('.cloud.btn.next').hide();
      $('.cloud.btn.finish').show();
    }
  });
  
  $('.cloud.wizard').on('changed', function (e, data) {
    var item = $('.cloud.wizard').wizard('selectedItem');
    if (item.step === 1) {
      $('.cloud.btn.prev').attr("disabled", "disabled");
    }
    if (item.step !== 3) {
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

  /* Validation
   ===================================================== */
  $("input,select,textarea").not("[type=submit]").jqBootstrapValidation();
  
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
  $("body").on("click", ".cloud-vm a#click-terminal", function() {
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
});