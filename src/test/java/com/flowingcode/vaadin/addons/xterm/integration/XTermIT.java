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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import com.vaadin.testbench.TestBenchElement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.openqa.selenium.By;

public class XTermIT extends AbstractViewTest {

  private Matcher<TestBenchElement> hasBeenUpgradedToCustomElement =
      new TypeSafeDiagnosingMatcher<TestBenchElement>() {

        @Override
        public void describeTo(Description description) {
          description.appendText("a custom element");
        }

        @Override
        protected boolean matchesSafely(TestBenchElement item, Description mismatchDescription) {
          String script = "let s=arguments[0].shadowRoot; return !!(s&&s.childElementCount)";
          if (!item.getTagName().contains("-")) {
            return true;
          }
          if ((Boolean) item.getCommandExecutor().executeScript(script, item)) {
            return true;
          } else {
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
    XTermElement term = $(XTermElement.class).first();

    Position pos = term.cursorPosition();

    term.sendKeys("HELLO");
    assertThat(term.currentLine(), is("HELLO"));
    assertThat(term.cursorPosition(), is(pos.plus(5, 0)));

    term.sendKeys("HELLO");
    assertThat(term.currentLine(), is("HELLOHELLO"));
    assertThat(term.cursorPosition(), is(pos.plus(10, 0)));

    term.sendKeys("\n");
    assertThat(term.currentLine(), is(""));
    assertThat(term.cursorPosition(), is(pos.advance(0, 1)));

    term.sendKeys("HELLO\nWORLD");
    assertThat(term.currentLine(), is("WORLD"));
    assertThat(term.cursorPosition(), is(pos.advance(0, 1).plus(5, 0)));
  }

  private Integer getKeyCount() {
    return ((Long)getCommandExecutor().executeScript("return keyCount")).intValue();
  }

  @Test
  public void customKeyHandlerLowLevel() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();
    term.executeScript("window.keyCount=0");

    // register an event handler
    term.executeScript(
        "r1=this.customKeyEventHandlers.register(ev=> ev.key=='E', ev=>++keyCount)");
    assertThat(getKeyCount(), is(0));

    // fire it
    term.sendKeys("E");
    assertThat(term.currentLine(), is("E"));
    assertThat(getKeyCount(), is(1));

    // register another event handler for the same key
    term.executeScript(
        "r2=this.customKeyEventHandlers.register(ev=> ev.key=='E', ev=>++keyCount)");

    // fire it: increment is performed twice (by each handler)
    term.sendKeys("E");
    assertThat(term.currentLine(), is("EE"));
    assertThat(getKeyCount(), is(3));

    // deregister
    term.executeScript("r1.dispose()");
    term.sendKeys("E");
    assertThat(getKeyCount(), is(4));

    term.executeScript("r2.dispose()");
    term.sendKeys("E");
    assertThat(getKeyCount(), is(4));
  }

  @Test
  public void customKeyHandlerRegistrationOrder() throws InterruptedException {
    // assert that custom key handlers are processed in registration order
    XTermElement term = $(XTermElement.class).first();
    term.executeScript("window.keyCount=0");

    term.executeScript(
        "r1=this.customKeyEventHandlers.register(ev=> ev.key=='E', ev=>keyCount=keyCount*10+1)");
    term.executeScript(
        "r2=this.customKeyEventHandlers.register(ev=> ev.key=='E', ev=>keyCount=keyCount*10+2)");
    term.sendKeys("E");
    assertThat(getKeyCount(), is(12));
    term.executeScript("r1.dispose()");
    term.executeScript("r2.dispose()");
  }

  @Test
  public void customKeyHandlerStopImmediatePropagation() throws InterruptedException {
    // since custom key handlers are processed in registration order
    // then r2 will prevent the processing of r3
    XTermElement term = $(XTermElement.class).first();
    term.executeScript("window.keyCount=0");

    term.executeScript(
        "r1=this.customKeyEventHandlers.register(ev=> ev.key=='E', ev=>keyCount=keyCount*10+1)");
    term.executeScript(
        "r2=this.customKeyEventHandlers.register(ev=> ev.key=='E', ev=>ev.stopImmediatePropagation())");
    term.executeScript(
        "r3=this.customKeyEventHandlers.register(ev=> ev.key=='E', ev=>keyCount=keyCount*10+3)");
    term.sendKeys("E");
    assertThat(getKeyCount(), is(1));
    term.executeScript("r1.dispose()");
    term.executeScript("r2.dispose()");
    term.executeScript("r3.dispose()");
  }

  @Test
  public void customKeyHandlerHighLevel() throws InterruptedException {
    XTermElement term = $(XTermElement.class).first();
    term.executeScript("window.keyCount=0");

    // register interest on CustomKey event
    Long id1 = (Long) term.executeScript("return this.registerCustomKeyListener({key:'E'})");
    term.executeScript("this.addEventListener('CustomKey', ()=>++keyCount)");
    assertThat(id1, is(notNullValue()));
    term.sendKeys("E");
    assertThat(getKeyCount(), is(1));

    // register interest again on CustomKey event for the same key
    // increment is performed by the CustomKey listener, which is called once
    Long id2 = (Long) term.executeScript("return this.registerCustomKeyListener({key:'E'})");
    assertThat(id2, is(notNullValue()));
    assertThat(id2, is(not(id1)));
    term.sendKeys("E");
    assertThat(getKeyCount(), is(2));

    // deregister
    term.executeScript("this.customKeyEventHandlers.remove(arguments[0])", id1);
    term.sendKeys("E");
    assertThat(getKeyCount(), is(3));

    term.executeScript("this.customKeyEventHandlers.remove(arguments[0])", id2);
    term.sendKeys("E");
    assertThat(getKeyCount(), is(3));
  }

}
