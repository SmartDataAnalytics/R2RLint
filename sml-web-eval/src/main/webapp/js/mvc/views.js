var org, aksw, sml_eval, views;

(function(ns) { (function(ns) { (function(ns) { (function(ns) {

	ns.ViewTask = Backbone.View.extend({
		initialize: function() {
			_.bindAll(this);

			this.model.on('remove', this.unrender, this);
			
			this.model.on('change:mapperOutput', this.onChangeMapperOutput, this);
			this.model.on('change:mapperTriples', this.onChangeMapperTriples, this);
		},
		
		events: {
			'click .btnSubmit': function() {
				
				var mappingText = this.$el.find('.mappingArea').val();
				
				this.model.set('mappingText', mappingText);
				
				//alert("submit of task" + JSON.stringify(this.model.attributes));
				this.trigger('run', this.model);
			}
		},
		
		onChangeMapperOutput: function(model) {
			var mapperOutput = model.get('mapperOutput');
			this.$el.find('.mapperOutput').html(utils.escapeHTML(JSON.stringify(mapperOutput)));
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
			
			var tab = utils.createTab($elParent, 'task' + taskName, taskName, 'Foobar', position);
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
