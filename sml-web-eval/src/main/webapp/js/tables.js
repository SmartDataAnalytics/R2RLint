var tables;

(function(ns) {
		
	

	ns.renderTable = function(json) {
		var body = json.body;
		var head = json.head;
	
		var n = head.length;
		
		var titleN = Math.ceil(n / 2);
		
		var result = '<table class="table table-striped table-condensed table-bordered">';
		
		result += '<tr>';
		result += '<td colspan="' + titleN + '"><b>';
		result += json.name;
		result += '</b></td>';
		
		var m = body.length;
		
		
		// Write table heads
		result += '<tr>';
		for(var j = 0; j < n; ++j) {
			var colHead = head[j];
			var colName = colHead.name;
			
			result += '<th>';
			result += utils.escapeHTML(colName);
			//result += '<br />';
			
			//result += escapeHTML(colName) + '<br />' + escapeHTML(colType);
			
			result += '</th>';
		}
		result += '</tr>';

		// Write table heads
		result += '<tr>';
		for(var j = 0; j < n; ++j) {
			var colHead = head[j];
			var colType = colHead.type;
			
			result += '<td>';
			result += '<span style="color: grey">(';
			result += utils.escapeHTML(colType);
			result += ')</span>';
			result += '</td>';
		}
		result += '</tr>';
		
		// Write table body
		for(var i = 0; i < m; ++i) {
			var rowData = body[i];
			
			result += '<tr>';
			
			
			for(var j = 0; j < n; ++j) {
				var colHead = head[j];
				var colName = colHead.name
				
				var cellData = rowData[colName];
				
				result += '<td>';
				result += utils.escapeHTML(cellData);
				result += '</td>';
			}
			result += '</tr>';
		}
		
		result += '<table>';
		
		return result;
	}

	
	
})(tables || (tables = {}));
