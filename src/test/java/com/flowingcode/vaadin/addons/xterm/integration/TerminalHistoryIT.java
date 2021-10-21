package com.flowingcode.vaadin.addons.xterm.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import com.vaadin.testbench.HasTestBenchCommandExecutor;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class TerminalHistoryIT extends AbstractXTermTest {

  @Test
  public void testArrowKeys() {
    TestBenchElement term = $("fc-xterm").first();

    WebElement input = (WebElement) waitUntil(driver -> ((HasTestBenchCommandExecutor) driver)
        .getCommandExecutor().executeScript("return arguments[0].terminal.textarea", term));

    int y = cursorPosition(term).y;
    input.sendKeys("foo1\nfoo2\n");

    assertThat(cursorPosition(term), is(at(0, y += 2)));
    assertThat(lineAtOffset(term, 0), isEmptyString());

    input.sendKeys(Keys.ARROW_UP);
    assertThat(currentLine(term), is("foo2"));

    input.sendKeys(Keys.ARROW_UP);
    assertThat(currentLine(term), is("foo1"));

    input.sendKeys(Keys.ARROW_UP);
    assertThat(currentLine(term), is("foo1"));

    input.sendKeys(Keys.ARROW_DOWN);
    assertThat(currentLine(term), is("foo2"));

    input.sendKeys(Keys.ARROW_DOWN);
    assertThat(currentLine(term), isEmptyString());
  }

  @Test
  public void testArrowKeysAndRestore() {
    TestBenchElement term = $("fc-xterm").first();

    WebElement input = (WebElement) waitUntil(driver -> ((HasTestBenchCommandExecutor) driver)
        .getCommandExecutor().executeScript("return arguments[0].terminal.textarea", term));

    int y = cursorPosition(term).y;
    input.sendKeys("foo1\nfoo2\n");

    assertThat(cursorPosition(term), is(at(0, y += 2)));
    assertThat(lineAtOffset(term, 0), isEmptyString());

    input.sendKeys("bar");
    input.sendKeys(Keys.ARROW_UP);
    assertThat(currentLine(term), is("foo2"));

    input.sendKeys(Keys.ARROW_DOWN);
    assertThat(currentLine(term), is("bar"));
  }

  @Test
  public void testPageUpDown() {
    TestBenchElement term = $("fc-xterm").first();

    WebElement input = (WebElement) waitUntil(driver -> ((HasTestBenchCommandExecutor) driver)
        .getCommandExecutor().executeScript("return arguments[0].terminal.textarea", term));

    int y = cursorPosition(term).y;
    input.sendKeys("foo1\nfoo2\nbar1\nfoo3\n");

    assertThat(cursorPosition(term), is(at(0, y += 4)));
    assertThat(lineAtOffset(term, 0), isEmptyString());

    input.sendKeys("f", Keys.PAGE_UP);
    assertThat(currentLine(term), is("foo3"));

    input.sendKeys(Keys.PAGE_UP);
    assertThat(currentLine(term), is("foo2"));

    input.sendKeys(Keys.PAGE_UP);
    assertThat(currentLine(term), is("foo1"));

    input.sendKeys(Keys.PAGE_UP);
    assertThat(currentLine(term), is("foo1"));

    input.sendKeys(Keys.PAGE_DOWN);
    assertThat(currentLine(term), is("foo2"));

    input.sendKeys(Keys.PAGE_DOWN);
    assertThat(currentLine(term), is("foo3"));

    input.sendKeys(Keys.PAGE_DOWN);
    assertThat(currentLine(term), isEmptyString());

  }

}
