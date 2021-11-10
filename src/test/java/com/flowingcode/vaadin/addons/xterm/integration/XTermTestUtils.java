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
