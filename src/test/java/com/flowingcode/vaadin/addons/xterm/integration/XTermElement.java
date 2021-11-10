package com.flowingcode.vaadin.addons.xterm.integration;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.commands.TestBenchCommandExecutor;
import com.vaadin.testbench.elementsbase.Element;
import java.util.Arrays;
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
        driver -> executeScript("return this.terminal.textarea"));
  }
  public void write(String text) {
    executeScript(String.format("this.terminal.write('%s')", text));
  }

  public int getColumnWidth() {
    return ((Long) executeScript("return this.terminal.cols")).intValue();
  }

  final String currentLine() {
    return getPropertyString("currentLine");
  }

  public String getSelection() {
    return (String) executeScript("return this.terminal.getSelection()");
  }

  public String lineAtOffset(int offset) {
    return ((String) executeScript(
        "buffer=this.terminal._core._inputHandler._bufferService.buffer;"
            + "line=buffer.lines.get(buffer.ybase+buffer.y+(arguments[0]));"
            + "return line.translateToString().substr(0,line.getTrimmedLength());",
        offset));
  }

  public Position cursorPosition() {
    int[] pos = intArray(executeScript(
        "buffer=this.terminal.buffer.active; return [buffer.cursorX, buffer.cursorY]",
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

  @Override
  public Object executeScript(String script, Object... arguments) {
    script = String.format(
        "return function(arguments){arguments.pop(); %s}.bind(arguments[arguments.length-1])([].slice.call(arguments))",
        script);
    arguments = Arrays.copyOf(arguments, arguments.length + 1);
    arguments[arguments.length - 1] = this;
    return getCommandExecutor().executeScript(script, arguments);
  }

}
