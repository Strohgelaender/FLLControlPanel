function updateTable() {
	$.ajax({
		method: "GET",
		url: "/rest/timetable"
	}).done(function(timetable) {
		$('#timetable tbody').empty();
		$.each(timetable, function(i, item) {
			const row = $('<tr>');
			row.append($('<td>').text(item.time));
			if (item.hasOwnProperty('tableA')) {
				row.append(
					$('<td>').text(item.teamA != null ? item.teamA.name : ''),
					$('<td>').text(item.tableA.name),
					$('<td>').text(item.tableB.name),
					$('<td>').text(item.teamB != null ? item.teamB.name : '')
				);
			} else {
				//Pause
				row.append($('<td>').attr('colspan', 4).text('Pause'));
			}
			row.appendTo('#timetable');
		});
	}).catch(function(error) {
		console.log(error);
	})
}

$(document).ready(function() {
	setupHeadlineUpdate('timetable');

	updateTable();
	setInterval(updateTable, 120000);
});