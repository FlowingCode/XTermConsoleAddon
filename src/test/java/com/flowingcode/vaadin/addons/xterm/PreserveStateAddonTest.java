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
