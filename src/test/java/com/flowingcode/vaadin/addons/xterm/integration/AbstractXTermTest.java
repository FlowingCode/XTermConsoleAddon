/*-
 * #%L
 * XTerm Addon
 * %%
 * Copyright (C) 2020 Flowing Code
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.flowingcode.vaadin.addons.xterm.XTermFeature;
import com.vaadin.flow.component.Tag;
import com.vaadin.testbench.TestBenchElement;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class AbstractXTermTest extends AbstractViewTest {

  final int getColumnWidth(TestBenchElement term) {
    return ((Long) getCommandExecutor().executeScript("return arguments[0].terminal.cols", term))
        .intValue();
  }

  final void assertFeatureHasBeenLoaded(
      TestBenchElement term, Class<? extends XTermFeature> featureClass, String featureName) {
    Object property =
        getCommandExecutor()
            .executeScript("return arguments[0].features[arguments[1]]", term, featureName);
    assertNotNull("No property for feature " + featureName, property);

    if (featureClass != null) {
      String elementName = featureClass.getAnnotation(Tag.class).value();
      WebElement element =
          term.findElement(By.xpath(String.format("./%s[@slot='feature']", elementName)));
      getCommandExecutor().executeScript("arguments[0].test=42", element);
      Object test =
          getCommandExecutor()
              .executeScript("return arguments[0].features[arguments[1]].test", term, featureName);
      assertEquals("Property and element differ", 42L, test);
    }
  }
  ;

  final String currentLine(TestBenchElement terminal) {
    return lineAtOffset(terminal, 0);
  }

  final String lineAtOffset(TestBenchElement terminal, int offset) {
    String command =
        "buffer=arguments[0].terminal._core._inputHandler._bufferService.buffer; return buffer.lines.get(buffer.ybase+buffer.y+(%s)).translateToString()";
    command = String.format(command, offset);
    return ((String) getCommandExecutor().executeScript(command, terminal))
        .replaceFirst("\\s+$", "");
  }

  static Position at(int x, int y) {
    return new Position(x, y);
  }

  static int[] intArray(Object obj) {
    return ((List<?>) obj).stream().mapToInt(i -> ((Long) i).intValue()).toArray();
  }

  final Position cursorPosition(TestBenchElement terminal) {
    int[] pos =
        intArray(
            getCommandExecutor()
                .executeScript(
                    "buffer=arguments[0].terminal.buffer; return [buffer.cursorX, buffer.cursorY]",
                    terminal));
    return new Position(pos[0], pos[1]);
  }

  final void write(TestBenchElement terminal, String text) {
    getCommandExecutor()
        .executeScript(String.format("arguments[0].terminal.write('%s')", text), terminal);
  }
}
