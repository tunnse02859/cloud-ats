$(document).ready(function() {
  
  // left menu click.
	$("#left-panel #left-panel-content ul li").on("click", function() {
	  //clear active
	  $("#left-panel #left-panel-content ul li").removeClass("active");
	  $("ul.lp-dropdown-menu li").removeClass("active");
	  
	  $(this).addClass("active");
	});
	
	// lef sub-menu click.
	$("ul.lp-dropdown-menu li").on("click", function() {
	  
	  //clear active
	  $("ul.lp-dropdown-menu li").removeClass("active");
	  $("#left-panel #left-panel-content ul li").removeClass("active");
	  
	  var owner = $(this).parent().attr("data-dropdown-owner");
	  var wrapper = $("div.lp-dropdown-wrapper[data-dropdown-owner='" +owner+ "']");
	  
	  $("#" + owner).removeClass("open");
	  owner = $("#" + owner).parent();
	  owner.addClass("active")
	  $(this).addClass("active");
	  wrapper.removeClass("open");
	});
	
});