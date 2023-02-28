/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2023 Flowing Code
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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

/** Server-side component for the XTerm component. */
@SuppressWarnings("serial")
@Tag("fc-xterm")
@JsModule("./fc-xterm/xterm.ts")
public class XTerm extends XTermBase
    implements ITerminalFit, ITerminalConsole, ITerminalClipboard {

  public XTerm() {
    setInsertMode(true);
  }

}
