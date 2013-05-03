var rdfDiff;

(function(ns) {
	var predicatesURLs = {
		'http://example.org/': 'example',
		'http://xmlns.com/foaf/0.1/': 'foaf'
	};

	ns.getState = function(isExpected, isActual) {
		var result;

		if(isExpected) {
			if(isActual) {
				result = "covered";
			} else {
				result = "uncovered"
			}
		} else {
			if(isActual) {
				result = "excessive";
			} else {
				result = "invalid";
			}
		}

		return result;
	}

	ns.createDiff = function(expected, actual) {

		var result = ns.diffResource(expected, actual, function(expected, actual) {
			var result = ns.diffResource(expected, actual, function(expected, actual) {
				var result = ns.diffObjects(_.values(expected), _.values(actual));
				return result;
			});
			return result;
		});

		return result;
	}

	ns.diffResource = function(expected, actual, fnProcessChildren) {
		var result = [];

		// Get all subjects
		var items =
			_.chain(_.keys(expected))
			.union(_.keys(actual))
			.uniq()
			.sort()
			.value();

		//console.log("[diffResource] items: ", items)

		for(var i = 0; i < items.length; ++i) {
			var item = items[i];

			//console.log('[diffResource] item:', item);

			var isExpected = item in expected;
			var isActual = item in actual;

			var state = ns.getState(isExpected, isActual);

			var expectedChildren = item in expected ? expected[item] : {};
			var actualChildren = item in actual ? actual[item] : {};

			var children = fnProcessChildren(expectedChildren, actualChildren);

			result.push({
				item: item,
				state: state,
				children: children
			});
		}

		return result;
	}

	ns.diffObjects = function(expected, actual) {

		var myIndexOf = function(list, item) {
			var result = -1;

			for(var i = 0; i < list.length; ++i) {
				var it = list[i];

				if(_.isEqual(item, it)) {
					result = i;
					break;
				}
			}
			return result;
		}

		//console.log("[diffObjects] expected, actual: ", expected, actual);

		var rdfObjectToString = function(json) {
			var node = sparql.Node.fromJson(json);
			var result = node.toString();
			return result;
		}

//		var items = _.chain().union(expected, actual).uniq(false, rdfObjectToString).sortBy(rdfObjectToString).value();

		var union = _.union(expected, actual);
		var uniq = _.uniq(union, false, rdfObjectToString);
		var items = _.sortBy(uniq, rdfObjectToString);
		//console.log("items", items, expected, actual);


		var result = [];

		for(var i = 0; i < items.length; ++i) {
			var item = items[i];

			var isExpected = myIndexOf(expected, item) != -1;
			var isActual = myIndexOf(actual, item) != -1;
			//console.log("On item: ", item, isExpected, isActual);

			var state = ns.getState(isExpected, isActual);

			var resultItem = {
				item: item,
				state:state
			};

			result.push(resultItem);
		}

		return result;
	}



	ns.renderResource = function(item) {
		var str = item.item;
		for(var key in predicatesURLs){
			if(str.indexOf(key) !== -1){
				str = str.replace(key, predicatesURLs[key]+":");
				break;
			}
		}
		var text = utils.escapeHTML(str);
		var result = '<span class="' + item.state + '">' + text + '</span>';

		return result;
	}

	ns.renderObject = function(item) {
		var json = item.item;
		var node = sparql.Node.fromJson(json);
		var str = node.toString();
		var text = utils.escapeHTML(str);
		var result = '<span class="' + item.state + '">' + text + '</span>';

		return result;
	}

	ns.renderDiff = function(subjects) {

		//var result = '<table>';
		var result = '<ul class="separated bullets-none">';

		for(var i = 0; i < subjects.length; ++i) {
			var subject = subjects[i];

			result += '</li>';
//				result += '<tr>'
//				result += '<td>';
			result += ns.renderResource(subject);
			//result += '</td><td>';

			var predicateStr = '<table class="separated-vertical" style="margin-left:15px; margin-bottom: 15px;">';

			// TODO color
			var predicates = subject.children;

			for(var j = 0; j < predicates.length; ++j) {
				var predicate = predicates[j];

				predicateStr += '<tr>';
				predicateStr += '<td style=" vertical-align: top;">' + ns.renderResource(predicate) + '</td>';

				var objectStr = '<td><ul class="separated bullets-none">';

				var objects = predicate.children;
				for(var k = 0; k < objects.length; ++k) {
					var object = objects[k];

					var str = ns.renderObject(object);

					objectStr += '<li>' + str + '</li>';
				}

				objectStr += '</ul></td>';

				predicateStr += objectStr;
				predicateStr += '</tr>';
			}

			predicateStr += '</table>';

			result += predicateStr;

			//result += '</td>';
			//result += '</tr>';
			result += '</li>';

		}

		//result += "</table>"
		result += '</ul>';
		return result;
	}


})(rdfDiff || (rdfDiff = {}));
