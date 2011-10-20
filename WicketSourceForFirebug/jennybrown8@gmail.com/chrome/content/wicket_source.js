FBL.ns(function() { with (FBL) {

/**
 * WicketSource displays the wicketsource='foo.bar:HelloWorld.java:62' html attribute, and
 * produces a link that can be clicked to open the Java file in an Eclipse editor.  It does
 * so by sending an http request to a specified server, port, uri.  This port must be the
 * one that the matching Eclipse plugin is listening on.
 *
 * User preferences available to configure the server, port, and timeout, but not the full uri.
 * 
 */

function wicket_sourcePanel() {}

wicket_sourcePanel.prototype = extend(Firebug.Panel,
{
    name: "wicket_source",
    title: "WicketSource",
    parentPanel: "html",
    order: 4,
    enableA11y: true,
    deriveA11yFrom: "console",
    initialize: function() {
        Firebug.Panel.initialize.apply(this, arguments);
        wicketPanel = this;
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

var wicketPanel = null;
var selectedWicketSource = null;
var selectedWicketId = null;
var requestTimer = null;
/**
 * Success/failure message from the most recent http connection
 */
var eclipseResult = "";

var xmlHttp = new XMLHttpRequest();
xmlHttp.onreadystatechange = function() {
	if (xmlHttp.readyState == 4) {
		clearTimeout(requestTimer);
        if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource readyState, clearing timer. Status = " + xmlHttp.status, xmlHttp);
		if (xmlHttp.status == 200) {
			eclipseResult = "OK";
		} else {
			eclipseResult = "Error connecting. Is your WicketSource Eclipse plugin configured and running?";
		}
		displayWicket();
	}
};

var wicket_sourceRep = domplate (
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
		
		eclipseResult = "...requesting...";
        displayWicket();
		
		var url = makeUrl();
		if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, click handler launching url ", url);
		
		requestTimer = setTimeout(function() {
            if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource timed out on url, aborting.");
            eclipseResult = "ERROR, Timed out at " + MAXIMUM_WAITING_TIME + " ms.";
            displayWicket();
			xmlHttp.abort();
       
		  }, prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout"));
		
		xmlHttp.open("GET",url,true);
		xmlHttp.send();
	}
});

function makeUrl()
{
	var url = "http://" + prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server") 
	+ ":" + prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port") 
	+ "/open?src=" + encodeURIComponent(selectedWicketSource);
	return url;
}


function displayWicket()
{
    var pieces = selectedWicketSource.split(":");
    var packageName = pieces[0];
    var sourceFile = pieces[1];
    var lineNumber = pieces[2];
    var rootTemplateElement = wicket_sourceRep.wicketElement.replace({
		packageName: packageName, 
    	sourceFile: sourceFile, 
    	lineNumber: lineNumber, 
    	wicketId:selectedWicketId, 
    	wicketsource:selectedWicketSource,
    	eclipseResult:eclipseResult,
    	server: prefWatcher.prefManager.getCharPref("extensions.firebug.wicketsource.server"),
    	port: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.port"),
    	timeout: prefWatcher.prefManager.getIntPref("extensions.firebug.wicketsource.timeout")
    }, wicketPanel.panelNode, wicket_sourceRep);
}

Firebug.wicket_sourceModel = extend(Firebug.Module,
{
    showPanel: function(browser, panel) {
        var iswicket_sourcePanel = panel && panel.name == "wicket_source";
        var wicket_sourceButtons = browser.chrome.$("fbwicket_sourceButtons");
        collapse(wicket_sourceButtons, !iswicket_sourcePanel);

        if (!iswicket_sourcePanel) return;
        var doc = panel.document;
        
        if (!this.initialized) {
            var t = doc.createTextNode('I am the Wicket Source extension. Hello World.');
            panel.panelNode.appendChild(t);
            this.initialized = true;
        }

    },
    
    onObjectSelected: function(object, panel)
    {
        if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource, onObjectSelected ", object);
        eclipseResult = "";
        if (object.hasAttribute("wicketsource")) {
            selectedWicketSource = object.getAttribute("wicketsource");
            selectedWicketId = object.getAttribute("wicket:id"); 
            displayWicket();
        } else {
        	selectedWicketSource = null;
        	selectedWicketId = null;
            if (FBTrace.DBG_PANELS) FBTrace.sysout("wicketsource calling displayInfo");
            if (wicketPanel != null) {
            	var rootTemplateElement = wicket_sourceRep.chooseElement.replace({}, wicketPanel.panelNode, wicket_sourceRep);
            }
        }

    }
    
});

Firebug.registerUIListener(Firebug.wicket_sourceModel);
Firebug.registerPanel(wicket_sourcePanel);
Firebug.registerModule(Firebug.wicket_sourceModel);




}});