var org, aksw, sml_eval, app;

(function(ns) { (function(ns) { (function(ns) { (function(ns) {
	
	var views = org.aksw.sml_eval.views;

	
	/*
	ns.TaskListView = Backbone.Collection.extend({
		model: ns.ViewTask
	});
	*/

	

	
	ns.SmlEval = Backbone.View.extend({//function(appModel) {
		initialize: function(options) {
			
			this.apiUrl = this.options.apiUrl; //"api/0.1/";
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
				
				self.submitMapping(taskId, mappingText);
			});
			
			this.restoreSession();

			//this.initialize();
		},
		
		reset: function() {
			this.resetTasks();
		},
		
		
		advance: function() {

			var self = this;
			$.ajax({
				url: self.apiUrl + "advance",
				type: 'POST'
			}).done(function(data) {

				self.reset();

			}).fail(function() {
				alert("Failed to advance to next language");
			});
			
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
					evalMode: data.evalMode,
					limesToken: data.limesToken
				});

			}).fail(function() {
				alert("Failed to fetch the session state");
			});
			
		},

		
		hashPassword: function(credentials) {
			var result = _.extend({}, credentials);
			
			var tmp = credentials.password;
			for(i = 0; i < 7; ++i) {
				tmp = CryptoJS.MD5(tmp).toString();
			}
			
			result.password = tmp; 

			return result;
		},
		
		login: function(credentials) {
			
			var c = this.hashPassword(credentials);
			
			var self = this;
			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "login",
				type: 'POST',
				data: c
			}).done(function(data) {
			
				self.restoreSession();
				
			}).fail(function() {
				alert("Login Fail");
			});

		},
		
		fetchSummary: function() {
			var self = this;

			var result = $.ajax({
				url: self.apiUrl + "fetchSummary",
				type: 'GET'
			});
			
			return result;
		},
		
		
		updateSummary: function() {
			var self = this;

			
			var promise = this.fetchSummary();
			promise.done(function(data) {
				self.model.set({summary: data});
			}).fail(function() {
				console.log("Failed to fetch summary. Maybe not logged in.");
				//alert("Failed fetching summary");
			});
		},
		
		// Returns the initial mapping, the latest mapping
		// the latest working mapping and/or a flag whether
		// the task is completed
		fetchTaskState: function(taskId) {

			var self = this;
			var apiUrl = this.apiUrl;
			var result = $.ajax({
				url: apiUrl + "fetchTaskState",
				type: 'POST',
				data: {
					taskId: taskId
				}
			});
			
			
			/*
			.always(function() {
				self.model.set({
					isLoggedIn: false
				});				
		});*/
			return result;
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

			var c = this.hashPassword(credentials);
			
			var self = this;
			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "register",
				type: 'POST',
				data: c
			}).done(function() {
				self.login(credentials);
				//alert("Register Success");
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
				alert("Failed to initialize. Most likely the server is down or it cannot be reached. Message: " + JSON.stringify(msg));
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
		
		
		runMapping: function(taskId, mappingText) {
			var result = this.submitMappingCore(taskId, mappingText, 'runMapping');
			return result;			
		},
		
		submitMapping: function(taskId, mappingText) {

			var result = this.submitMappingCore(taskId, mappingText, 'submitMapping');
			
			var self = this;
			result.done(function() {
				self.updateSummary();
			});
			
			return result;
		},
		
		/**
		 * Runs a task without creating a submit entry.
		 * Used for calculating the diff
		 * 
		 */
		submitMappingCore: function(taskId, mappingText, action) {
			
			// TODO In the task model set the state to running
			
			var data = {
				taskId: taskId,
				mapping: mappingText
			};

			
			var col = this.model.get('tasks');
			var tsk = col.get(taskId);
			tsk.set({state: 'running'});
			
			
			this.setMappingResult(taskId, {
				model: {},
				messages: ['Running...']
			});

			var apiUrl = this.apiUrl;
			var self = this;
			
			var result = $.ajax({
				url: apiUrl + action,
				type: 'POST',
				data: data
			}).done(function(json) {
				tsk.set({state: 'ready'});
				
				self.setMappingResult(taskId, json);
				
			}).fail(function() {
				tsk.set({state: 'Failure. Please check the output below.'});
				
				alert('Mapping Fail');
			});
			
			return result;
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
