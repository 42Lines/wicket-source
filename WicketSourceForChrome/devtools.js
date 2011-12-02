chrome.experimental.devtools.panels.elements.createSidebarPane("WicketSource", function(sidebar) {

	// The function below is executed in the context of the inspected
	// page because it is called from sidebar.setExpression. Because it is
	// in a different context, functions used must be accessible from there.
	function page_getProperties() {
		var hiddenDivId = "wicketsource-chrome-div";
		var boxStyle = "background-color: #EEEEEE; border: #666666 solid 1px; width: 300px; height: 50px; " +
				"text-align: center; display: block; position: absolute; top: 30px; right: 20px; ";
		var linkStyle = "text-decoration: none; color: blue; ";
		
		function Message() {
			this.message = "Select a node with a wicketsource attribute to see details.";
		}

		// Data container.
		function WicketProperties() {
			this.wicketId = null;
			this.packageName = null;
			this.sourceLine = null;
			this.eclipseUrl = null;
		}
		function shallowCopy(data) {
			// Make a shallow copy with a null prototype, so that sidebar does not
			// expose prototype.
			var props = Object.getOwnPropertyNames(data);
			var copy = {
				__proto__ : null
			};
			for ( var i = 0; i < props.length; ++i)
				copy[props[i]] = data[props[i]];
			return copy;
		}
		
		// Parses the wicketsource attribute into a json object.
		function parseNode() {
			var wp = new WicketProperties();
			var sourceFile = "";
			var lineNumber = "";
			if (($0 == null) || ($0.attributes.wicketsource == undefined)) {
				return wp;
			}
			var pieces = $0.attributes.wicketsource.value.split(":");
			if (pieces.length == 3) {
				wp.packageName = pieces[0];
				sourceFile = pieces[1];
				lineNumber = pieces[2];
			} else if (pieces.length == 2) {
				sourceFile = pieces[0];
				lineNumber = pieces[1];
			}
			wp.sourceLine = sourceFile + ":" + lineNumber;
			if (wp.packageName == null) {
				wp.packageName = "";
			}
			wp.wicketId = $0.attributes['wicket:id'].value;
			wp.eclipseUrl = "http://" + "localhost" + ":" + 9123 + "/open?src=" + encodeURIComponent($0.attributes.wicketsource.value);
			return wp;
		}
		
		function updateDataDiv(wp)
		{
			// Find or create the hidden data div we're using.
			var wicketsourceDiv = document.getElementById(hiddenDivId);
			if (!wicketsourceDiv) {
				wicketsourceDiv = document.createElement("div");
				wicketsourceDiv.setAttribute("id", hiddenDivId);
				wicketsourceDiv.setAttribute("style", boxStyle);
				document.body.appendChild(wicketsourceDiv);
			}
			
			// Empty out any children from previous uses.
			if (wicketsourceDiv && wicketsourceDiv.childNodes.length > 0) {
				for (var i = 0; i < wicketsourceDiv.childNodes.length; i++) { 
					wicketsourceDiv.removeChild(wicketsourceDiv.childNodes.item(i));
				}
			}

			// Create the new data contents based on currently selected wicketsource node.
			var nodeA = document.createElement("a");
			nodeA.setAttribute("style", linkStyle);
			nodeA.setAttribute("href", wp.eclipseUrl);
			nodeA.setAttribute("data", wp.packageName + ":" + wp.sourceLine);
			nodeA.setAttribute("target", "_wicketsource_chrome_eclipse");
			nodeA.appendChild(document.createTextNode("Jump to " + wp.sourceLine + " in Eclipse"));
			wicketsourceDiv.appendChild(nodeA);
			
		}
		// When not in use, delete the div entirely so it doesn't show.
		function deleteDataDiv()
		{
			var wicketsourceDiv = document.getElementById(hiddenDivId);
			if (wicketsourceDiv) {
				document.body.removeChild(wicketsourceDiv);
			}
		}


		// Gets the currently selected node, and if it's not a wicketsource node, shows a default message.
		if (($0 == null) || ($0.attributes.wicketsource == null)) {
			deleteDataDiv();
			return shallowCopy(new Message());
		}

		// If it is a wicketsource node, parse it and return the json for display.
		var wp = parseNode();
		updateDataDiv(wp);
		return shallowCopy(wp);
	}

	
	// Listener event callback initiates "inspected-page context" data retrieval 
	// (which understands currently selected node in the Inspect Element tab). 
	function update() {
		sidebar.setExpression("(" + page_getProperties.toString() + ")()", "Component Properties");
	}
	
	// On the way in the first time, show an instructional message.
	update();
	
	// All further activity comes through the listener events.
	chrome.experimental.devtools.panels.elements.onSelectionChanged.addListener(update);
});

