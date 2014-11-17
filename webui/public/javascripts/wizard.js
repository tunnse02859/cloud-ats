$(document).ready(function(){
  
  //Click Use wizard
  $("body").on("click", ".btn.use-wizard", function() {
    var form = $(".project-wizard-block form");
    var href = $(this).attr("data-target") + "?" + $(form).serialize();
    window.location = href;
  });
  
  //Form summit
  $(".performance form").submit(function(event) {
    var samplerNames = $(this).find("input.sampler-name");
    $(samplerNames).each(function(index) {
      
      var sampler = $(this).closest("tr.sampler");
      $(this).attr("name", "sampler[" + index + "]");
      
      var method = $(sampler).find("select.sampler-method");
      $(method).attr("name", "sampler-method[" + index + "]");
      
      var url = $(sampler).find("input.sampler-url");
      $(url).attr("name", "sampler-url[" + index + "]");
      
      var assertionText = $(sampler).find("input.sampler-assertion-text");
      $(assertionText).attr("name", "sampler-assertion-text[" + index + "]");
      
      var constantTime = $(sampler).find("select.sampler-constant-time");
      $(constantTime).attr("name", "sampler-constant-time[" + index + "]");

      var prefix = "sampler[" + index + "]";
      var params = $(sampler).next("tr.sampler-params");
      
      var paramNames = $(params).find("input.sampler-param-name");
      
      $(paramNames).each(function(index) {
        $(this).attr("name", prefix + "-param-name[" + index + "]");
      });

      var paramValues = $(params).find("input.sampler-param-value");
      $(paramValues).each(function(index) {
        $(this).attr("name", prefix + "-param-value[" + index + "]");
      }); 
      
    });
    
  });
//  $("body").on("click", ".performance.finish", function(event) {
//    var form = $(".performance form");
//    var samplerNames = $(this).find("input.sampler-name");
//    
//    $(samplerNames).each(function(index) {
//      console.log(index);
//    });
//    
//    event.preventDefault();
//    return false;
//  })
  
  //Click show parameter block
  $("body").on("click", ".performance .show-param", function() {
    var parent = $(this).parent();
    $(this).remove();
    $(parent).append($("#param-block-tmpl").html());
  });
  
  //Click remove parameter block
  $("body").on("click", ".performance .param-block .remove", function() {
    var block = $(this).parent();
    var td = $(block).parent();
    
    var paramSize = $(td).find(".param-block").length;
    if (paramSize == 1) {
      $(td).append($("#show-param-tmpl").html());
    }
    
    $(block).remove();
  });
  
  //Click add parameter block
  $("body").on("click", ".performance .param-block .add", function() {
    var block = $(this).parent();
    var td = $(block).parent();
    td.find(".param-block").last().after($("#param-block-tmpl").html());
  });
  
  //Click add sampler block
  $("body").on("click", ".performance .sampler .btn.add-sampler", function() {
    var table = $(this).closest("table");
    var tr = $(table).find("tr.sampler-params").last();
    $(tr).after($("#sampler-block-tmpl").html());
  });
  
  //Click remove sampler block
  $("body").on("click", ".performance .sampler .btn.remove-sampler", function() {
    var sampler = $(this).closest("tr.sampler");
    var params = sampler.next("tr.sampler-params");
    $(sampler).remove();
    $(params).remove();
  });
  
  $('.slider').slider({tooltip: 'never'});
  $("#users").on("slide", function(slideEvt) {
    $("#usersSliderVal").text(slideEvt.value);
  });
  $("#rampup").on("slide", function(slideEvt) {
    $("#rampupSliderVal").text(slideEvt.value);
  });
  $("#loops").on("slide", function(slideEvt) {
    $("#loopsSliderVal").text(slideEvt.value);
  });
  $("#duration").on("slide", function(slideEvt) {
    $("#durationSliderVal").text(slideEvt.value);
  });

  /* Wizard
  ================================================== */
  $('.performance .wizard').on('change', function (e, data) {
    if (data.direction === 'next') {
      $('.performance .btn.prev').removeAttr('disabled');
    }
    if (data.direction === 'previous') {
      $('.performance .btn.next').show();
      $('.performance .btn.prev').attr("disabled", "disabled");
      $('.performance .btn.finish').hide();
    }

    if (data.step === 1 && data.direction === 'next') {
      $('.performance .btn.next').hide();
      $('.performance .btn.finish').show();
    }
  });

  $('.performance .btn.prev').on('click', function () {
    var item = $('.performance .wizard').wizard('selectedItem');
    if (item.step === 1) {
      $(this).attr("disabled", "disabled");
    }
    
    $('.performance .wizard').wizard('previous');
  });
  
  $('.performance .btn.next').on('click', function () {
    $('.performance .wizard').wizard('next', 'foo');
  });

  $('.performance .wizard').on('changed', function (e, data) {
    var item = $('.performance .wizard').wizard('selectedItem');
    if (item.step === 1) {
      $('.performance .btn.prev').attr("disabled", "disabled");
      $('.performance .btn.next').show();
      $('.performance .btn.finish').hide();
    }
  });
});