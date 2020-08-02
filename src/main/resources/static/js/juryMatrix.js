function updateTable() {
	//table header
	$.ajax({
		method: "GET",
		url: "/rest/teams"
	}).done(function(teams) {
		$('#matrix thead').empty();
		const header = $('<tr>');
		header.append($('<th>').text('Time'));
		$.each(teams, function(i, item) {
			header.append($('<th>').text(item.id));
		});
		header.appendTo('#matrix thead');

		//table body
		$.ajax({
			method: "GET",
			url: "/rest/juryMatrix"
		}).done(function(matrix) {
			$('#matrix tbody').empty();
			for (const time in matrix) {
				if (matrix.hasOwnProperty(time)) {
					const contents = matrix[time];
					if (contents == null) {
						$('<tr>').append(
							$('<td>').text(time),
							$('<td>').attr('colspan', teams.length).text('Pause')
						).appendTo('.matrix tbody');
					} else {
						const row = $('<tr>');
						row.append($('<td>').text(time));
						$.each(matrix[time], function(i, item) {
							row.append($('<td>').text(item));
						});
						row.appendTo('#matrix tbody');
					}
				}
			}
		}).catch(function(error) {
			console.log(error);
		});

		//teams table
		$('#teams').empty();
		const columnCount = 5; //TODO
		const rows = parseInt((teams.length / columnCount) + 1);
		console.log(rows);
		for (let i = 0; i < rows; i++) {
			const row = $('<tr>');
			for (let j = 0; j < columnCount; j++) {
				const tIndex = i + j * rows;
				if (tIndex < teams.length) {
					row.append(
						$('<td>').text(teams[tIndex].id).css('text-align', 'center').css('vertical-align', 'middle'),
						$('<td>').text(teams[tIndex].name)
					);
				}
			}
			row.appendTo('#teams');
		}

	}).catch(function(error) {
		console.log(error);
	});
}

$(document).ready(function() {
	setupHeadlineUpdate('juryMatrix');

	updateTable();
	setInterval(updateTable, 120000);
});