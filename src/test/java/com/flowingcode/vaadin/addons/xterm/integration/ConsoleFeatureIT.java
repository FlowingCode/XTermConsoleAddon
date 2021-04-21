/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2021 Flowing Code
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import com.flowingcode.vaadin.addons.xterm.XTermConsole;
import com.vaadin.testbench.TestBenchElement;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class ConsoleFeatureIT extends AbstractXTermTest {

  @Test
  public void testFeature() throws InterruptedException {
    TestBenchElement term = $("fc-xterm").first();
    assertFeatureHasBeenLoaded(term, XTermConsole.class, "console");
    assertFeatureHasBeenLoaded(term, null, "insertFix");

    WebElement input =
        (WebElement) getCommandExecutor().executeScript("return document.activeElement");

    int y = cursorPosition(term).y;

    input.sendKeys("HELLO");
    assertThat(currentLine(term), is("HELLO"));
    assertThat(cursorPosition(term), is(at(5, y)));

    input.sendKeys(Keys.ARROW_LEFT);
    assertThat(cursorPosition(term), is(at(4, y)));

    input.sendKeys(Keys.ARROW_RIGHT);
    assertThat(cursorPosition(term), is(at(5, y)));

    input.sendKeys(Keys.HOME);
    assertThat(cursorPosition(term), is(at(0, y)));

    input.sendKeys(Keys.END);
    assertThat(cursorPosition(term), is(at(5, y)));

    input.sendKeys(Keys.BACK_SPACE);
    assertThat(currentLine(term), is("HELL"));
    assertThat(cursorPosition(term), is(at(4, y)));

    input.sendKeys(Keys.HOME, Keys.DELETE);
    assertThat(currentLine(term), is("ELL"));
    assertThat(cursorPosition(term), is(at(0, y)));

    input.sendKeys("A");
    assertThat(currentLine(term), is("ALL"));

    input.sendKeys(Keys.INSERT, "B");
    assertThat(currentLine(term), is("ABLL"));

    // long line

    int cols = getColumnWidth(term);
    String text = StringUtils.repeat("0123456789", cols / 10 + 1).substring(0, cols);

    input.sendKeys("\n");
    assertThat(currentLine(term), is(""));
    assertThat(cursorPosition(term), is(at(0, ++y)));

    input.sendKeys(text);
    input.sendKeys(Keys.HOME);
    assertThat(cursorPosition(term), is(at(0, y)));
    assertThat(currentLine(term), is(text));

    input.sendKeys("A");
    assertThat(currentLine(term), is("A" + text.substring(0, cols - 1)));
    assertThat(lineAtOffset(term, +1), is(text.substring(cols - 1)));

    input.sendKeys("B");
    assertThat(currentLine(term), is("AB" + text.substring(0, cols - 2)));
    assertThat(lineAtOffset(term, +1), is(text.substring(cols - 2)));

    input.sendKeys(Keys.END);
    assertThat(cursorPosition(term), is(at(2, y + 1)));

    input.sendKeys(Keys.HOME);
    assertThat(cursorPosition(term), is(at(0, y)));

    input.sendKeys(Keys.DELETE);
    assertThat(currentLine(term), is("B" + text.substring(0, cols - 1)));
    assertThat(lineAtOffset(term, +1), is(text.substring(cols - 1)));

    input.sendKeys(Keys.DELETE);
    assertThat(currentLine(term), is(text));
    assertThat(lineAtOffset(term, +1), isEmptyString());
  }
}
