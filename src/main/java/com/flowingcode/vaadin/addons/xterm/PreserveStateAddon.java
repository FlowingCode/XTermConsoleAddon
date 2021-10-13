package com.flowingcode.vaadin.addons.xterm;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Add-on which preserves the client-side state when the component is removed
 * from the UI then reattached later on.
 * <p></p>
 * To use this addon, simply create the addon then make sure to call all {@link ITerminal}
 * and {@link ITerminalOptions} methods via this addon:
 * <pre>
 * final XTerm xterm = new XTerm();
 * final PreserveStateAddon addon = new PreserveStateAddon(xterm);
 * addon.writeln("Hello!");
 * addon.write("$ ");
 * </pre>
 */
public class PreserveStateAddon implements ITerminal {
    private final XTermBase xterm;
    private final StringBuilder scrollbackBuffer = new StringBuilder();
    /**
     * All commands are properly applied before the first attach; they're just
     * not preserved after subsequent detach/attach.
     */
    private boolean wasDetachedOnce = false;

    public PreserveStateAddon(XTerm xterm) {
        this.xterm = Objects.requireNonNull(xterm);
        xterm.addAttachListener(e -> {
            if (wasDetachedOnce) {
                xterm.write(scrollbackBuffer.toString());
            }
        });
        xterm.addDetachListener(e -> wasDetachedOnce = true);
        xterm.addLineListener(e -> {
            // also make sure that any user input ends up in the scrollback buffer.
            scrollbackBuffer.append(e.getLine());
            scrollbackBuffer.append('\n');
        });
    }

    @Override
    public void blur() {
        xterm.blur();
    }

    @Override
    public void focus() {
        xterm.focus();
    }

    @Override
    public CompletableFuture<Boolean> hasSelection() {
        return xterm.hasSelection();
    }

    @Override
    public CompletableFuture<String> getSelection() {
        return xterm.getSelection();
    }

    @Override
    public void clearSelection() {
        xterm.clearSelection();
    }

    @Override
    public void select(int column, int row, int length) {
        xterm.select(column, row, length);
    }

    @Override
    public void selectAll() {
        xterm.selectAll();
    }

    @Override
    public void selectLines(int start, int end) {
        xterm.selectLines(start, end);
    }

    @Override
    public void scrollLines(int amount) {
        xterm.scrollLines(amount);
    }

    @Override
    public void scrollPages(int pageCount) {
        xterm.scrollPages(pageCount);
    }

    @Override
    public void scrollToTop() {
        xterm.scrollToTop();
    }

    @Override
    public void scrollToBottom() {
        xterm.scrollToBottom();
    }

    @Override
    public void scrollToLine(int line) {
        xterm.scrollToLine(line);
    }

    @Override
    public void clear() {
        xterm.clear();
        scrollbackBuffer.delete(0, scrollbackBuffer.length());
    }

    @Override
    public void write(String data) {
        xterm.write(data);
        scrollbackBuffer.append(data);
    }

    @Override
    public void writeln(String data) {
        xterm.writeln(data);
        scrollbackBuffer.append(data);
        scrollbackBuffer.append('\n');
    }

    @Override
    public void paste(String data) {
        xterm.paste(data);
    }

    @Override
    public void refresh(int start, int end) {
        xterm.refresh(start, end);
    }

    @Override
    public void reset() {
        xterm.reset();
    }

    @Override
    public void resize(int columns, int rows) {
        xterm.resize(columns, rows);
    }
}
