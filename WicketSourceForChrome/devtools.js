chrome.experimental.devtools.panels.elements.createSidebarPane("WicketSource", function(sidebar) {
	
	function update() {
		sidebar.setExpression("(" + page_getProperties.toString() + ")()", "Component Properties");
	}

	// The function below is executed in the context of the inspected
	// page because it is called from sidebar.setExpression. Because it is
	// in a different context, functions used must be accessible from there.
	function page_getProperties() {
		function Message() {
			this.message = "Select a node with a wicketsource attribute to see details.";
		}

		// Data container.
		function WicketProperties() {
			this.wicketId = null;
			this.packageName = null;
			this.sourceLine = null;
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
			currentWP = wp;
			return wp;
		}

		// Creates an http url for the Eclipse call. Not yet used due to no UI hooks.
		function makeUrl() {
			var url = "http://" + "localhost" + ":" + 9123 + "/open?src="
					+ encodeURIComponent(this.selectedWicketSource) + "&p="
					+ encodeURIComponent("");
			return url;
		}

		// Gets the currently selected node, and if it's not a wicketsource node, shows a default message.
		if (($0 == null) || ($0.attributes.wicketsource == null)) {
			return shallowCopy(new Message());
		}

		// If it is a wicketsource node, parse it and return the json for display.
		return shallowCopy(parseNode());
	}

	update();
	chrome.experimental.devtools.panels.elements.onSelectionChanged.addListener(update);
});

