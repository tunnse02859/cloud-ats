$(document).ready(function() {
  $("#left-panel a").on("click", function() {
    $("#left-panel li").removeClass('active')
    var href = $(this).attr('href');
    window.history.pushState('obj', 'newtitle', href);
    $(this).parent().addClass('active');
    
    var ajaxURL = $(this).attr("ajax-url");
    $.ajax({
      method: "GET",
      url: ajaxURL,
      dataType: "html",
      success: function(data) {
        $("#main.container").html(data);
      },
      error: function() {
        location.reload();
      }
    });
    return false;
  });
  
});