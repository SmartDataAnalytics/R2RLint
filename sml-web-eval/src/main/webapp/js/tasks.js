var tasks;
(function(ns) {

	ns.template
		= '<div class="row-fluid">'
		+ '    <div class="span12">'
		+ '        <h3 class="taskName">Loading task description...</h3>'
		+ '        <p class="taskHint"></p>'
		+ '    </div>'
		+ '</div>'
		+ ''
		+ '<div class="row-fluid">'
		+ '    <div class="tableArea span8" style="margin-bottom: 15px">'
		+ '    </div>'
		+ '</div>'
		+ ''
		+ '<div class="row-fluid">'
		+ '    <div class="span8">'
		+ '        <textarea class="mappingArea span12" style="resize:none" rows="20">Create ...</textarea>'
		+ '    </div>'
		+ '    <div class="rdfdiff span4">'
		+ '        Result goes here'
		+ '    </div>'
		+ '</div>'
		+ ''
		+ '<div class="row-fluid">'
		+ '    <div class="span8">'
		+ '        <div style="float:right">'
		+ '            <button class="btnSubmit btn btn-primary">Run!</button>'
		+ '        </div>'
		+ '    </div>'
		+ '</div>'
		;
	
	ns.createTaskView = function(task) {
		var $elResult = $(ns.template);
		
		console.log("Task: ", task);
		$elResult.find('.taskName').html(task.name);
		
		
		var $elTableArea = $('.tableArea');
		var tbles = task.tables;
		for(var i = 0; i < tbles.length; ++i) {
			var table = tbles[i];
			
			$el = tables.renderTable(table);
			
			$elTableArea.append($el);
		}
		
		$elResult.find('.mappingArea').val(task.initialMappings['sparqlify']);
		
		
		var rd = rdfDiff.createDiff(task.referenceData, {});
		var rdHtml = rdfDiff.renderDiff(rd);
		$elResult.find('.rdfdiff').html(rdHtml);
		
		return $elResult;
	};

	
})(tasks || (tasks = {}));
