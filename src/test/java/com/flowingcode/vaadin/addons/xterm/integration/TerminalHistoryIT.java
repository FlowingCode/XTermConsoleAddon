package com.flowingcode.vaadin.addons.xterm.integration;

import static com.flowingcode.vaadin.addons.xterm.integration.Position.at;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class TerminalHistoryIT extends AbstractViewTest {

  @Test
  public void testArrowKeys() {
    XTermElement term = $(XTermElement.class).first();

    int y = term.cursorPosition().y;
    term.sendKeys("foo1\nfoo2\n");

    assertThat(term.cursorPosition(), is(at(0, y += 2)));
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

    int y = term.cursorPosition().y;
    term.sendKeys("foo1\nfoo2\n");

    assertThat(term.cursorPosition(), is(at(0, y += 2)));
    assertThat(term.lineAtOffset(0), isEmptyString());

    term.sendKeys("bar");
    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo2"));

    term.sendKeys(Keys.ARROW_DOWN);
    assertThat(term.currentLine(), is("bar"));
  }

}
