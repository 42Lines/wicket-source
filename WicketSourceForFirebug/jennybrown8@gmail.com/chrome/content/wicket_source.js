FBL.ns(function() { with (FBL) {

const Cc = Components.classes;
const Ci = Components.interfaces;	

var uniqueId = 0;
function getNextUniqueId() {
	uniqueId++;
	return uniqueId;
}

var prefWatcher = {
		prefManager : null,
		
		startup: function() {
			this.prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
			this.prefManager.getBranch("extensions.firebug.wicketsource").QueryInterface(Components.interfaces.nsIPrefBranch2);  
			this.prefManager.addObserver("", this, false);  
		},

		observe: function(subject, topic, data) {
		    if (topic != "nsPref:changed")
		    {
		      return;
		    }
		    if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource pref changed. ", data);
		}
};
prefWatcher.startup();

/**
 * WicketSource displays the wicketsource='foo.bar:HelloWorld.java:62' html attribute, and
 * produces a link that can be clicked to open the Java file in an Eclipse editor.  It does
 * so by sending an http request to a specified server, port, uri.  This port must be the
 * one that the matching Eclipse plugin is listening on, and if set, passwords must match.
 *
 * User preferences are available to configure the server, port, and timeout, but not the full uri.
 * 
 */
function WicketSourcePanel() {
}

WicketSourcePanel.prototype = extend(Firebug.Panel,
{
    name: "wicket_source" + getNextUniqueId(),
    title: "WicketSource",
    parentPanel: "html",
    order: 4,
    enableA11y: true,
    deriveA11yFrom: "console",
	wicketPanel : null,

	onObjectSelected : function(object, panel) {
		this.context.tabData.onObjectSelected(object, panel);
	},
    initialize: function() {
        Firebug.Panel.initialize.apply(this, arguments);
//        var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"].getService(Components.interfaces.nsIWindowMediator);
//        var window = wm.getMostRecentWindow("navigator:browser");
        this.context.tabData = new TabData();
        this.context.tabData.initialize(this);
        wicketPanel = this;
        if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, -- Panel Initialize -- called ", this);
    }	
});


//function populatePrefPane() 
//{
//	if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource populate pref pane");
//}



function TabData() {
}


TabData.prototype = {
		myId : getNextUniqueId(),
		wicketPanel : null,
		xmlHttp : null,
		selectedWicketSource : null,
		selectedWicketId : null,
		requestTimer : null,
		eclipseResult : "",
		
		makeUrl : function()
		{
			var url = "http://" + prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server") 
			+ ":" + prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port") 
			+ "/open?src=" + encodeURIComponent(this.selectedWicketSource);
			return url;
		},
		displayWicket : function() 
		{
		    var pieces = this.selectedWicketSource.split(":");
		    var packageName = pieces[0];
		    var sourceFile = pieces[1];
		    var lineNumber = pieces[2];
		    var root = this.getWicketSourceDomplateRoot();
		    root.wicketElement.replace({
				packageName: packageName, 
		    	sourceFile: sourceFile, 
		    	lineNumber: lineNumber, 
		    	wicketId:this.selectedWicketId, 
		    	wicketsource:this.selectedWicketSource,
		    	eclipseResult:this.eclipseResult,
		    	tabData:this,
		    	server: prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server"),
		    	port: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port"),
		    	timeout: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout")
		    }, this.wicketPanel.panelNode, this);
		},	
		displayWicketEmpty : function()
		{
	    	var root = this.getWicketSourceDomplateRoot();
	    	root.chooseElement.replace({}, this.wicketPanel.panelNode, this);
		},
		
		/**
		 * Success/failure message from the most recent http connection
		 */
		getWicketSourceDomplateRoot : function(tabData) {
			return domplate ({
					chooseElement:
						DIV({style: "padding: 4px;"}, P("Inspect an element with a 'wicketsource' attribute to see details.")),
					wicketElement:
						DIV({style: "padding: 4px;"},
							TABLE({style: "margin-right: 10px;", onclick:"$handleLinkClick"},
									TR(TD("Wicket")),
									TR(TD({style: "color: gray;"},"wicket:id "),TD(" "),TD("$wicketId")),
									TR(TD({style: "color: gray;"},"package "),TD(" "),TD("$packageName")),
									TR(TD({style: "color: gray;"},"source "),TD(" "),TD(A("$sourceFile:$lineNumber")))
							),
							P(),
							TABLE({style: "margin-right: 10px;"},
									TR(TD("Eclipse")),
									TR(TD({style: "color: gray;"},"Request"),TD(" "),TD("$eclipseResult")),
									TR(TD({style: "color: gray;"},"PluginServer"),TD(" "),TD("$server")),
									TR(TD({style: "color: gray;"},"PluginPort"),TD(" "),TD("$port")),
									TR(TD({style: "color: gray;"},"Timeout ms"),TD(" "),TD({style: "color: gray;"},"$timeout"))
							)
						)
				});	
		},
		handleLinkClick : function(event) {
			if (event.target.tagName != 'A') { return; }
			this.eclipseResult = "...requesting...";
			this.displayWicket();
			var url = this.makeUrl();
			var myTabDataRef = this;
			this.requestTimer = setTimeout(function() {
	            myTabDataRef.eclipseResult = "ERROR, Timed out at " + prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout") + " ms.";
	            myTabDataRef.displayWicket();
	            myTabDataRef.xmlHttp.abort();
			  }, prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout"));
			this.xmlHttp.open("GET",url,true);
			this.xmlHttp.send();			
		},
		onObjectSelected : function(object, panel) { 
	        this.eclipseResult = "";
	        if (object.hasAttribute("wicketsource")) {
	        	this.selectedWicketSource = object.getAttribute("wicketsource");
	            this.selectedWicketId = object.getAttribute("wicket:id"); 
	            this.displayWicket();
	        } else {
	        	this.selectedWicketSource = null;
	        	this.selectedWicketId = null;
	            this.displayWicketEmpty();
	        };
		},
		onReady : function() {
	 		if (this.readyState == 4) {
	 			clearTimeout(this.tabDataRef.requestTimer);
	 			if (this.status == 200) {
	 				this.tabDataRef.eclipseResult = "OK";
	 			} else {
	 				this.tabDataRef.eclipseResult = "Error connecting. Is your WicketSource Eclipse plugin configured and running?";
	 			}
	 			this.tabDataRef.displayWicket();
	 		}
		},
		initialize : function(wicketPane) {
			this.wicketPanel = wicketPane;
			if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, tabData ----- INITIALIZE ----- called", this);
			var myTabDataRef = this;
			this.xmlHttp = new XMLHttpRequest();
			this.xmlHttp.tabDataRef = this;
			this.xmlHttp.onreadystatechange = this.onReady;
		 	this.displayWicketEmpty();
		}
};

Firebug.WicketSourceModel = extend(Firebug.Module,
{
	isWicketSourcePanel : function(panel) {
		return (panel && panel.name.substring(0,13) == "wicket_source");	
	},
    showPanel: function(browser, panel) {
        var isWicketSourcePanel = this.isWicketSourcePanel(panel);
        var wicket_sourceButtons = browser.chrome.$("fbwicket_sourceButtons");
        collapse(wicket_sourceButtons, !isWicketSourcePanel);
        if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, showPanel called for panel ", panel);

        if (!isWicketSourcePanel) return;
        
        if (!this.initialized) {
            panel.panelNode.appendChild(panel.document.createTextNode('One-time initialization of the Wicket Source extension.'));
            this.initialized = true;
        }
    },
    onObjectSelected: function(object, panel)
    {
    	var isWicketSourcePanel = this.isWicketSourcePanel(panel);
        if (!isWicketSourcePanel) return;
        panel.onObjectSelected(object, panel);
    }
});

Firebug.registerUIListener(Firebug.WicketSourceModel);
Firebug.registerPanel(WicketSourcePanel);
Firebug.registerModule(Firebug.WicketSourceModel);
}

});