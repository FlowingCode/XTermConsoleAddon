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
package com.flowingcode.vaadin.addons.xterm.integration;

import static com.flowingcode.vaadin.addons.xterm.integration.XTermTestUtils.makeFullLine;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class ConsoleFeatureIT extends AbstractViewTest {

  @Test
  public void testWriteWrappedLine() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();
    Position home = term.cursorPosition();
    String text = makeFullLine(term, true) + "Z";
    term.sendKeys(text);
    assertThat(term.currentLine(), is(text));
    assertThat(term.cursorPosition(), is(new Position(1, home.y + 1)));
    assertThat(term.lineAtOffset(0), is("Z"));
  }

  @Test
  public void testFeature() throws InterruptedException {
    // navigation with keyboard
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
    term.sendKeys(Keys.ENTER);
    assertThat(term.currentLine(), is(""));
    assertThat(term.cursorPosition(), is(pos.advance(0, 1)));

    String prompt = term.lineAtOffset(0);
    String text = makeFullLine(term, true);
    int cols = text.length();

    term.sendKeys(text);
    term.sendKeys(Keys.HOME);
    assertThat(term.cursorPosition(), is(pos));
    assertThat(term.currentLine(), is(text));

    term.sendKeys("A");
    assertThat(term.lineAtOffset(0), is(prompt + "A" + text.substring(0, cols - 1)));
    assertThat(term.lineAtOffset(1), is(text.substring(cols - 1)));

    term.sendKeys("B");
    assertThat(term.lineAtOffset(0), is(prompt + "AB" + text.substring(0, cols - 2)));
    assertThat(term.lineAtOffset(1), is(text.substring(cols - 2)));

    term.sendKeys(Keys.END);
    assertThat(term.cursorPosition(), is(new Position(2, pos.y + 1)));

    term.sendKeys(Keys.HOME);
    assertThat(term.cursorPosition(), is(pos));

    term.sendKeys(Keys.DELETE);
    assertThat(term.lineAtOffset(0), is(prompt + "B" + text.substring(0, cols - 1)));
    assertThat(term.lineAtOffset(1), is(text.substring(cols - 1)));

    term.sendKeys(Keys.DELETE);
    assertThat(term.lineAtOffset(0), is(prompt + text));
    assertThat(term.lineAtOffset(1), isEmptyString());
  }

  @Test
  public void testCsiSequences() throws InterruptedException {
    // CSI sequences that implement navigation with keyboard
    XTermElement term = $(XTermElement.class).first();
    Position pos = term.cursorPosition();

    term.sendKeys("HELLO");
    assertThat(term.currentLine(), is("HELLO"));

    // Cursor Home Logical Line
    term.write("\u001b[<H");
    assertThat(term.cursorPosition(), is(pos));

    // Cursor End Logical Line
    term.write("\u001b[<E");
    assertThat(term.cursorPosition(), is(pos.plus(5, 0)));

    // Cursor Backward Wrapped
    term.write("\u001b[<L");
    assertThat(term.cursorPosition(), is(pos.plus(4, 0)));

    term.write("\u001b[<2L");
    assertThat(term.cursorPosition(), is(pos.plus(2, 0)));

    // Cursor Forward Wrapped
    term.write("\u001b[<R");
    assertThat(term.cursorPosition(), is(pos.plus(3, 0)));

    // Backspace
    term.write("\u001b[<B");
    assertThat(term.cursorPosition(), is(pos.plus(2, 0)));
    assertThat(term.currentLine(), is("HELO"));

    // Delete Characters Wrapped
    term.write("\u001b[<H");
    term.write("\u001b[<D");
    assertThat(term.cursorPosition(), is(pos));
    assertThat(term.currentLine(), is("ELO"));

    term.write("\u001b[<2D");
    assertThat(term.cursorPosition(), is(pos));
    assertThat(term.currentLine(), is("O"));
  }

  @Test
  public void testCsiSequencesWrapped() throws InterruptedException {
    // CSI sequences that implement navigation with keyboard, on a wrapped line
    XTermElement term = $(XTermElement.class).first();
    term.executeScript("this.terminal.resize(30,20)");
    term.sendKeys(Keys.ENTER);
    Position home = term.cursorPosition();

    String text = makeFullLine(term, true) + makeFullLine(term, false) + makeFullLine(term, false);
    term.sendKeys(text);

    assertThat(term.currentLine(), is(text));
    Position end = term.cursorPosition();

    term.write("\u001b[<H");
    assertThat(term.cursorPosition(), is(home));

    term.write("\u001b[<" + text.length() + "D");
    assertThat(term.cursorPosition(), is(home));
    assertThat(term.currentLine(), isEmptyString());

    term.sendKeys(text);
    term.write("\u001b[<H");
    term.write("\u001b[<" + text.length() + "R");
    assertThat(term.cursorPosition(), is(end));

    term.write("\u001b[<" + (text.length() - 1) + "L");
    assertThat(term.cursorPosition(), is(home));

    int cols = term.getColumnWidth();
    for (int i = 0; i < text.length() - 1; i++) {
      term.write("\u001b[<R");
      assertThat(term.cursorPosition(), is(home.plus(i + 1, 0).adjust(cols)));
    }
    term.write("\u001b[<R");
    assertThat(term.cursorPosition(), is(end));

    term.write("\u001b[<L");
    for (int i = text.length() - 1; i > 0; i--) {
      assertThat(term.cursorPosition(), is(home.plus(i - 1, 0).adjust(cols)));
      term.write("\u001b[<L");
    }

  }

}