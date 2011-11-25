// -------- begin clip from sample extension -------------
/*
 function Console() {
 }

 Console.Type = {
 LOG : "log",
 DEBUG : "debug",
 INFO : "info",
 WARN : "warn",
 ERROR : "error",
 GROUP : "group",
 GROUP_COLLAPSED : "groupCollapsed",
 GROUP_END : "groupEnd"
 };

 Console.addMessage = function(type, format, args) {
 chrome.extension.sendRequest({
 command : "sendToConsole",
 tabId : chrome.experimental.devtools.tabId,
 args : escape(JSON.stringify(Array.prototype.slice.call(arguments, 0)))
 });
 };

 // Generate Console output methods, i.e. Console.log(), Console.debug() etc.
 (function() {
 var console_types = Object.getOwnPropertyNames(Console.Type);
 for ( var type = 0; type < console_types.length; ++type) {
 var method_name = Console.Type[console_types[type]];
 Console[method_name] = Console.addMessage.bind(Console, method_name);
 }
 })();
 // -------- end -------------
 */

// The function below is executed in the context of the inspected
// page because it is called from sidebar.setExpression.
var page_getProperties = function() {
	function Message()  {
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
	;
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
		return wp;
	}
	;
	function makeUrl() {
		var url = "http://" + "localhost" + ":" + 9123 + "/open?src="
				+ encodeURIComponent(this.selectedWicketSource) + "&p="
				+ encodeURIComponent("");
		return url;
	}
	;
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

	if (($0 == null) || ($0.attributes.wicketsource == null)) {
		return shallowCopy(new Message());
	}
	var wp = parseNode();
	return shallowCopy(wp);
};

chrome.experimental.devtools.panels.elements.createSidebarPane("WicketSource",
		function(sidebar) {
			var addButton = document.createElement("button");
			addButton.className = "pane-title-button goeclipse";
			addButton.id = "go-eclipse-button-id";
//			addButton.title = WebInspector.UIString("View in Eclipse");
////			addButton.addEventListener("click", _goEclipse.bind(this),false);
//			this.titleElement.appendChild(addButton);
			
			
			function updateElementProperties() {
				sidebar.setExpression("(" + page_getProperties.toString()
						+ ")()", "Component Properties");
			}
			updateElementProperties();
			chrome.experimental.devtools.panels.elements.onSelectionChanged
					.addListener(updateElementProperties);
			
			function _goEclipse(event) {
		        event.stopPropagation();
		        this.expanded = true;
			}
			this.prototype.__proto__ = WebInspector.SidebarPane.prototype;
		});
