var tasks;
(function(ns) {

	var expected = {
			  "http://example.org/about" : 
			    {
			       "http://purl.org/dc/elements/1.1/title": [ { "type" : "literal" , "value" : "Anna's Homepage" }, { "type" : "literal" , "value" : "Peter's Homepage" } ]
			    },
			    
				  "http://example.org/goobar" : 
				    {
				       "http://purl.org/dc/elements/1.1/title": [ { "type" : "literal" , "value" : "Anna's Homepage" }, { "type" : "literal" , "value" : "Peter's Homepage" } ]
				    },
			};

	ns.template
		= '<div class="row-fluid">'
		+ '    <div class="span12">'
		+ '        <div class="hero-unit" style="padding: 10px; margin-bottom: 10px;">'
		+ '            <p class="taskName">Loading task description...</p>'
		+ '            <p class="taskHint"></p>'
		+ '        </div>'
		+ '    </div>'
		+ '</div>'
		+ ''
		+ '<div class="row-fluid">'
		+ '    <div class="tableArea span8" style="margin-bottom: 15px">'
		+ '    </div>'
		+ '</div>'
		+ ''
		+ '<div class="container-fluid">'
		+ '    <div class="row-fluid">'
		+ '        <div class="span8">'
		+ '            <textarea class="mappingArea span12" style="resize:none" rows="20">Create ...</textarea>'

//		+ '            <div style="float:left">'
//		+ '                <button class="btnSubmit btn btn-primary">Reset</button>'
//		+ '            </div>'

		+ '            <div style="float:right">'
		+ '                <button style="width:100px; height: 40px; font-weight: bold; font-size: 15px;" class="btnSubmit btn btn-success">Run!</button>'
		+ '            </div>'

		+ '            <div class="mapperOutput" style="float:clear-both"></div>'
		
		
		+ '        </div>'
		+ '        <div class="rdfdiff span4">'
		+ '            Result goes here'
		+ '        </div>'
		+ '    </div>'
		+ ''
		+ "</div>"
		;

//	+ '    <div class="row-fluid">'
//	+ '        <div class="span8">'
//	+ '            <div style="float:right">'
//	+ '                <button class="btnSubmit btn btn-primary">Run!</button>'
//	+ '            </div>'
//	+ '        </div>'
//	+ '    </div>'

	ns.createTaskView = function(task) {
		var $elResult = $(ns.template);
		
		console.log("Task: ", task);
		$elResult.find('.taskName').html(task.name);
		
		
		var $elTableArea = $elResult.find('.tableArea');
		var tbles = task.tables;
		for(var i = 0; i < tbles.length; ++i) {
			var table = tbles[i];
			
			var html = tables.renderTable(table);
			var $el = $(html);
			//console.log("table html", html);
			$elTableArea.append($el);
		}
		
		//$elResult.find('.mappingArea').val(task.initialMappings['sparqlify']);
		
		
		var rd = rdfDiff.createDiff(task.referenceData, expected);
		var rdHtml = rdfDiff.renderDiff(rd);
		$elResult.find('.rdfdiff').html(rdHtml);
		
		return $elResult;
	};

	
})(tasks || (tasks = {}));
