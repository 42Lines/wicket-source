var WicketSourceForChrome = new function() {

var xmlhttp = new XMLHttpRequest();

xmlhttp.onreadystatechange = function() {
	if (this.readyState == 4) {
			clearTimeout(this.requestTimer);
			if (xmlhttp.lastRequestTimedOut) {
				xmlhttp.updateText("Error connecting. Is your SourceOpener Eclipse plugin configured and running?");
			} else {
				xmlhttp.updateText("OK");
			}
	}
};
xmlhttp.lastRequestTimedOut = false;

xmlhttp.updateText = function(text) {
	if (document.getElementById("eclipseResult").hasChildNodes()) {
		document.getElementById("eclipseResult").removeChild(
				document.getElementById("eclipseResult").firstChild);
	}
	document.getElementById("eclipseResult").appendChild(
			document.createTextNode(text));
	document.close();
};

this.fetch = function(url) {
	xmlhttp.updateText("...requesting...");
	xmlhttp.lastRequestTimedOut = false;
	xmlhttp.requestTimer = setTimeout(function() {
		xmlhttp.lastRequestTimedOut = true;
        xmlhttp.abort();
	  }, 2000);
	xmlhttp.open("GET", url, true);
	xmlhttp.send();
};




// Data container.
function WicketProperties() {
	this.wicketId = null;
	this.packageName = null;
	this.sourceLine = null;
	this.eclipseUrl = null;
}


// Parses the wicketsource attribute into a json object.
function parseNode(wicketsourceString, wicketIdString, server, port, password) {
	var wp = new WicketProperties();
	var sourceFile = "";
	var lineNumber = "";
	if (wicketsourceString == undefined) {
		return wp;
	}
	// Basic attempt to remove script injection exploits.
	wicketsourceString = wicketsourceString.replace(/'/g, "");
	wicketsourceString = wicketsourceString.replace(/\(/g, "");
	wicketsourceString = wicketsourceString.replace(/\)/g, "");
	wicketsourceString = wicketsourceString.replace(/\;/g, "");
	wicketsourceString = wicketsourceString.replace(/\&/g, "");
	wicketsourceString = wicketsourceString.replace(/\|/g, "");
	wicketsourceString = wicketsourceString.replace(/ /g, "");
	
	var pieces = wicketsourceString.split(":");
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
	if (wicketIdString) {
		wp.wicketId = wicketIdString;
	} else {
		wp.wicketId = null;
	}
	wp.eclipseUrl = "http://" + server + ":" + port + "/open?src=" + encodeURIComponent(wicketsourceString) + "&p=" + password;
	return wp;
}


function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function drawDataRow(table, id, title, value)
{
	var tr1 = document.createElement("tr");
	table.appendChild(tr1);
	var tdL = document.createElement("td");
	tdL.setAttribute("class", "dataTitle");
	tdL.appendChild(document.createTextNode(title));
	tr1.appendChild(tdL);
	var tdR = document.createElement("td");
	tdR.appendChild(document.createTextNode(value));
	tdR.setAttribute("class", "dataValue");
	tdR.setAttribute("id", id);
	tr1.appendChild(tdR);
}

function drawLinkRow(table, title, value, wp)
{
	var hiddenNodeId = "wicket-source-chrome-data";
	
	var nodeA = document.createElement("a");
	nodeA.setAttribute("id", hiddenNodeId);
	
	nodeA.setAttribute("href", "javascript:void();");
	nodeA.setAttribute("data", wp.packageName + ":" + wp.sourceLine);
	nodeA.addEventListener("click", function() { WicketSourceForChrome.fetch(wp.eclipseUrl) });
	
	nodeA.appendChild(document.createTextNode(value));

	var tr1 = document.createElement("tr");
	table.appendChild(tr1);
	var tdL = document.createElement("td");
	tdL.setAttribute("class", "dataTitle");
	tdL.appendChild(document.createTextNode(title));
	tr1.appendChild(tdL);
	var tdR = document.createElement("td");
	tdR.appendChild(nodeA);
	tdR.setAttribute("class", "dataValue");
	tr1.appendChild(tdR);	
}

function drawTable() {

	// All the data we need was presented in the url.
	var urlVars = getUrlVars();
	var ws = decodeURIComponent(urlVars["ws"]);
	var wid = decodeURIComponent(urlVars["wid"]);
	var server = decodeURIComponent(urlVars["server"]);
	var port = decodeURIComponent(urlVars["port"]);
	var password = decodeURIComponent(urlVars["p"]);
	
	var wp = parseNode(ws, wid, server, port, password);
	
	// Now that we've parsed it, display an html document with a clickable link.
	var existingTable = document.getElementById("wsTable");
	if (existingTable) { 
		document.getElementById("wsDiv").removeChild(existingTable);
	}
	
	var table = document.createElement("table");
	table.setAttribute("id", "wsTable");
	drawDataRow(table, "wicketid", "wicket:id", wid);
	drawDataRow(table, "package", "package", wp.packageName);
	drawLinkRow(table, "source", wp.sourceLine, wp);
	drawDataRow(table, "eclipseResult", "eclipseRequest", "");
	
	document.getElementById("wsDiv").appendChild(table);
}

drawTable();


};
