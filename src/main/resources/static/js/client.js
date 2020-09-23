let contentClient = null;

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

function setupHeadlineUpdate(screen, jury) {
	updateH1(screen, jury);
	updateH2(screen, jury);
	setInterval(function() {
		updateH1(screen, jury);
		updateH2(screen, jury);
	}, 100000000);
}

function connectContent() {
	const socket = new SockJS('/content');
	contentClient = Stomp.over(socket);
	contentClient.connect({}, function(frame) {
		console.log('Connected: ' + frame);
		contentClient.subscribe('/topic/content', function(obj) {
			const msg = JSON.parse(obj.body);
			console.log(msg);
			if (msg === 'ShowWelcome' && !isWelcomeSite()) {
				window.location.href = "/welcome";
			} else if (msg === 'ShowBye' && !isByeSite())
				window.location.href = "/bye";
			else if (msg === 'ShowNormal' && isGreetingSite())
				window.history.back();
			else if (msg === 'RefreshConfig')
				location.reload();
			//TODO refresh Content
			else if (msg === 'RefreshContent')
				location.reload(); //TODO
		});
		contentClient.send("/content", {});
	}, function(e) {
		console.error(e, "Reconnecting WS");
		setTimeout(connectContent, 2500);
	});
}

let urlParams;

function getQueryParam(param) {
	if (!urlParams) {
		urlParams = new URLSearchParams(window.location.search);
	}
	if (urlParams.has(param)) {
		return urlParams.get(param);
	}
}

function isGreetingSite() {
	return isWelcomeSite() || isByeSite();
}

function isWelcomeSite() {
	return window.location.href.endsWith("welcome");
}

function isByeSite() {
	return window.location.href.endsWith("bye");
}

$(document).ready(() => {
	connectContent();

	if (getQueryParam('streaming')) {
		$('html').css('background', 'transparent');
	}
});


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