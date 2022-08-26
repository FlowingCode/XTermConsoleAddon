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
import org.junit.Test;
import org.openqa.selenium.Keys;

public class TerminalHistoryIT extends AbstractViewTest {

  @Test
  public void testArrowKeys() {
    XTermElement term = $(XTermElement.class).first();

    Position pos = term.cursorPosition();
    term.sendKeys("foo1\nfoo2\n");

    assertThat(term.cursorPosition(), is(pos.advance(0, 2)));
    assertThat(term.currentLine(), isEmptyString());

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
    assertThat(term.currentLine(), isEmptyString());

    term.sendKeys("bar");
    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo2"));

    term.sendKeys(Keys.ARROW_DOWN);
    assertThat(term.currentLine(), is("bar"));
  }

  @Test
  public void testArrowUpAfterRunningLastCommandFromHistory() {
    XTermElement term = $(XTermElement.class).first();

    term.sendKeys("foo1\n");
    term.sendKeys("foo2\n");

    assertThat(term.currentLine(), isEmptyString());

    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo2"));
    term.sendKeys("\n");

    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo2"));

    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo1"));
  }

  @Test
  public void testArrowUpAfterRunningEmptyCommand() {
    XTermElement term = $(XTermElement.class).first();

    term.sendKeys("foo1\n");
    term.sendKeys("foo2\n");

    assertThat(term.currentLine(), isEmptyString());

    term.sendKeys(Keys.ARROW_UP);
    assertThat(term.currentLine(), is("foo2"));
    term.sendKeys("\u0008\u0008\u0008\u0008"); // 4 backspaces
    assertThat(term.currentLine(), isEmptyString());
    term.sendKeys("\n");

    term.sendKeys(Keys.ARROW_UP);
    // The position in the history should be back at the end
    assertThat(term.currentLine(), is("foo2"));
  }

}
