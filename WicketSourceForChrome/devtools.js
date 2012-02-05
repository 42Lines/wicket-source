chrome.devtools.panels.elements.createSidebarPane("WicketSource", function(sidebar) {

	this.wicketsourceString = null;
	this.wicketIdString = null;
		
	function Message() {
		this.message = "Select a node with a wicketsource attribute to see details.";
	}
	
	function getServer() {
		if (localStorage["server"]) {
			return localStorage["server"];
		}
		return "localhost";
	}
	function getPort() {
		if (localStorage["port"]) {
			return localStorage["port"];
		}
		return "9123";
	}
	function getPassword() {
		if (localStorage["password"]) {
			return localStorage["password"];
		}
		return "";
	}	
	
	// Listener event callback initiates "inspected-page context" data retrieval 
	// (which understands currently selected node in the Inspect Element tab). 
	function update() {
		chrome.devtools.inspectedWindow.eval("$0.attributes['wicket:id'].value", wicketIdEval);
		chrome.devtools.inspectedWindow.eval("$0.attributes.wicketsource.value", wicketsourceEval);
	}
	function wicketIdEval(result, isException)
	{
		if (!isException) { 
			this.wicketIdString = result;
		} else {
			this.wicketIdString = null;
		}
	}
	function wicketsourceEval(result, isException)
	{
		if (!isException) { 
			this.wicketsourceString = result;
		} else {
			this.wicketsourceString = null;
		}
		displaySidebar();
	}
	function displaySidebar() 
	{
		if (this.wicketsourceString == null) {
			var msg = new Message();
			sidebar.setObject(msg);
			return;
		}
		sidebar.setPage("sidebar.html?ws=" + encodeURIComponent(this.wicketsourceString) 
				+ "&wid=" + encodeURIComponent(this.wicketIdString)
				+ "&server=" + encodeURIComponent(getServer())
				+ "&port=" + encodeURIComponent(getPort())
				+ "&p=" + encodeURIComponent(getPassword())
		);
	}
	
	
	// On the way in the first time, show an instructional message.
	update();
	
	// All further activity comes through the listener events.
	chrome.devtools.panels.elements.onSelectionChanged.addListener(update);
});

