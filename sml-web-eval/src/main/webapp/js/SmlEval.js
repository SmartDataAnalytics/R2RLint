var org, aksw, sml_eval, app;

(function(ns) { (function(ns) { (function(ns) { (function(ns) {
	
	var views = org.aksw.sml_eval.views;

	
	/*
	ns.TaskListView = Backbone.Collection.extend({
		model: ns.ViewTask
	});
	*/

	

	
	ns.SmlEval = Backbone.View.extend({//function(appModel) {
		initialize: function() {
			this.apiUrl = "api/0.1/";
			//this.model = appModel;
			
			var model = this.model;
			
			model.set({
				tasks: new Backbone.Collection(),
				isLoggedIn: false,
				evalMode: null // The selected eval mode (r2rml / sml) 
			});
			
			
			var viewTasks = new views.ViewTaskTabs({
				el: $('#tabs'),
				collection: model.get('tasks'),
				offset: 1
			});
			
			viewTasks.render();

			
			var self = this;
			viewTasks.on('run', function(model) {

				//console.log('run model', model);
				var taskId = model.get('id'); //model.get('taskId');
				var mappingText = model.get('mappingText');
				
				self.runMapping(taskId, mappingText);
			});
			
			this.restoreSession();

			//this.initialize();
		},
		
		reset: function() {
			this.resetTasks();
		},
		
		// Checks for an active session
		// Also re-fetches the session state
		restoreSession: function() {
			
			var self = this;
			$.ajax({
				url: self.apiUrl + "fetchState",
				type: 'POST'
			}).done(function(data) {
				
				
				self.model.set({
					isLoggedIn: data.isLoggedIn,
					evalMode: data.evalMode
				});

			}).fail(function() {
				alert("Failed to fetch the session state");
			});
			
		},
		
		login: function(credentials) {

			var self = this;
			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "login",
				type: 'POST',
				data: credentials
			}).done(function(data) {
			
				self.restoreSession();
				
			}).fail(function() {
				alert("Login Fail");
			});

		},
		
		logout: function() {

			var self = this;
			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "logout",
				type: 'POST',
			}).always(function() {
				self.model.set({
					isLoggedIn: false
				});				
			});
			
		},
		
		register: function(credentials) {

			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "register",
				type: 'POST',
				data: credentials
			}).done(function() {
				alert("Register Success");
			}).fail(function() {
				alert("Register Fail");
			});
		},
		

		/**
		 * Fetch all tasks
		 * 
		 * 
		 */
		resetTasks: function() {
			
			var self = this;
			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "fetchTasks",
				dataType: 'json'
			}).done(function(json) {
				
				self.loadTasks(json);
				
			}).fail(function(json) {
				alert("Failed to initialize. Most likely the server is down or it cannot be reached. Message: " + JSON.stringif(msg));
			});

		},
		
		loadTasks: function(tsks) {
			
			var col = this.model.get('tasks');
			
			//col.reset();
			
			for(var i = 0; i < tsks.length; ++i) {
				var task = tsks[i];
				
				var m = new Backbone.Model(task);
				console.log("Task", task, col);
				col.add(m);				
			}
		},
		
		
		/**
		 * Fetch the state of the user
		 * 
		 * 
		 */
		fetchUserState: function() {
			
		},
		
		
		/**
		 * Runs a task without creating a submit entry.
		 * Used for calculating the diff
		 * 
		 */
		runMapping: function(taskId, mappingText) {
			
			var data = {
				taskId: taskId,
				mapping: mappingText
			};

			this.setMappingResult(taskId, {
				model: {},
				messages: ["Running..."]
			});

			var apiUrl = this.apiUrl;
			var self = this;
			$.ajax({
				url: apiUrl + "runMapping",
				type: 'POST',
				data: data
			}).done(function(json) {
				
				
				self.setMappingResult(taskId, json);
				
			}).fail(function() {
				alert("Mapping Fail");
			});
			
		},
		
		
		setMappingResult: function(taskId, json) {
			var taskModels = this.model.get('tasks');
			var taskModel = taskModels.get(taskId);
			
			taskModel.set({
				mapperTriples: json.model,
				mapperOutput: json.messages
			});
		},
		
		/**
		 * Submits a mapping and records it
		 * 
		 */
		submitTask: function(mapping) {
			
		}

	});

	
})(ns.app || (ns.app = {}));
})(ns.sml_eval || (ns.sml_eval = {}));
})(ns.aksw || (ns.aksw = {}));
})(org || (org = {}));
