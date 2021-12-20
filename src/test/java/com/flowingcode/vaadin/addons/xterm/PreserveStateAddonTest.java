package com.flowingcode.vaadin.addons.xterm;

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
    public void promptGoesToScrollbackBuffer() {
        final PreserveStateAddon addon = new PreserveStateAddon(new XTerm());
        addon.setPrompt("a> ");
        addon.writePrompt();
        addon.writeln("bar");
        addon.setPrompt("b> ");
        addon.writePrompt();
        addon.writeln("baz");
        assertEquals("a> bar\nb> baz\n", addon.getScrollbackBuffer());
    }
}
