/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2026 Flowing Code
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

public class SelectionFeatureIT extends AbstractViewTest {

  @Test
  public void testSelectionFeature1() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();

    term.write("abcd");
    Position pos = term.cursorPosition();

    // select left, backspace
    term.sendKeys(Keys.SHIFT, Keys.ARROW_LEFT);
    assertThat(term.getSelection(), is("d"));
    assertThat(term.cursorPosition(), is(pos));
    term.sendKeys(Keys.BACK_SPACE);
    assertThat(term.currentLine(), is("abc"));
    assertThat(term.cursorPosition(), is(pos.advance(-1, 0)));

    // select left, delete
    term.sendKeys(Keys.SHIFT, Keys.ARROW_LEFT);
    assertThat(term.getSelection(), is("c"));
    assertThat(term.cursorPosition(), is(pos));
    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), is("ab"));
    assertThat(term.cursorPosition(), is(pos.advance(-1, 0)));

    // select right, delete
    term.sendKeys(Keys.HOME);
    pos = term.cursorPosition();
    term.sendKeys(Keys.SHIFT, Keys.ARROW_RIGHT);
    assertThat(term.getSelection(), is("a"));
    assertThat(term.cursorPosition(), is(pos));
    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), is("b"));
    assertThat(term.cursorPosition(), is(pos));

    // select right, backspace
    term.sendKeys(Keys.SHIFT, Keys.ARROW_RIGHT);
    assertThat(term.getSelection(), is("b"));
    assertThat(term.cursorPosition(), is(pos));
    term.sendKeys(Keys.BACK_SPACE);
    assertThat(term.currentLine(), isEmptyString());
    assertThat(term.cursorPosition(), is(pos));
  }

  @Test
  public void testSelectionFeature2() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();

    term.write("abcd");
    Position pos = term.cursorPosition();

    // select to home, delete
    term.sendKeys(Keys.SHIFT, Keys.HOME);
    assertThat(term.getSelection(), is("abcd"));
    assertThat(term.cursorPosition(), is(pos));
    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), isEmptyString());
    assertThat(term.cursorPosition(), is(pos.advance(-4, 0)));
  }

  @Test
  public void testSelectionFeature3() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();

    term.write("abcd");
    Position pos = term.cursorPosition();

    // select to end, delete
    term.sendKeys(Keys.HOME);
    pos = term.cursorPosition();
    term.sendKeys(Keys.SHIFT, Keys.END);
    assertThat(term.getSelection(), is("abcd"));
    assertThat(term.cursorPosition(), is(pos));
    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), isEmptyString());
    assertThat(term.cursorPosition(), is(pos));
  }

  @Test
  public void testSelectionFeature4() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();
    String text = makeFullLine(term, true) + makeFullLine(term, false) + makeFullLine(term, false);

    // select to home, delete (wrapping)
    term.write(text);
    assertThat(term.currentLine(), is(text));
    term.sendKeys(Keys.SHIFT, Keys.HOME);
    assertThat(term.getSelection(), is(text));
    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), isEmptyString());
  }

  @Test
  public void testSelectionFeature5() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();
    String text = makeFullLine(term, true) + makeFullLine(term, false) + makeFullLine(term, false);
    // select to end, delete (wrapping)
    term.write(text);
    assertThat(term.currentLine(), is(text));
    term.sendKeys(Keys.HOME);
    term.sendKeys(Keys.SHIFT, Keys.END);
    assertThat(term.getSelection(), is(text));
    term.sendKeys(Keys.DELETE);
    assertThat(term.currentLine(), isEmptyString());
  }

}