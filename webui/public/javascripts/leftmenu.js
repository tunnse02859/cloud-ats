$(document).ready(function() {
  $("#left-panel a").on("click", function() {
    var href = $(this).attr('href');
    var ajaxURL = $(this).attr("ajax-url");
    
    if (ajaxURL === undefined) {
      return true;
    }
    
    $("#left-panel li").removeClass('active')
    
    window.history.pushState('obj', 'newtitle', href);
    $(this).parent().addClass('active');
    
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