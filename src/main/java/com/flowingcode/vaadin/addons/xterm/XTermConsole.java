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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

import lombok.Getter;

/**Add console support to XTerm.
 * This provides handling of cursor, home/end, insert, delete and backspace keys, 
 * as well as a {@link #addLineListener(ComponentEventListener) line event}.*/
@JsModule("./fc-xterm/console-feature.js")
@Tag("fc-xterm-line-editor")
@SuppressWarnings("serial")
public class XTermConsole extends XTermFeature {

	//TODO set cursor properties (blink, style, width) separately for insert and overwrite modes
	
	@DomEvent("line")
	public static class LineEvent extends ComponentEvent<XTerm> {
		
		private final @Getter String line;
		
		public LineEvent(XTerm source, boolean fromClient, @EventData("event.detail") String line) {			
			super(source, fromClient);
			this.line = line;
		}

	}
	
	/** Adds a line listener. The listener is called when a line feed is intered in the console.*/
	public Registration addLineListener(ComponentEventListener<LineEvent> listener) {
		return ComponentUtil.addListener(getTerminal(), LineEvent.class, listener);
	}
	
}
