function updateScoreboard() {
	$.ajax({
		method: "GET",
		url: "/rest/scores"
	}).done(function(msg) {
		$('#scoreboard tbody').empty();
		$.each(msg, function(i, item) {
			$('<tr>').append(
				$('<td>').text(item.name),
				$('<td>').text(item.round1),
				$('<td>').text(item.round2),
				$('<td>').text(item.round3),
				$('<td>').text(item.rank)
			).appendTo('#scoreboard');
		});
	}).catch(function(error) {
		console.log(error);
	});
}

$(document).ready(function() {
	setupHeadlineUpdate('scoreboard');

	updateScoreboard();
	setInterval(updateScoreboard, 120000);
});