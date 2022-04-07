[![Build Status](https://jenkins.flowingcode.com/job/XTerm-2-addon/badge/icon)](https://jenkins.flowingcode.com/job/XTerm-2-addon)

# XTerm Console Add-on

Vaadin 14+ Java integration of [xterm.js](https://xtermjs.org/) terminal emulator.

## Features

* Send input text to server
* Programmatically write to the console
* Clipboard support 
* Command line edition (cursor keys, insert, etc.)
* ANSI escape sequences
* And much more...

## Online demo

* [Vaadin 14](http://addonsv14.flowingcode.com/xterm) (Addon version 1.x)
* [Vaadin 23](http://addonsv23.flowingcode.com/xterm) (Addon version 2.x)

## Download release

[Available in Vaadin Directory](https://vaadin.com/directory/component/xterm-console-addon)

## Building and running demo

- git clone repository
- mvn clean install jetty:run

To see the demo, navigate to http://localhost:8080/

## Release notes

See [here](https://github.com/FlowingCode/XTermConsoleAddon/releases)

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:

- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it

## License & Author

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

XTermConsoleAddon is written by Flowing Code S.A.

# Developer Guide

## Getting started

Initialize a terminal with autosize, clipboard support and listener:
```
XTerm xterm = new XTerm();
xterm.writeln("Hello world.\n\n");
xterm.setCursorBlink(true);
xterm.setCursorStyle(CursorStyle.UNDERLINE);
    	
xterm.setSizeFull();
xterm.setCopySelection(true);
xterm.setUseSystemClipboard(UseSystemClipboard.READWRITE);
xterm.setPasteWithRightClick(true);

xterm.addLineListener(ev->{
    String line = ev.getLine();
    System.out.println(line);
});	
		
xterm.focus();
    	
xterm.fit();
```
