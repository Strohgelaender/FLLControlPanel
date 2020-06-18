function updateH1(screen, jury) {
	$.ajax({
		method: "GET",
		url: "/screens/h1?screen=" + screen + "&jury=" + jury
	}).done(function(msg) {
		$('h1').text(msg);
	}).catch(function(error) {
		console.log(error);
	});
}

function updateH2(screen, jury) {
	$.ajax({
		method: "GET",
		url: "/screens/h2?screen=" + screen + "&jury=" + jury
	}).done(function(msg) {
		$('h2').text(msg);
	}).catch(function(error) {
		console.log(error);
	});
}