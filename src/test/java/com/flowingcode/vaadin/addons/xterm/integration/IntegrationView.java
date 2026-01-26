/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2026 Flowing Code
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
package com.flowingcode.vaadin.addons.xterm.integration;

import com.flowingcode.vaadin.addons.xterm.ITerminalClipboard.UseSystemClipboard;
import com.flowingcode.vaadin.addons.xterm.ITerminalOptions.CursorStyle;
import com.flowingcode.vaadin.addons.xterm.TerminalHistory;
import com.flowingcode.vaadin.addons.xterm.XTerm;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route(value = IntegrationView.ROUTE)
public class IntegrationView extends VerticalLayout implements IntegrationViewCallables {

  public static final String ROUTE = "xterm/it";

  private XTerm xterm;

  private SampleClientTerminalAddon sampleClientTerminalAddon;

  public IntegrationView() {
    setSizeFull();
    setPadding(false);
    getElement().getStyle().set("background", "black");

    xterm = new XTerm();
    xterm.setPrompt("[user@xterm ~]$ ");

    xterm.writeln("xterm add-on by Flowing Code S.A.\n\n");
    xterm.writePrompt();

    xterm.setCursorBlink(true);
    xterm.setCursorStyle(CursorStyle.UNDERLINE);

    xterm.setSizeFull();

    xterm.setCopySelection(true);
    xterm.setUseSystemClipboard(UseSystemClipboard.READWRITE);
    xterm.setPasteWithMiddleClick(true);
    xterm.setPasteWithRightClick(true);

    sampleClientTerminalAddon = new SampleClientTerminalAddon(xterm);
    TerminalHistory.extend(xterm);
    xterm.addLineListener(ev -> xterm.writePrompt());

    xterm.focus();
    xterm.fit();
    add(xterm);
  }

  @Override
  @ClientCallable
  public void setSampleClientTerminalAddonValue(String value) {
    sampleClientTerminalAddon.setValue(value);
  }

}
