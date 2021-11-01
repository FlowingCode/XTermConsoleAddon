package com.flowingcode.vaadin.addons.xterm.integration;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import com.vaadin.testbench.elementsbase.Element;
import java.util.List;
import org.openqa.selenium.WebElement;

/**
 * A TestBench element representing a <code>&lt;fc-xterm&gt;</code> element.
 */
@Element("fc-xterm")
public class XTermElement extends TestBenchElement {

  private WebElement input;

  @Override
  protected void init(WebElement element, TestBenchCommandExecutor commandExecutor) {
    super.init(element, commandExecutor);
    input = (WebElement) waitUntil(
        driver -> executeScript("return arguments[0].terminal.textarea", this));
  }
  public void write(String text) {
    executeScript(String.format("arguments[0].terminal.write('%s')", text), this);
  }

  public int getColumnWidth() {
    return ((Long) executeScript("return arguments[0].terminal.cols", this)).intValue();
  }

  final String currentLine() {
    return getPropertyString("currentLine");
  }

  public String lineAtOffset(int offset) {
    return ((String) executeScript(
        "buffer=arguments[0].terminal._core._inputHandler._bufferService.buffer;"
            + "line=buffer.lines.get(buffer.ybase+buffer.y+(arguments[1]));"
            + "return line.translateToString().substr(0,line.getTrimmedLength());",
        this, offset));
  }

  public Position cursorPosition() {
    int[] pos = intArray(executeScript(
        "buffer=arguments[0].terminal.buffer.active; return [buffer.cursorX, buffer.cursorY]",
        this));
    return new Position(pos[0], pos[1]);
  }

  private static int[] intArray(Object obj) {
    return ((List<?>) obj).stream().mapToInt(i -> ((Long) i).intValue()).toArray();
  }

  public void setUseSystemClipboard(boolean value) {
    setProperty("useSystemClipboard", value);
  }

  public void setPrompt(String value) {
    setProperty("prompt", value);
  }

  @Override
  public void sendKeys(CharSequence... keysToSend) {
    input.sendKeys(keysToSend);
  }
}
