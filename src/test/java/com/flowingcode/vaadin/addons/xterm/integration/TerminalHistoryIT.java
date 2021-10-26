package com.flowingcode.vaadin.addons.xterm.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class TerminalHistoryIT extends AbstractViewTest {

  @Test
  public void testArrowKeys() {
    XTermElement term = $(XTermElement.class).first();

    Position pos = term.cursorPosition();
    term.sendKeys("foo1\nfoo2\n");

    assertThat(term.cursorPosition(), is(pos.advance(0, 2)));
    assertThat(term.lineAtOffset(0), isEmptyString());

    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo2"));

    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo1"));

    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo1"));

    term.sendKeys(Keys.ARROW_DOWN);
    assertThat(term.currentLine(), is("foo2"));

    term.sendKeys(Keys.ARROW_DOWN);
    assertThat(term.currentLine(), isEmptyString());
  }

  @Test
  public void testArrowKeysAndRestore() {
    XTermElement term = $(XTermElement.class).first();

    Position pos = term.cursorPosition();
    term.sendKeys("foo1\nfoo2\n");

    assertThat(term.cursorPosition(), is(pos.advance(0, 2)));
    assertThat(term.lineAtOffset(0), isEmptyString());

    term.sendKeys("bar");
    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo2"));

    term.sendKeys(Keys.ARROW_DOWN);
    assertThat(term.currentLine(), is("bar"));
  }

}
