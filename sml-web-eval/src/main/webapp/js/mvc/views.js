var org, aksw, sml_eval, views;

(function(ns) { (function(ns) { (function(ns) { (function(ns) {

	var renderMapState = function(clazz, message) {
		var resultClass = clazz;
		// heading with status
		var result = '<div class="alert '+resultClass+' fade in">';
        result += '<button type="button" class="close" data-dismiss="alert">×</button>';
        //result += '<strong>Result:</strong> ' + message;
        result += message
        result += '</div>';
        
        return result;
	};

	
	ns.ViewTask = Backbone.View.extend({
		initialize: function() {
			_.bindAll(this);

			this.model.on('remove', this.unrender, this);
			
			this.model.on('change:mapperOutput', this.onChangeMapperOutput, this);
			this.model.on('change:mapperTriples', this.onChangeMapperTriples, this);
			
			this.model.on('change:isSolved', this.onChangeIsSolved, this);
			
			this.model.on('change:description', this.onChangeDescription, this);

			this.model.on('change:mapping', this.onChangeMapping, this);
			
			this.model.on('change:state', this.onChangeState, this);
		},
		
		events: {
			'click .btnSubmit': function() {
				
				var mappingText = this.$el.find('.mappingArea').val();
				
				this.model.set('mappingText', mappingText);
				
				//alert("submit of task" + JSON.stringify(this.model.attributes));
				this.trigger('run', this.model);
			}
		},
		
		
		onChangeState: function(model) {
			var isSolved = model.get('isSolved');
			if(isSolved) {
				return;
			}
			
			var state = model.get('state');
			
			var str;
			if(state === 'running') {
				str = renderMapState('alert', 'Running...');
			} else if(state === 'ready') {
				str = renderMapState('alert-success', 'Output:');
			} else {
				str = renderMapState('alert-error', 'Failure. Please check the output.');
			}
			
			if(isSolved) {
				var str = renderMapState('alert-success', 'Task solved sucessfully!');
				
				this.$el.find('.mapstate').html(str);
			}

			
			this.$el.find('.mapstate').html(str);

			
			// Disable the submit button while a task is running
			var disabled = state === 'running' ? 'disabled' : false;
			this.$el.find('.btnSubmit').attr("disabled", disabled);
		},
		
		onChangeMapping: function(model) {
			var mapping = model.get('mapping');
			
			this.$el.find('.mappingArea').val(mapping);
		},
		
		/**
		 * Once the task is solved, prevent further editing
		 */
		onChangeIsSolved: function(model) {
			var isSolved = model.get('isSolved');
			
			var state = isSolved ? "disabled" : false; 
			
			this.$el.find('.mappingArea').attr("disabled", state);
			this.$el.find('.btnSubmit').attr("disabled", state);
			
			if(isSolved) {
				var str = renderMapState('alert-success', 'Task solved sucessfully!');
				
				this.$el.find('.mapstate').html(str);
			}
		},
		
		onChangeMapperOutput: function(model) {
			var mapperOutput = model.get('mapperOutput');

			var levelToClass = {
					'INFO': 'info',
					'ERROR': 'error',
					'WARN': 'warning',
					'FATAL': 'fatal'
					//'DEBUG', null,
					//'TRACE', null
			};
			
			//console.log("Mapper output", mapperOutput);
			
			var data = [];
			for(var i = 0; i < mapperOutput.length; ++i) {
				var item = mapperOutput[i];
				var level = item.level; 
				
				var clazz = levelToClass[level];
				if(!clazz) {
					continue;
				}
				
				data.push({
					'class': clazz,
					'text': item.text
				});
			}
			
			// TODO Color the mapper output, skip debug messages
			// TODO Add give up button
			var templateStr
				= '<table style="width: 80%" class="table">'
				+ '<tr><th>Message</th></tr>'
				+ '{{~it :item:itemi}}'
				+ '<tr class="{{=item["class"]}}"><td>{{!item.text}}</td></tr>'
				//+ '<tr><td><p class="text-{{=item["class"]}}">{{!item.text}}</p></td></tr>'
				+ '{{~}}'
				+ '</table>'
				;

			
			var tempFn = doT.template(templateStr);
			
			var str = tempFn(data);

			
			this.$el.find('.mapperOutput').html(str); //utils.escapeHTML(JSON.stringify(mapperOutput)));
		},
		
		onChangeDescription: function(model) {
			var description = model.get('description');
			this.$el.find('.taskName').html(utils.escapeHTML(JSON.stringify(description)));
		},

		onChangeMapperTriples: function(model) {
			var mapperTriples = model.get('mapperTriples');
			var referenceTriples = model.get('referenceData');
			
			console.log("Diff", referenceTriples, mapperTriples);
			
			var rd = rdfDiff.createDiff(referenceTriples, mapperTriples);
			var rdHtml = rdfDiff.renderDiff(rd);
			this.$el.find('.rdfdiff').html(rdHtml);

			
			//this.$el.find('.mapper').html(utils.escapeHtml(mapperOutput));
		},
		
		render: function() {
			var $el = this.$el;
			
			var model = this.model;
			var data = model.attributes;
			
			var view = tasks.createTaskView(data);
			
			$el.append(view);
			
			//var textArea = $el.find('.lined');
			//console.log("TextArea: ", textArea);
			//textArea.linedtextarea();
			//textArea.linedtextarea({selectedLine: 1});

			
			return this;
		},
		
		unrender: function() {
			this.$el.remove();
		}
	});

	
	ns.ViewTab = Backbone.View.extend({
		initialize: function() {
			_.bindAll(this);
			
			this.$elHead = null;
			this.$elBody = null;
			
			this.model.on('remove', this.unrender, this);
		},
		
		render: function() {
			var $elParent = this.$el;
						
			var options = this.options;
			var position = options.position;
			
			console.log('Creating tab', $elParent, position);

			
			var taskName = this.model.get('name');
			
			var tab = utils.createTab($elParent, 'task' + taskName, taskName, '', position);
			this.$elHead = tab.head;
			this.$elBody = tab.body;
						
			return this;
		},
		
	
		unrender: function() {
			this.$elBody.remove();
			this.$elHead.remove();
		}
		
	});
	
		
	
	/**
	 * This view creates tab elements based on the collection
	 * 
	 */
	ns.ViewTaskTabs = Backbone.View.extend({
		initialize: function() {
			_.bindAll(this);
			
			this.collection.bind('add', this.addModel, this);
			this.collection.bind('remove', this.removeModel, this);
		},
		
		addModel: function(model) {
			var options = this.options;

			var offset = options.offset ? options.offset : 0;
			var position = offset + this.collection.length - 1;
			
			var viewTab = new ns.ViewTab({
				el: this.el,
				model: model,
				position: position
			});
		
			var $elTabBody = viewTab.render().$elBody;
			
			var viewTask = new ns.ViewTask({
				model: model
			});
			
			
			var self = this;
			viewTask.on('all', function(ev, args) {
				self.trigger(ev, args);
			});
			
			var $elViewTask = viewTask.render().$el;
			
			$elTabBody.append($elViewTask);
		},
		
		removeModel: function() {
			// Nothing to do
		}
	});
	
})(ns.views || (ns.views = {}));
})(ns.sml_eval || (ns.sml_eval = {}));
})(ns.aksw || (ns.aksw = {}));
})(org || (org = {}));
