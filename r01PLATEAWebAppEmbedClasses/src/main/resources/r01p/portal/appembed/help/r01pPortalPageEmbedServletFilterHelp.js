// BEWARE double binding (see: https://www.gajotres.net/prevent-jquery-multiple-event-triggering/) 
$(document)
//	.on("pagebeforeshow","#r01pHelp",
//		function() {
//			$(document)
				.on("click","#btnRefreshMetrics",
					function(e) {
						if (e.handled == true) return;		// avoid double binding handling
						e.preventDefault();
						refreshMetrics(window.location.href);
					})
				.on("click","#btnRefreshReqLog",
					function(e) {
						if (e.handled == true) return;		// avoid double binding handling
						e.preventDefault();
						refreshReqLog(window.location.href);
					});
//		});
/////////////////////////////////////////////////////////////////////////////////////////
//	METRICS & LOG
/////////////////////////////////////////////////////////////////////////////////////////
function refreshMetrics(url) {
	$.ajax({
		url: url,
		data: $('#frmRefreshMetrics').serialize(),
		type: "GET",
		success: function(refreshResult) {
					console.log(refreshResult);
					$('#rawMetrics').html(refreshResult);
				 },
		  error: function (xhr,ajaxOptions,thrownError) {
					console.log(thrownError);
			     }
	});
}
function refreshReqLog(url) {
	$.ajax({
		url: url,
		data: $('#frmRefreshReqLog').serialize(),
		type: "GET",
		success: function(refreshResult) {
					console.log(refreshResult);
					$('#reqLogDetail').html(refreshResult);
				 },
		  error: function (xhr,ajaxOptions,thrownError) {
					console.log(thrownError);
			     }
	});
	$('#r01pClearReqLog').prop("checked",false);
}