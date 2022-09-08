/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2022 Flowing Code
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

import org.apache.commons.lang3.StringUtils;

public class XTermTestUtils {

  /** Returns a line that runs up to the right margin */
  public static String makeFullLine(XTermElement term, boolean hasPrompt) {
    int cols = term.getColumnWidth();
    int x = hasPrompt ? term.cursorPosition().x : 0;
    return StringUtils.repeat("0123456789", cols / 10 + 1).substring(0, cols - x);
  }

}
