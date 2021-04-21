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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/** Enables fitting the terminal's dimensions to a containing component */
@NpmPackage(value = "xterm-addon-fit", version = "0.3.0")
@JsModule("./fc-xterm/fit-feature.js")
@Tag("fc-xterm-fit")
@SuppressWarnings("serial")
public class XTermFit extends XTermFeature {

  public void fit() {
    getElement().executeJs("this._fitAddon.fit()");
  }

  public void setFitOnResize(boolean value) {
    getElement().setProperty("fitOnResize", value);
  }

  public boolean isFitOnResize() {
    return getElement().getProperty("fitOnResize", false);
  }
}
