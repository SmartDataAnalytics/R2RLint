var org, aksw, sml_eval, app;

(function(ns) { (function(ns) { (function(ns) { (function(ns) {
	
	var views = org.aksw.sml_eval.views;

	
	/*
	ns.TaskListView = Backbone.Collection.extend({
		model: ns.ViewTask
	});
	*/

	

	
	ns.SmlEval = function(appModel) {
		this.apiUrl = "api/0.1/";
		this.model = appModel;
		
		var model = this.model;
		
		model.set({
			tasks: new Backbone.Collection()
		});
		
		
		var viewTasks = new views.ViewTaskTabs({
			el: $('#tabs'),
			collection: model.get('tasks'),
			offset: 1
		});
		
		viewTasks.render();

		//this.initialize();
	};
	
	ns.SmlEval.prototype = {
		
		reset: function() {
			this.resetTasks();
		},
		
		login: function(credentials) {

			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "login",
				type: 'POST',
				data:data
			}).done(function() {
				alert("Login Success");
			}).fail(function() {
				alert("Login Fail");
			});

		},
		
		logout: function() {

			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "logout",
				type: 'POST',
				data:data
			}).done(function() {
				alert("Logout Success");
			}).fail(function() {
				alert("Logout Fail");
			});
			
		},
		
		register: function(credentials) {

			var apiUrl = this.apiUrl;
			$.ajax({
				url: apiUrl + "register",
				type: 'POST',
				data:data
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
		runTask: function(mapping) {
			
		},
		
		
		/**
		 * Submits a mapping and records it
		 * 
		 */
		submitTask: function(mapping) {
			
		}

	};

	
})(ns.app || (ns.app = {}));
})(ns.sml_eval || (ns.sml_eval = {}));
})(ns.aksw || (ns.aksw = {}));
})(org || (org = {}));
