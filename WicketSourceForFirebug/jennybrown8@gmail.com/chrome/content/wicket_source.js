FBL.ns(function() { with (FBL) {

/**
 * WicketSource displays the wicketsource='foo.bar:HelloWorld.java:62' html attribute, and
 * produces a link that can be clicked to open the Java file in an Eclipse editor.  It does
 * so by sending an http request to a specified server, port, uri.  This port must be the
 * one that the matching Eclipse plugin is listening on, and if set, passwords must match.
 *
 * User preferences are available to configure the server, port, and timeout, but not the full uri.
 * 
 */

function WicketSourcePanel() {}

WicketSourcePanel.prototype = extend(Firebug.Panel,
{
    name: "wicket_source",
    title: "WicketSource",
    parentPanel: "html",
    order: 4,
    enableA11y: true,
    deriveA11yFrom: "console",
	xmlHttp : null,
	wicketPanel : null,
	selectedWicketSource : null,
	selectedWicketId : null,
	requestTimer : null,
    

	makeUrl : function()
	{
		var url = "http://" + prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server") 
		+ ":" + prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port") 
		+ "/open?src=" + encodeURIComponent(wicketPanel.selectedWicketSource);
		return url;
	},
	displayWicket : function() 
	{
	    var pieces = wicketPanel.selectedWicketSource.split(":");
	    var packageName = pieces[0];
	    var sourceFile = pieces[1];
	    var lineNumber = pieces[2];
	    var root = wicketPanel.getWicketSourceDomplateRoot();
	    root.wicketElement.replace({
			packageName: packageName, 
	    	sourceFile: sourceFile, 
	    	lineNumber: lineNumber, 
	    	wicketId:wicketPanel.selectedWicketId, 
	    	wicketsource:wicketPanel.selectedWicketSource,
	    	eclipseResult:wicketPanel.eclipseResult,
	    	server: prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server"),
	    	port: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port"),
	    	timeout: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout")
	    }, wicketPanel.panelNode, root);
	},	
	displayWicketEmpty : function()
	{
    	var root = wicketPanel.getWicketSourceDomplateRoot();
    	root.chooseElement.replace({}, wicketPanel.panelNode, root);
	},
	
	/**
	 * Success/failure message from the most recent http connection
	 */
	eclipseResult : "",
	getWicketSourceDomplateRoot : function() {
		return domplate (
			{
				chooseElement:
					DIV({style: "padding: 4px;"}, P("Inspect an element with a 'wicketsource' attribute to see details.")),
				wicketElement:
					DIV({style: "padding: 4px;"},
						TABLE({style: "margin-right: 10px;", onclick:"$handleClick"},
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
					),
				handleClick: function(event)
				{
					if (event.target.tagName != 'A') { return; }
					if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, click handler event ", event);
					
					wicketPanel.eclipseResult = "...requesting...";
					wicketPanel.displayWicket();
					
					var url = wicketPanel.makeUrl();
					if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, click handler launching url ", url);
					
					wicketPanel.requestTimer = setTimeout(function() {
			            if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource timed out on url, aborting.");
			            wicketPanel.eclipseResult = "ERROR, Timed out at " + prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout") + " ms.";
			            wicketPanel.displayWicket();
			            wicketPanel.xmlHttp.abort();
					  }, prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout"));
					
					wicketPanel.xmlHttp.open("GET",url,true);
					wicketPanel.xmlHttp.send();
				}
			});	
	},
    initialize: function() {
        Firebug.Panel.initialize.apply(this, arguments);
        wicketPanel = this;
        wicketPanel.xmlHttp = new XMLHttpRequest();
        wicketPanel.xmlHttp.onreadystatechange = function() {
	 		if (wicketPanel.xmlHttp.readyState == 4) {
	 			clearTimeout(wicketPanel.requestTimer);
	 	        if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource readyState, clearing timer. Status = " + wicketPanel.xmlHttp.status, wicketPanel.xmlHttp);
	 			if (wicketPanel.xmlHttp.status == 200) {
	 				wicketPanel.eclipseResult = "OK";
	 			} else {
	 				wicketPanel.eclipseResult = "Error connecting. Is your WicketSource Eclipse plugin configured and running?";
	 			}
	 			wicketPanel.displayWicket();
	 		}
	 	};
	 	wicketPanel.displayWicketEmpty();
    }	
	
});

function populatePrefPane() 
{
	if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource populate pref pane");
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
	},
		
};
prefWatcher.startup();




Firebug.WicketSourceModel = extend(Firebug.Module,
{
    showPanel: function(browser, panel) {
        var isWicketSourcePanel = panel && panel.name == "wicket_source";
        var wicket_sourceButtons = browser.chrome.$("fbwicket_sourceButtons");
        collapse(wicket_sourceButtons, !isWicketSourcePanel);

        if (!isWicketSourcePanel) return;
        var doc = panel.document;
        
        if (!this.initialized) {
            var t = doc.createTextNode('I am the Wicket Source extension. Hello World.');
            panel.panelNode.appendChild(t);
            this.initialized = true;
        }

    },
    
    onObjectSelected: function(object, panel)
    {
        var isWicketSourcePanel = panel && panel.name == "wicket_source";
        if (!isWicketSourcePanel) return;
        
        if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, onObjectSelected ", object);
        panel.eclipseResult = "";
        if (object.hasAttribute("wicketsource")) {
            panel.selectedWicketSource = object.getAttribute("wicketsource");
            panel.selectedWicketId = object.getAttribute("wicket:id"); 
            if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource calling displayWicket()");
            panel.displayWicket();
        } else {
        	panel.selectedWicketSource = null;
        	panel.selectedWicketId = null;
            if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource calling displayWicketEmpty()");
           	panel.displayWicketEmpty();
        }

    }
    
});

Firebug.registerUIListener(Firebug.WicketSourceModel);
Firebug.registerPanel(WicketSourcePanel);
Firebug.registerModule(Firebug.WicketSourceModel);




}});