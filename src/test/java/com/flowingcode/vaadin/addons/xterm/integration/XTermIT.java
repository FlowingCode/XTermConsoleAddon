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
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import com.vaadin.testbench.TestBenchElement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class XTermIT extends AbstractXTermTest {

  private Matcher<TestBenchElement> hasBeenUpgradedToCustomElement =
      new TypeSafeDiagnosingMatcher<TestBenchElement>() {

        @Override
        public void describeTo(Description description) {
          description.appendText("a custom element");
        }

        @Override
        protected boolean matchesSafely(TestBenchElement item, Description mismatchDescription) {
          String script = "let s=arguments[0].shadowRoot; return !!(s&&s.childElementCount)";
          if (!item.getTagName().contains("-")) return true;
          if ((Boolean) item.getCommandExecutor().executeScript(script, item)) return true;
          else {
            mismatchDescription.appendText(item.getTagName() + " ");
            mismatchDescription.appendDescriptionOf(is(not(this)));
            return false;
          }
        }
      };


  @Test
  public void componentWorks() {
    TestBenchElement term = $("fc-xterm").first();
    assertThat(term, hasBeenUpgradedToCustomElement);
    term.findElement(By.xpath("./*[@slot='terminal-container']"));
  }

  @Test
  public void writeText() throws InterruptedException {
    TestBenchElement term = $("fc-xterm").first();
    WebElement input =
        (WebElement) getCommandExecutor().executeScript("return document.activeElement");

    int y = cursorPosition(term).y;

    input.sendKeys("HELLO");
    assertThat(currentLine(term), is("HELLO"));
    assertThat(cursorPosition(term), is(at(5, y)));

    input.sendKeys("HELLO");
    assertThat(currentLine(term), is("HELLOHELLO"));
    assertThat(cursorPosition(term), is(at(10, y)));

    input.sendKeys("\n");
    assertThat(currentLine(term), is(""));
    assertThat(cursorPosition(term), is(at(0, ++y)));

    input.sendKeys("HELLO\nWORLD");
    assertThat(currentLine(term), is("WORLD"));
    assertThat(cursorPosition(term), is(at(5, ++y)));
  }
}
