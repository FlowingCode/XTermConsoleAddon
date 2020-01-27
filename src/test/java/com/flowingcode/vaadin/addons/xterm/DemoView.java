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


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.flowingcode.vaadin.addons.xterm.ITerminalOptions.BellStyle;
import com.flowingcode.vaadin.addons.xterm.ITerminalOptions.CursorStyle;
import com.flowingcode.vaadin.addons.xterm.XTermClipboard.UseSystemClipboard;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route("")
public class DemoView extends VerticalLayout {

	private XTerm xterm;
	
	public DemoView() {
		this.setSizeFull();
		setPadding(false);
		getElement().getStyle().set("background", "black");
		
		
		xterm = new XTerm();
		xterm.writeln("xterm add-on by Flowing Code S.A.\n\n");
		xterm.writeln("Commands: time, date, beep\n");
		xterm.setCursorBlink(true);
		xterm.setCursorStyle(CursorStyle.UNDERLINE);
		xterm.setBellStyle(BellStyle.SOUND);
    	
		xterm.setSizeFull();
		xterm.loadFeature(new XTermClipboard(), clipboard->{
			clipboard.setCopySelection(true);
			clipboard.setUseSystemClipboard(UseSystemClipboard.READWRITE);
			clipboard.setPasteWithMiddleClick(true);
			clipboard.setPasteWithRightClick(true);
		});
		xterm.loadFeature(new XTermConsole(), console->{
			console.addLineListener(ev->{
				switch (ev.getLine().toLowerCase()) {
				case "time": 
					xterm.writeln(LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_TIME));
					break;
				case "date":
					xterm.writeln(LocalDate.now().toString());
					break;
				case "beep":
					xterm.write("\u0007");
					break;
				}
				Notification.show(ev.getLine());
			});
		});
		
		
    	xterm.focus();
    	
    	xterm.getFeature(XTermFit.class).ifPresent(fit->{
    		fit.fit();
    	});
    	add(xterm);    	
	}

}

