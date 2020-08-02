let stompClient = null;

//tween function from https://github.com/sim642/fll-timer/tree/master/public/js/timer.js
function tween(d) {
	const t = 1 - d / totaltime;

	let r, g;
	if (t < 0.5) {
		g = 1.0;
		r = 2 * t;
	} else {
		r = 1.0;
		g = 1 - 2 * (t - 0.5);
	}

	$('#timer').css('color', 'rgb(' + Math.floor(256 * r) + ', ' + Math.floor(256 * g) + ', 0)');
}

function connect() {
	const socket = new SockJS('/timer');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		console.log('Connected: ' + frame);
		stompClient.subscribe('/topic/timer', function(obj) {
			const msg = JSON.parse(obj.body);
			console.log(msg);
			resetTimer(msg.time, msg.time, tween);

			if (msg.game)
				$('#timer').removeClass('nogame');
			else
				$('#timer').addClass('nogame');

			if (msg.start)
				startTimer(msg.time, msg.time, tween);
		});
	}, function(e) {
		console.error(e, "Reconnecting WS");
		setTimeout(connect, 2500);
	});
}

function updateClocktime() {
	const time = new Date();
	const hr = time.getHours();
	const min = time.getMinutes();
	$('#clocktime').text((hr < 10 ? '0' : '') + hr + ':' + (min < 10 ? '0' : '') + min);
}

$(function() {
	console.log('start Setup Method');
	resetTimer(defaulttime, defaulttime, tween);
	connect();

	setupHeadlineUpdate(window.location.href.endsWith("timer") ? 'timer' : 'clock');

	updateClocktime();
	setIntervalExact(updateClocktime, 60 * 1000);

	console.log('startup finnish')
});
