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

// https://stackoverflow.com/a/10797177/854540
function setIntervalExact(func, interval) {
	// Check current time and calculate the delay until next interval
	const now = new Date();
	const delay = interval - now % interval;

	function start() {
		// Execute function now...
		func();
		// ... and every interval
		setInterval(func, interval);
	}

	// Delay execution until it's an even interval
	setTimeout(start, delay);
}