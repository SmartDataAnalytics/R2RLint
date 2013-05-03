var utils;

(function(ns) {
	
	//console.log("The diff:", diff);
	
	// http://stackoverflow.com/questions/901115/how-can-i-get-query-string-values
	ns.getUriQuery(url) {
	    if(!url) {
	    	url = location.href;
	    }

	    var qs = url.substring(url.indexOf('?') + 1).split('&');
	    for(var i = 0, result = {}; i < qs.length; i++){
	        qs[i] = qs[i].split('=');
	        result[qs[i][0]] = decodeURIComponent(qs[i][1]);
	    }
	    return result;
	}
	
	// I think this is just the prototype escapeHTML method
	ns.escapeHTML = function(text) {
		if(typeof(text) === 'number') {
			text = "" + text;
		}
		
		return !text ? "" : text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
	};

	
	ns.createTab = function(tab, idSuffix, head, body, index) {
		
		var $elTab = $(tab);
		
		var $elHeads = $elTab.find('> ul > li');
		var $elBodies = $elTab.find('> div > div');
		
		var idPrefix = $elTab.attr('id');
		
		var idHead = idPrefix + '-head-' + idSuffix; 
		var idBody = idPrefix + '-body-' + idSuffix;
		
		var $elLiHead = $('<li id="' + idHead + '" />');
		var $elDivBody = $('<div id="' + idBody + '" class="tab-pane" />');
		
		// Just in case there is a mismatch in the lengths...
		var n = Math.min($elHeads.length, $elBodies.length);
		
		var i = index;
		
		var isAppendMode = index >= n;
		if(isAppendMode) {
			i = n - 1;
		}
		//console.log("...", $elHeads, $elBodies, i);

		var $elHeadI = $elHeads.eq(i);
		var $elBodyI = $elBodies.eq(i);
		
		//console.log("---", $elHeadI, $elBodyI);
		
		if(isAppendMode) {
			$elHeadI.after($elLiHead);
			$elBodyI.after($elDivBody);
		} else {
			$elHeadI.before($elLiHead);
			$elBodyI.before($elDivBody);				
		}

		$elDivBody.append(body);

		var $elAHead = $('<a href="#' + idBody + '" />');
		$elLiHead.append($elAHead);
		$elAHead.append(head);
		
		$elAHead.click(function (e) {
	        e.preventDefault();
	        $(this).tab('show');
	    });
		
		
		var result = {
			head: $elLiHead,
			body: $elDivBody
		};
		
		return result;
		//$('div.active').removeClass('active').removeClass('in');
		//$('li.active').removeClass('active');

//			$(elTab, "> ")
//			$('#myTabContent').append('<div class="tab-pane in active" id="new_tab_id"><p> Loading content ...</p></div>');
//			$('#tab').append('<li><a href="#new_tab_id" data-toggle="tab">Tab Name</a></li>');
//			$('#tab a:last').tab('show');
	}

	
	
})(utils || (utils = {}));
