
var sparql;
(function(ns) {
	
	ns.Node = function(type, value, language, datatype) {
		this.type = type;
		this.value = value;
		this.language = language;
		this.datatype = datatype;
	};
	
	ns.Node.prototype = {
			getValue: function() {
				return this.value;
			},
	
			getType: function() {
				return this.type;
			},
	
			getLanguage: function() {
				return this.language;
			},
	
			getDatatype: function() {
				return this.datatype;
			},
			
			equals: function(that) {
				var result = _.isEqual(this, that);
				return result;
			},
			
			/**
			 * Warning: If fnNodeMap does not return a copy, the node will not be copied.
			 * In general, Node should be considered immutable!
			 * 
			 * @param fnNodeMap
			 * @returns
			 */
			copySubstitute: function(fnNodeMap) {
				var sub = fnNodeMap(this);		 
				var result = (sub == undefined || sub == null) ? this : sub;
				return result;
			},
			
			toString: function() {
				switch(this.type) {
				case -1: return "?" + this.value;
				case 0: return "_:" + this.value;
				case 1: return "<" + this.value + ">";
				case 2: return "\"" + this.value + "\"" + (this.language ? "@" + this.language : "");
				case 3: return "\"" + this.value + "\"" + (this.datatype ? "^^<" + this.datatype + ">" : "");
				}
			},
			
			isVar: function() {
				return this.type === -1;
			},
			
			isUri: function() {
				return this.type === ns.Node.Type.Uri;
			}
	};
	
	
	ns.Node.Type = {};
	ns.Node.Type.Variable = -1;
	ns.Node.Type.BlankNode = 0;
	ns.Node.Type.Uri = 1;
	ns.Node.Type.PlainLiteral = 2;
	ns.Node.Type.TypedLiteral = 3;
	
	ns.Node.fromJson = function(talisJson) {
		var result = new ns.Node();
		
		if(!talisJson || typeof(talisJson.type) === 'undefined') {
			throw "Invalid node";
		}
		
		var type;
		switch(talisJson.type) {
		case 'bnode': type = 0; break;
		case 'uri': type = 1; break;
		case 'literal': type = 2; break;
		case 'typed-literal': type = 3; break;
		default: console.error("Unknown type: '" + talisJson.type + "'");
		}
		
		result.type = type;
		result.value = talisJson.value;
		result.language = talisJson.lang ? talisJson.lang : "";
		result.datatype = talisJson.datatype ? talisJson.datatype : "";

		// TODO I thought it happened that a literal hat a datatype set, but maybe I was imaginating things
		if(result.datatype) {
			result.type = 3;
		}
		
		return result;
		/*
		var type = -2;
		if(node.type == "uri") {
			
		}*/
	};
	
	ns.Node.isNode = function(candidate) {
		return candidate && (candidate instanceof ns.Node);
	};
	
	ns.Node.isUri = function(candidate) {
		return ns.Node.isNode(candidate) && candidate.isUri();		
	};

	
	ns.Node.parse = function(str) {
		var str = str.trim();
		
		if(strings.startsWith(str, '<') && strings.endsWith(str, '>')) {		
			return ns.Node.uri(str.substring(1, str.length - 1));
		} else {
			throw "Node.parse not implemented for argument: " + str;
		}
	};
	
	ns.Node.uri = function(str) {
		return new ns.Node(1, str, null, null);
	};
		
	ns.Node.v = function(name) {
		return new ns.Node(-1, name, null, null);
	};
	
	ns.Node.blank = function(id) {
		return new ns.Node(0, id, null, null);
	};
	
	ns.Node.plainLit = function(value, language) {
		return new ns.Node(2, value, language, null);
	};
	
	ns.Node.typedLit = function(value, datatype) {
		return new ns.Node(3, value, null, datatype);
	};

	ns.Node.forValue = function(value) {
		var dt = typeof value;		
		if(dt === "number") {
			return ns.Node.typedLit(value, "http://www.w3.org/2001/XMLSchema#double");
		} else {
			console.error("No handling for datatype ", td);
		}
		
		//alert(dt);		
	};
	
	
})(sparql || (sparql = {}));
		
