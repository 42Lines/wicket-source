FBL.ns(function() { with (FBL) {

// documentation references: 
// https://developer.mozilla.org/en/Firebug_internals
// http://getfirebug.com/wiki/index.php/Domplate
// https://developer.mozilla.org/en-US

// These are suggested shortcuts but I'm not using the abbreviations anywhere.
//const Cc = Components.classes;
//const Ci = Components.interfaces;	

/**
 * Gives access to the preferences settings for this plugin. 
 * 
 * Also observes changes in the preferences settings for this plugin; no meaningful
 * functionality at this time but it's a placeholder for later behavior.
 */
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
 * Display panel overlay for Firebug.  This displays only when the left side panel is
 * the HTML panel, and it is an extra tab on the right side panel from there.
 * A new panel is created for every browser tab the user creates, and tab-specific
 * data must be kept in "this.context.variable" to avoid polluting other tabs.
 * 
 * WicketSource displays the wicketsource='foo.bar:HelloWorld.java:62' html attribute, and
 * produces a link that can be clicked to open the Java file in an Eclipse editor.  It does
 * so by sending an http request to a specified server, port, uri.  This port must be the
 * one that the matching Eclipse plugin is listening on, and if set, passwords must match.
 *
 * User preferences are available to configure the server, port, and timeout, but not the full uri.
 * 
 * @author Jenny Brown
 * 
 */
function WicketSourcePanel() {
}

WicketSourcePanel.prototype = extend(Firebug.Panel,
{
    name: "wicket_source",
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
        this.context.tabData = new TabData();
        this.context.tabData.initialize(this);
        wicketPanel = this;
    }	
});

/**
 * TabData stores the core data model information for this plugin, and
 * it is reproduced for each tab the user has open.  It handles display
 * of the "WicketSource" html contents (properties table, current dom node selection,
 * and result of eclipse http request). Most of the interesting 
 * extension-specific behaviors are in here.
 */
function TabData() {
}

TabData.prototype = {
		wicketPanel : null,
		xmlHttp : null,
		selectedWicketSource : null,
		selectedWicketId : null,
		requestTimer : null,
		eclipseResult : "",
		
		/**
		 * How to speak to the Eclipse server
		 * @returns {String}
		 */
		makeUrl : function()
		{
			// Basic attempt to remove script injection exploits.
			var wicketsourceString = this.selectedWicketSource;
			wicketsourceString = wicketsourceString.replace(/'/g, "");
			wicketsourceString = wicketsourceString.replace(/\(/g, "");
			wicketsourceString = wicketsourceString.replace(/\)/g, "");
			wicketsourceString = wicketsourceString.replace(/\;/g, "");
			wicketsourceString = wicketsourceString.replace(/\&/g, "");
			wicketsourceString = wicketsourceString.replace(/\|/g, "");
			wicketsourceString = wicketsourceString.replace(/ /g, "");
			
			var url = "http://" + prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server") 
			+ ":" + prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port") 
			+ "/open?src=" + encodeURIComponent(wicketsourceString)
			+ "&p=" + encodeURIComponent(prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.password"));
			if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource made url ", url);
			return url;
		},
		/**
		 * When inspecting an element with a wicketsource='' attribute, this function
		 * displays the html table with all that data.
		 */
		displayWicket : function() 
		{
			var packageName = "";
			var sourceFile = "";
			var lineNumber = "";
			var sourceLine = "";
			if (this.selectedWicketSource!="" && this.selectedWicketSource!=":") {
			    var pieces = this.selectedWicketSource.split(":");
			    if (pieces.length == 3) { 
				    packageName = pieces[0];
				    sourceFile = pieces[1];
				    lineNumber = pieces[2];
			    } else if (pieces.length == 2) {
				    sourceFile = pieces[0];
				    lineNumber = pieces[1];
			    }
			    sourceLine = sourceFile + ":" + lineNumber;
			}
			
		    var root = this.getWicketSourceDomplateRoot();
		    root.wicketElement.replace({
				packageName: packageName, 
				sourceLine: sourceLine,
		    	wicketId:this.selectedWicketId, 
		    	wicketsource:this.selectedWicketSource,
		    	eclipseResult:this.eclipseResult,
		    	tabData:this,
		    	server: prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server"),
		    	port: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port"),
		    	timeout: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout")
		    }, this.wicketPanel.panelNode, this);
		},	
		/**
		 * When inspecting an element with no wicketsource='' attribute or not 
		 * inspecting any element at all, this function displays simple text message 
		 * with instructions.
		 */
		displayWicketEmpty : function()
		{
	    	var root = this.getWicketSourceDomplateRoot();
	    	root.chooseElement.replace({}, this.wicketPanel.panelNode, this);
		},
		
		// Best resource for domplate documentation: http://getfirebug.com/wiki/index.php/Domplate
		/**
		 * Produces HTML template for ui display, and adds a click handler reference for the Eclipse hyperlink.
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
									TR(TD({style: "color: gray;"},"source "),TD(" "),TD(A("$sourceLine")))
									,
//							),
//							P(),
//							TABLE({style: "margin-right: 10px;"},
//									TR(TD("Eclipse")),
									TR(TD({style: "color: gray;"},"Request"),TD(" "),TD("$eclipseResult"))
//									,
//									TR(TD({style: "color: gray;"},"PluginServer"),TD(" "),TD("$server")),
//									TR(TD({style: "color: gray;"},"PluginPort"),TD(" "),TD("$port")),
//									TR(TD({style: "color: gray;"},"Timeout ms"),TD(" "),TD({style: "color: gray;"},"$timeout"))
							)
						)
				});	
		},
		/**
		 * Handler for a click on the domplate href; prepares an http request to Eclipse and 
		 * gets the request started.
		 * @param event
		 */
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
		/**
		 * When the user clicks between HTML dom nodes in the Firebug HTML panel, this 
		 * chains off of that selection event and updates the WicketSource panel to match.
		 * @param object
		 * @param panel
		 */
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
		/**
		 * When the Eclipse http request completes (success or failure), this
		 * updates the status display and cancels the timeout timer.
		 */
		onReady : function() {
	 		if (this.readyState == 4) {
	 			clearTimeout(this.tabDataRef.requestTimer);
	 			if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, xml http response ", this);
	 			if (this.status == 200) {
	 				this.tabDataRef.eclipseResult = "OK";
	 			} else if (this.status == 400) {
	 				this.tabDataRef.eclipseResult = "Server response - " + this.responseText + "";
	 			} else {
	 				this.tabDataRef.eclipseResult = "Error connecting. Is your SourceOpener Eclipse plugin configured and running?";
	 			}
	 			this.tabDataRef.displayWicket();
	 		}
		},
		/**
		 * Sets up the TabData initial values; effectively this is a constructor.
		 * Defaults to displaying the instructional message since the user hasn't
		 * had time to select a DOM node yet.
		 * @param wicketPane
		 */
		initialize : function(wicketPane) {
			this.wicketPanel = wicketPane;
			this.xmlHttp = new XMLHttpRequest();
			this.xmlHttp.tabDataRef = this;
			this.xmlHttp.onreadystatechange = this.onReady;
		 	this.displayWicketEmpty();
		}
};

/**
 * Event handler (UI Listener) to integrate with browser behaviors.
 * 
 * showPanel is called as the browser loads and as new
 * tabs are requested or switched to.  Most of this
 * code was auto-generated by the Eclipse PDE plug-in 
 * template, and then slightly modified by me.
 * 
 * onObjectSelected is called when the user selects
 * a DOM node in the HTML Panel of Firebug.
 * 
 * Events are forwarded to the panel, because it has access to 
 * this.context for tab-specific data, which is necessary to 
 * properly handle the events.
 *  
 */
Firebug.WicketSourceModel = extend(Firebug.Module,
{
	isWicketSourcePanel : function(panel) {
		return (panel && panel.name == "wicket_source");	
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

/*
 * These lines were autogenerated by the PDE template, to register components.
 */
Firebug.registerUIListener(Firebug.WicketSourceModel);
Firebug.registerPanel(WicketSourcePanel);
Firebug.registerModule(Firebug.WicketSourceModel);
}

});