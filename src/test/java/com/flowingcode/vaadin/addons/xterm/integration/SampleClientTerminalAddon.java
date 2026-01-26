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

import com.flowingcode.vaadin.addons.xterm.ClientTerminalAddon;
import com.flowingcode.vaadin.addons.xterm.XTermBase;

@SuppressWarnings("serial")
public class SampleClientTerminalAddon extends ClientTerminalAddon {

  public final static String NAME = "sample-terminal-addon";

  public SampleClientTerminalAddon(XTermBase xterm) {
    super(xterm);

    // see https://github.com/FlowingCode/XTermConsoleAddon/issues/98
    xterm.getElement().executeJs("""
        window.Vaadin.Flow.fcXtermConnector = window.Vaadin.Flow.fcXtermConnector || {};
        window.Vaadin.Flow.fcXtermConnector.load_sample = (name, node) => {
          const addon = {
            activate: () => {},
            dispose: () => {}
          };
          node.terminal.loadAddon(addon);
          node.addons[name]=addon;
        };
        Vaadin.Flow.fcXtermConnector.load_sample($0, this);""", NAME);
  }

  public void setValue(String value) {
    executeJs("this.value=$0;", value);
  }

  @Override
  public String getName() {
    return NAME;
  }

}