Wicket-Source speeds up wicket development by providing click-through from browser HTML back to the original Wicket components in your source.

## Features

1. Wicket Module "wicketsource" - records where in the code each component is constructed. Adds an HTML attribute.
2. Browser Extension "WicketSource" - displays html attribute and lets you click to open (for Firefox+Firebug and for Chrome).
3. Eclipse plugin "Source Opener" - listens for clicks from browser and opens the file to that line.

## Installation

See the [Wiki page](https://github.com/42Lines/wicket-source/wiki) for deeper details.

1. Install the wicket-source jar into your project using maven or your preferred build tool.  Add it to your WicketApplication `init()` as
     `WicketSource.configure(this);`


2. Get Firebug first if you don't have it; then install the Firefox extension by clicking on the .xpi file.

3. Or, if you prefer Chrome, click the chrome extension WicketSourceForChrome.crx from the downloads area. 

4. Install SourceOpener by putting its .jar into the eclipse drop-ins folder and restarting Eclipse. Then "Show View" / "Source Opener, Recent File Locations" to get to the tab.


## Notes

Feedback and bug reports welcome. See the [Issue Tracker](https://github.com/42Lines/wicket-source/issues). 
