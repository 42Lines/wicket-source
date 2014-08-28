chrome.devtools.panels.elements.createSidebarPane("WicketSource", function(sidebar) {

	this.wicketsourceString = null;
	this.wicketIdString = null;
		
	function Message() {
		this.message = "Select a node with a wicketsource attribute to see details.";
	}

	// Listener event callback initiates "inspected-page context" data retrieval 
	// (which understands currently selected node in the Inspect Element tab). 
	function update() {
		chrome.devtools.inspectedWindow.eval("$0.attributes['wicket:id'].value", wicketIdEval);
		chrome.devtools.inspectedWindow.eval("$0.attributes.wicketsource.value", wicketsourceEval);
	}
	function wicketIdEval(result, isException)
	{
		if (isException) {
			this.wicketIdString = null;
		} else {
			this.wicketIdString = result;
		}
	}
	function wicketsourceEval(result, isException)
	{
		if (isException) {
		  this.wicketsourceString = null;
		} else {
		  this.wicketsourceString = result;
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
    chrome.runtime.sendMessage(
      {
        method: "getWicketsourceSidebarUrl",
        wicketsourceString: this.wicketsourceString,
        wicketIdString: this.wicketIdString
      },
      function(url) {
        sidebar.setPage(url);
      }
    );
  }
	
	
	// On the way in the first time, show an instructional message.
	update();
	
	// All further activity comes through the listener events.
	chrome.devtools.panels.elements.onSelectionChanged.addListener(update);
});

