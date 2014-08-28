chrome.runtime.onMessage.addListener(function(request, sender, callback) {
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

  if (request.method == "getWicketsourceSidebarUrl") {
    var url = "sidebar.html?ws=" + encodeURIComponent(request.wicketsourceString)
                        + "&wid=" + encodeURIComponent(request.wicketIdString)
                        + "&server=" + encodeURIComponent(getServer())
                        + "&port=" + encodeURIComponent(getPort())
                        + "&p=" + encodeURIComponent(getPassword());
    callback(url);
  }
  else {
    callback(null);
  }
});
