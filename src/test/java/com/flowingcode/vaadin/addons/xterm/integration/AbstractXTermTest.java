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

import com.vaadin.testbench.TestBenchElement;
import java.util.List;

public abstract class AbstractXTermTest extends AbstractViewTest {

  final int getColumnWidth(TestBenchElement term) {
    return ((Long) getCommandExecutor().executeScript("return arguments[0].terminal.cols", term))
        .intValue();
  }

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
