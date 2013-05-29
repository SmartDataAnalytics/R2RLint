(function() {
	

	
	
	
	var tableModel = {
			name: 'Person',
			head: [{
				name: 'id',
				type: 'int'
			}, {
				name: 'name',
				type: 'text'
			}],
			body: [
				{id: 1, name: 'Claus'},
				{id: 2, name: 'Joerg'}
			]
		};

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
		
	var actual = {
			  "http://example.org/about" : 
			    {
			       "http://purl.org/dc/elements/1.1/title": [ { "type" : "literal" , "value" : "Anna's Homepage" } ]
			    },
			  "http://example.org/foo" : 
			    {
			       "http://example.org/bar": [ { "type" : "literal" , "value" : "Baz" } ]
			    }
	};

	
	
	
	
		/**
		 * Adjusts the frontend according to the backend's state.
		 * Concretely loads the tasks and score sheet.
		 */
		var resetAppTest = function() {
	
			for(var i = 0; i < 4; ++i) {
				
				var taskNo = i + 1;
				
				utils.createTab('#tabs', 'task' + taskNo, 'Task ' + taskNo, 'Foobar', 1 + i);
				
			}
			
	    	var tableHtml = tables.renderTable(tableModel);
	    	$('.tableArea').html(tableHtml);
	    	
	    	
			var diff = rdfDiff.createDiff(expected, actual);
	
			var html = rdfDiff.renderDiff(diff);
			
			//console.log('HTML', html);
			var $elTarget = $('.rdfdiff');
			console.log('Target Element: ', $elTarget);
			$elTarget.html(html);
			
	
		}
		
		
		
		var initTasks = function(tsks) {
			for(var i = 0; i < tsks.length; ++i) {
				var task = tsks[i];
				
				var taskNo = i + 1;
				
				var tab = utils.createTab('#tabs', 'task' + taskNo, 'Task ' + taskNo, 'Foobar', 1 + i);

				var view = tasks.createTaskView(task);
				
				tab.body.append(view);
			}
		};

		var apiUrl = config.apiUrl;

	
		var resetApp = function() {
			
			$.ajax({
				url: apiUrl + "fetchTasks",
				dataType: 'json'
			}).done(function(json) {
				
				initTasks(json);
				
			}).fail(function(json) {
				alert("Failed to initialize. Most likely the server is down or it cannot be reached. Message: " + JSON.stringif(msg));
			});
		}
		
		
		var readLogin = function() {
			var result = {
				username: $('#login-username').val(),
				password: $('#login-password').val()
			};
			return result
		}
		
		var readRegister = function() {
			var result = readLogin();
			
			_.extend(result, {
				passwordConfirm: $('#register-passwordConfirm').val(),
				email: $('#register-email').val(),
				emailConfirm: $('#register-emailConfirm').val(),
			});
			
			//console.log("Read register data: ", result);
			
			return result;
		};
		
		var bindKeyEvents = function() {
			//$('#login-username').keypress(doValidation);
			//$('#login-password').keypress(doValidation);
			$('#register-passwordConfirm').keyup(doValidation);
			$('#register-email').keyup(doValidation);
			$('#register-emailConfirm').keyup(doValidation);		
		};
		
		var doValidation = function() {
			var data = readRegister();
			var state = validateRegister(data);
			showValidation(state);
		}
		
		var validateEmail = function(mail) {
			var atIndex = mail.lastIndexOf('@');
			var dotIndex = mail.lastIndexOf('.'); 
			
			var result = atIndex < dotIndex && atIndex != -1;
			return result;
		};
		
		var validateRegister = function(data) {
			var result = {
				userOk: true,
				userMsg: "",
				passOk: true,
				passMsg: "",
				mailOk: true,
				mailMsg: "",
				passConfirmMsg: "",
				mailConfirmMsg: "",
				isAllOk: true
			};
			
			if(data.username.length < 3) {
				result.userOk = false;
				result.isAllOk = false;
				result.userMsg = "Username must be at least 3 characters";
			}
			
			if(data.password.length < 6) {
				result.passOk = false;
				result.isAllOk = false;
				result.passMsg = "Password must be at least 6 characters";
			}
				
			var mail = data.email.trim();
			var isValidEmail = validateEmail(mail);
			if(!isValidEmail) {
				result.mailOk = false;
				result.isAllOk = false;
				result.mailMsg = "Invalid email";
			}
			
			if(mail != data.emailConfirm) {
				result.mailOk = false;
				result.isAllOk = false;
				result.mailConfirmMsg = "Emails do not match";
			}
			
			//console.log("Data", data);
			if(data.password != data.passwordConfirm) {
				result.passwordOk = false;
				result.isAllOk = false;
				result.passConfirmMsg = "Passwords do not match";				
			}
			
			return result;
		};
		
		var showValidation = function(data) {
			
			username: $('#login-username-msg').text(data.userMsg);
			password: $('#login-password-msg').text(data.passMsg);
			passwordConfirm: $('#register-passwordConfirm-msg').text(data.passConfirmMsg);
			email: $('#register-email-msg').text(data.mailMsg);
			emailConfirm: $('#register-emailConfirm-msg').text(data.mailConfirmMsg);

			if(!data.isAllOk) {
				$('#registerAndLogIn').attr("disabled", "disabled");
			} else {
				$('#registerAndLogIn').removeAttr("disabled");
			}
		};
		
		
		var updateLogInPage = function(model) {
			var evalMode = model.get('evalMode');
			$('.lang-assignment').html('You have been chosen to solve the tasks with the mapping language: ' + evalMode);
			// TODO Make a link to the spec
		}
	
		
		/**
		 * 
		 * 
		 */
		var transformSummary = function(data) {
					
			
			var currentLang = data.currentLang;
			var langOrder = data.langOrder;
			var langSummaries = data.langSummaries;

			
			
			var result = {
					currentLang: currentLang,
					canAdvance: data.canAdvance,
					isAllTasksComplete: data.isAllTasksComplete,
					//langOrder: langOrder,
					langs: null
			};
	

			var ls = []; 
			for(var i = 0; i < langOrder.length; ++i) {
				var lang = langOrder[i];				
				
				var langSummary = langSummaries[lang];
				var taskSummaries = langSummary.taskSummaries;
				var taskOrder = langSummary.taskOrder;

				var ts = [];
				for(var j = 0; j < taskOrder.length; ++j) {
					var taskId = taskOrder[j];
					var taskSummary = taskSummaries[taskId];

					ts.push(taskSummary);
				}
				
				var item = {
					lang: lang,
					tasks: ts
				};
				
				ls.push(item);
			}

			result.langs = ls;
			
			return result;
		};
		
		
		/**
		 * {
		 *     currentLang:
		 *     langs: [{
		 *     		lang: 'r2rml',
		 *     		tasks: [{
		 *     			name:
		 *     			isCompleted:
		 *     		}]
		 *         
		 *     }
		 *     ]
		 * 
		 */
		var renderSummary = function(d) {
		
			var data = transformSummary(d);
			console.log("Transformed summary: ", data);
			
			
			var templateStr = "{{~it.langs :lang:ilang}}<h2>{{=lang.lang}}</h2><table>{{~lang.tasks :task:itask}}<tr><td>{{=task.taskId}}</td><td>{{=task.isCompleted}}</td></tr>{{~}}</table>{{~}}";

			var tempFn = doT.template(templateStr);
			
			var str = tempFn(data);
			
			// Show an advance button if that is possible
			str += "all tasks complete? " + d.isAllTasksComplete;
			str += "   can advance? " + d.canAdvance;
			
			$('#summary-content').html(str);
		};
		
		
		
	/*		
		var doLogin = function(data) {
			var data = readLogin();
			
			$.ajax({
				url: apiUrl + "login",
				type: 'POST',
				data:data
			}).done(function() {
				alert("Login Success");
			}).fail(function() {
				alert("Login Fail");
			});
		}
		
		var doRegister = function(data) {
			var data = readRegister();
			
			$.ajax({
				url: apiUrl + "register",
				type: 'POST',
				data:data
			}).done(function() {
				alert("Register Success");
			}).fail(function() {
				alert("Register Fail");
			});
		}
		*/	
		$(document).ready(function() {
			
			var app = org.aksw.sml_eval.app;
			var appModel = new Backbone.Model();
			
			var smlEval = new app.SmlEval({
				model: appModel,
				apiUrl: apiUrl,
			});
			
			
			
			
			//smlEval.on("loggedIn", function() {
			appModel.on("change:isLoggedIn", function() {
				console.log("model", this);
				var isLoggedIn = this.get('isLoggedIn');
				if(isLoggedIn) {
					//console.log("yay", $('#signUp-form'));
					$('#signUp-form').hide();
					$('#signUp-loggedIn').show();
					$('#logOut').show();
					
					$('#scoreSheet-loggedIn').show();
					$('#scoreSheet-loggedOut').hide();
				} else {
					//console.log("awww");
					$('#signUp-form').show();
					$('#signUp-loggedIn').hide();
					$('#logOut').hide();

					$('#scoreSheet-loggedIn').hide();
					$('#scoreSheet-loggedOut').show();
				}
			});

			appModel.on("change:isLoggedIn", function() {
				smlEval.updateSummary();
			});

			
			appModel.on('change:summary', function() {
				var summary = this.get('summary'); 
				renderSummary(summary);
			});
			

			
			appModel.on('change:limesToken', function() {
				var token = this.get('limesToken');
				$('#scoreSheet-loggedIn').html('<div class="alert">Please visit <a target="_blank" href="http://survey.geoknow.eu/index.php/survey/index/sid/676733/token/' + token + '/Y" >this page</a> and give feedback on a few questions.</div>');
			});
			
			appModel.on("change:isLoggedIn", function() {
				
				smlEval.reset();
				// For all known tasks fetch the state
				//appModel
				
				
				
				//console.log("model", this);
				//var isLoggedIn = this.get('isLoggedIn');
			});			
			

			var taskCol = appModel.get('tasks');

			
			var updateMappings = function(model) {
				var taskId = model.get('id');
				
				console.log("Fetching task state: ", taskId);
				var promise = smlEval.fetchTaskState(taskId);
				promise.done(function(json) {
					console.log("Task state is", json);
					var data = json.taskStates[taskId];
					
					/*
					if(data.isSolved) {
						// Disable write / execution
						
						
					}
					*/
					
					
					
					taskCol.get(data.id).set(data);					
				});
			};
			
			taskCol.on('reset', function() {
				this.each(function(model) {
					updateMappings(model);
				});
			});
			
			taskCol.on('add remove', function(model) {
				updateMappings(model);
			});

			
			appModel.on("change", function() {
				//var stage = this.get('evalMode');
				
				updateLogInPage(this);
				//updateSummary();
				
			});			
			
			$('#logOut').click(function(ev) {
				ev.preventDefault();
				smlEval.logout();
			});

			$('#logIn').click(function(ev) {
				ev.preventDefault();
				var data = readLogin();
				smlEval.login(data);
			});

			$('#registerAndLogIn').click(function(ev) {
				ev.preventDefault();
				var data = readRegister();
				smlEval.register(data);
			});

			
			// Twitter Bootstrap's way of enabling tabs
			$('#tabs > ul > li > a').click(function (e) {
		        e.preventDefault();
		        $(this).tab('show');
		    });
	    	$('#tabs a:first').tab('show');
	
	    	bindKeyEvents();
	    	
	    	smlEval.reset();
		});
	
})();
