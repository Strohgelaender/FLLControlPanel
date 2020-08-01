//File from https://github.com/sim642/fll-timer/tree/master/public/js/timer.lib.js
let endtime = null;
let totaltime = null;
let stepper = null;
const defaulttime = (2 * 60 + 30) * 1000;

function displayTime(d, func) {
	let min = 0, sec = 0, ms = 0;

	if (d > 0) {
		const dd = d;

		ms = Math.floor(d % 1000 / 100);
		d = Math.floor(d / 1000);
		sec = d % 60;
		d = Math.floor(d / 60);
		min = d;

		d = dd;
	}

	$('#timer #min').text(min);
	$('#timer #sec').text((sec < 10 ? '0' : '') + sec);
	$('#timer #ms').text(ms);

	(func || function(){})(d);

	if (d <= 0) {
		clear();
	}
}

function startTimer(time, totalTime, func) {
	totaltime = totalTime;
	endtime = Date.now() + time;

	stepper = setInterval(function() {
		displayTime(endtime - Date.now(), func);
	}, 100);
}

function resetTimer(time, totalTime, func) {
	totaltime = totalTime;
	displayTime(time, func);

	clear();
}

function clear() {
	endtime = null;
	totaltime = null;
	clearInterval(stepper);
	stepper = null;
}