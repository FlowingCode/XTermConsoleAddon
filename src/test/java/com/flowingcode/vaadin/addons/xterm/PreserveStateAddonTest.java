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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PreserveStateAddonTest {
    @Test
    public void smoke() {
        new PreserveStateAddon(new XTerm());
    }

    @Test
    public void writeGoesToScrollbackBuffer() {
        final PreserveStateAddon addon = new PreserveStateAddon(new XTerm());
        addon.write("foo");
        addon.writeln("bar");
        addon.write("baz");
        assertEquals("foobar\nbaz", addon.getScrollbackBuffer());
    }

    @Test
    public void promptGoesToScrollbackBufferAfterSubmit() {
        XTerm xterm = new XTerm();
        final PreserveStateAddon addon = new PreserveStateAddon(xterm);
        Component component = xterm.getElement().getComponent().get();

        addon.setPrompt("a> ");
        addon.writePrompt();
        ComponentUtil.fireEvent(component, new ITerminalConsole.LineEvent(xterm, true, "bar"));

        addon.setPrompt("b> ");
        addon.writePrompt();
        ComponentUtil.fireEvent(component, new ITerminalConsole.LineEvent(xterm, true, "baz"));

        addon.setPrompt("c> ");
        addon.writePrompt();
        // Does not get added to buffer

        assertEquals("a> bar\nb> baz\n", addon.getScrollbackBuffer());
    }

    @Test
    public void clearClearsScrollbackBuffer() {
        final PreserveStateAddon addon = new PreserveStateAddon(new XTerm());
        addon.writeln("bar");
        addon.clear();
        assertEquals("", addon.getScrollbackBuffer());
    }

    @Test
    public void resetClearsScrollbackBuffer() {
        final PreserveStateAddon addon = new PreserveStateAddon(new XTerm());
        addon.writeln("bar");
        addon.reset();
        assertEquals("", addon.getScrollbackBuffer());
    }
}
