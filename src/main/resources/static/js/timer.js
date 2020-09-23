let stompClient = null;

const startColor = new Color('#0095b9');
const midColor = new Color('#a5ca18');
const endColor = new Color('#FF0000');

function tween(d) {
	const t = 1 - d / totaltime;

	let r, g, b;
	if (t < 0.5) {
		r = startColor.red() + 2 * t * (midColor.red() - startColor.red());
		g = startColor.green() + 2 * t * (midColor.green() - startColor.green());
		b = startColor.blue() + 2 * t * (midColor.blue() - startColor.blue());
	} else {
		r = midColor.red() + (t - 0.5) * 2 * (endColor.red() - midColor.red());
		g = midColor.green() + (t - 0.5) * 2 * (endColor.green() - midColor.green());
		b = midColor.blue() + (t - 0.5) * 2 * (endColor.blue() - midColor.blue());
	}

	$('#timer').css('color', `rgb(${Math.floor(r)}, ${Math.floor(g)}, ${Math.floor(b)})`);
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
