//(function() {


	
chrome.experimental.devtools.panels.elements.createSidebarPane("WicketSource", function(sidebar) {
	
	var currentWP = null;
	
	function update() {
		sidebar.setExpression("(" + page_getProperties.toString() + ")()", "Component Properties");
//		for (var key in chrome.experimental.devtools.panels.elements) { 
//			alert("key " + key + " valu " + chrome.experimental.devtools.panels.elements[key] );
//		}
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
			this.debug = null;
			this.eclipseResult = null;

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

		function makeUrl() {
			var url = "http://" + "localhost" + ":" + 9123 + "/open?src="
					+ encodeURIComponent(this.selectedWicketSource) + "&p="
					+ encodeURIComponent("");
			return url;
		}


		if (($0 == null) || ($0.attributes.wicketsource == null)) {
			return shallowCopy(new Message());
		}
		
		return shallowCopy(parseNode());
	}

	function _goEclipse(event) {
		event.stopPropagation();
		this.expanded = true;
	}
	
	update();
	chrome.experimental.devtools.panels.elements.onSelectionChanged.addListener(update);
	
//	chrome.extension.sendRequest({greeting: "hello"}, function(response) {
//		  alert(response.farewell);
//	});
//	var button = new ExtensionButton('goeclipse', 'go_icon.png', "Go Eclipse", false);
});

//})();