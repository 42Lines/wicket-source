Wicket-Source provides click-through from browser HTML back to the original Wicket components.

## Features

1. Wicket Module "wicketsource" - records where in the code each component is constructed. Adds an HTML attribute.
2. Firebug Extension "WicketSource" - displays html attribute and lets you click to open.
3. Eclipse plugin "Source Opener" - listens for clicks from Firefox and opens the file to that line.

## Installation

See the [Wiki page](https://github.com/42Lines/wicket-source/wiki) for deeper details.

1. Install the wicket-source jar into your project using maven or your preferred build tool.  Add an `AttributeModifyingInstantiationListener` in your `WicketApplication` class with `addComponentInstantionListener`.  Add an `AttributeModifyingComponentVisitor` to your base `Page` class for your application, and make it a visitor to the page during `onBeforeRender()`. `locationTagger.addClassNameVisitor(this);`
2. Get Firebug first if you don't have it; then install the Firefox extension by clicking on the .xpi file.
3. Install SourceOpener by putting its .jar into the eclipse drop-ins folder and restarting Eclipse. Then "Show View" / "Source Opener, Recent File Locations" to get to the tab.


## Notes

This is very young software and still heavily in testing/development.  Feedback welcome, reliability uncertain - for a while yet, anyway. 

