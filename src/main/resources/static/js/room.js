function getJury() {
	const urlParams = new URLSearchParams(window.location.search);
	let jury;
	if (urlParams.has("jury")) {
		jury = urlParams.get('jury');
	} else if (urlParams.has('room')) {
		jury = urlParams.get('room');
	}
	return jury;
}

function updateJuryHeader() {
	const jury = getJury();
	console.log(jury)
	if (jury === undefined) {
		$('.juryHeader').text('no jury selected');
		return;
	}
	updateH2('room', jury);
}

function updateTable() {
	const jury = getJury();
	if (jury === undefined)
		return;
	$.ajax({
		method: "GET",
		url: "/rest/roomP/" + jury
	}).done(function(msg) {
		$('#timetable tbody').empty();
		$.each(msg, function(i, item) {
			$('<tr>').append(
				$('<td>').text(item.time),
				$('<td>').text(item.hasOwnProperty('team') ? item.team.name : 'Pause')
			).appendTo('#timetable');
		});
	}).catch(function(error) {
		console.log(error)
	});
}

$(document).ready(function() {
	updateH1('room', getJury());
	updateJuryHeader();
	setInterval(function() {
		updateH1('room', getJury());
		updateJuryHeader();
	}, 100000000);

	updateTable();
	setInterval(updateTable, 120000);
});