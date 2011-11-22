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

function WicketSource() {

};
*/




// The function below is executed in the context of the inspected
// page because it is called from sidebar.setExpression.

var page_getProperties = function() {
	
	// Data container.
	function WicketProperties() {
		this.wicketId = "a";
		this.packageName = "b";
		this.className = "c";
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
	};
	
	var wp = new WicketProperties();
	wp.className = shallowCopy( window.jQuery && $0 ? jQuery.data($0) : {});

	return shallowCopy(wp);
};

chrome.experimental.devtools.panels.elements.createSidebarPane("WicketSource",
		function(sidebar) {

			function updateElementProperties(stuff) {
				sidebar.setExpression("(" + page_getProperties.toString() + ")()", "WicketProperties");
			}
			updateElementProperties();
			chrome.experimental.devtools.panels.elements.onSelectionChanged
					.addListener(updateElementProperties);
		});
