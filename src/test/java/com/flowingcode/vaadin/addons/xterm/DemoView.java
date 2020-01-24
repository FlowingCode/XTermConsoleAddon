/*-
 * #%L
 * XTerm Addon
 * %%
 * Copyright (C) 2020 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.flowingcode.vaadin.addons.xterm;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.flowingcode.vaadin.addons.xterm.XTerm;
import com.flowingcode.vaadin.addons.xterm.XTermClipboard;
import com.flowingcode.vaadin.addons.xterm.XTermConsole;
import com.flowingcode.vaadin.addons.xterm.XTermFit;
import com.flowingcode.vaadin.addons.xterm.ITerminalOptions.CursorStyle;
import com.flowingcode.vaadin.addons.xterm.XTermClipboard.UseSystemClipboard;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route("")
//@CssImport("./styles/shared-styles.css")
public class DemoView extends VerticalLayout {

	private XTerm xterm;
	
	public DemoView() {
		this.setSizeFull();
		
		xterm = new XTerm();
		xterm.writeln("xterm add-on by Flowing Code S.A.\n\n");
		xterm.writeln("If you write \"time\" I'll tell you what time it is.\n");
		xterm.setCursorBlink(true);
		xterm.setCursorStyle(CursorStyle.UNDERLINE);
    	
		xterm.setSizeFull();
		xterm.loadFeature(new XTermClipboard(), clipboard->{
			clipboard.setCopySelection(true);
			clipboard.setUseSystemClipboard(UseSystemClipboard.READWRITE);
			clipboard.setPasteWithMiddleClick(true);
			clipboard.setPasteWithRightClick(true);
		});
		xterm.loadFeature(new XTermConsole(), console->{
			console.addLineListener(ev->{
				String line = ev.getLine();
				if (line.equalsIgnoreCase("time")) {
					xterm.writeln(ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
				}
				Notification.show(line);
			});
		});
		
		
    	xterm.focus();
    	
    	xterm.getFeature(XTermFit.class).ifPresent(fit->{
    		fit.fit();
    	});
    	add(xterm);    	
	}

}

