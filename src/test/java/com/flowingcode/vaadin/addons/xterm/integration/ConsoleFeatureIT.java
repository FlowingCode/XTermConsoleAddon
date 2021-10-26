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
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class ConsoleFeatureIT extends AbstractViewTest {

  @Test
  public void testFeature() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();

    Position pos = term.cursorPosition();

    term.sendKeys("HELLO");
    assertThat(term.currentLine(), is("HELLO"));
    assertThat(term.cursorPosition(), is(pos.plus(5, 0)));

    term.sendKeys(Keys.ARROW_LEFT);
    assertThat(term.cursorPosition(), is(pos.plus(4, 0)));

    term.sendKeys(Keys.ARROW_RIGHT);
    assertThat(term.cursorPosition(), is(pos.plus(5, 0)));

    term.sendKeys(Keys.HOME);
    assertThat(term.cursorPosition(), is(pos.plus(0, 0)));

    term.sendKeys(Keys.END);
    assertThat(term.cursorPosition(), is(pos.plus(5, 0)));

    term.sendKeys(Keys.BACK_SPACE);
    assertThat(term.currentLine(), is("HELL"));
    assertThat(term.cursorPosition(), is(pos.plus(4, 0)));

    term.sendKeys(Keys.HOME, Keys.DELETE);
    assertThat(term.currentLine(), is("ELL"));
    assertThat(term.cursorPosition(), is(pos.plus(0, 0)));

    term.sendKeys("A");
    assertThat(term.currentLine(), is("AELL"));

    term.sendKeys(Keys.INSERT, "B");
    assertThat(term.currentLine(), is("ABLL"));

    term.sendKeys(Keys.INSERT, "C");
    assertThat(term.currentLine(), is("ABCLL"));

    // long line

    int cols = term.getColumnWidth();
    String text = StringUtils.repeat("0123456789", cols / 10 + 1).substring(0, cols);

    term.sendKeys("\n");
    assertThat(term.currentLine(), is(""));
    assertThat(term.cursorPosition(), is(pos.advance(0, 1)));

    term.sendKeys(text);
    term.sendKeys(Keys.HOME);
    assertThat(term.cursorPosition(), is(pos));
    assertThat(term.currentLine(), is(text));

    term.sendKeys("A");
    assertThat(term.currentLine(), is("A" + text.substring(0, cols - 1)));
    assertThat(term.lineAtOffset(+1), is(text.substring(cols - 1)));

    term.sendKeys("B");
    assertThat(term.currentLine(), is("AB" + text.substring(0, cols - 2)));
    assertThat(term.lineAtOffset(+1), is(text.substring(cols - 2)));

    term.sendKeys(Keys.END);
    assertThat(term.cursorPosition(), is(pos.plus(2, 1)));

    term.sendKeys(Keys.HOME);
    assertThat(term.cursorPosition(), is(pos));

    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), is("B" + text.substring(0, cols - 1)));
    assertThat(term.lineAtOffset(+1), is(text.substring(cols - 1)));

    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), is(text));
    assertThat(term.lineAtOffset(+1), isEmptyString());
  }
}
