// Let's create the div that wicketsource will use, and make it draggable and pretty.
var hiddenDivId = "wicket-source-chrome-div";

var wicketsourceDiv = document.getElementById(hiddenDivId);
if (!wicketsourceDiv) {
	wicketsourceDiv = document.createElement("div");
	wicketsourceDiv.setAttribute("id", hiddenDivId);

	var headerDiv = document.createElement("div");
	headerDiv.appendChild(document.createTextNode("Wicket-Source to Eclipse"));
	headerDiv.setAttribute("id", "wicket-source-chrome-headerdiv");
	wicketsourceDiv.appendChild(headerDiv);
	
	var script = document.createElement("script");
	script.setAttribute("type", "text/javascript");
	script.appendChild(document.createTextNode(ajaxFetch.toString()));
	headerDiv.appendChild(script);

	document.body.appendChild(wicketsourceDiv);
}

$(document).ready(function() {
	$("#wicket-source-chrome-div").draggable();
});

function ajaxFetch(url) {
	var script = document.getElementById("wicket-source-chrome-script");
	if (script) {
		document.body.removeChild(script);
	}
	script = document.createElement("script");
	script.setAttribute("id", "wicket-source-chrome-script");
	script.setAttribute("type", "text/javascript");
	script.setAttribute("src", url);
	document.body.appendChild(script);
}
function oldFetch(url) {
	var xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = function() {
        if(xmlhttp.readyState == 4)
        {
                if(xmlhttp.status == 200 || xmlhttp.status == 0)
                {
                        alert("Success ");
                }
                else
                {
                        alert("Failure");
                }
        }				
	};
	xmlhttp.open('GET', url, true);
	xmlhttp.setRequestHeader('Access-Control-Allow-Origin', '*');
	xmlhttp.send();
}

// Just a trial.
//ajaxFetch("http://localhost:9123/open?src=edu.academyart.lms.components.userhome%3AUserHomeProfilePanel.java%3A61");


	
