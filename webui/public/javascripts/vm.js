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
  
});