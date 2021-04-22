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
package com.flowingcode.vaadin.addons.xterm;

import com.flowingcode.vaadin.addons.DemoLayout;
import com.flowingcode.vaadin.addons.GithubLink;
import com.flowingcode.vaadin.addons.xterm.ITerminalClipboard.UseSystemClipboard;
import com.flowingcode.vaadin.addons.xterm.ITerminalOptions.BellStyle;
import com.flowingcode.vaadin.addons.xterm.ITerminalOptions.CursorStyle;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("serial")
@Route(value = "xterm", layout = DemoLayout.class)
@GithubLink("https://github.com/FlowingCode/XTermConsoleAddon")
public class XtermDemoView extends VerticalLayout {

  private XTerm xterm;

  public XtermDemoView() {
    setSizeFull();
    setPadding(false);
    getElement().getStyle().set("background", "black");

    xterm = new XTerm();
    xterm.writeln("xterm add-on by Flowing Code S.A.\n\n");
    xterm.writeln("Commands: time, date, beep, color on, color off\n");
    xterm.setCursorBlink(true);
    xterm.setCursorStyle(CursorStyle.UNDERLINE);
    xterm.setBellStyle(BellStyle.SOUND);

    xterm.setSizeFull();

    xterm.setCopySelection(true);
    xterm.setUseSystemClipboard(UseSystemClipboard.READWRITE);
    xterm.setPasteWithMiddleClick(true);
    xterm.setPasteWithRightClick(true);

    xterm.addLineListener(
        ev -> {
          switch (ev.getLine().toLowerCase()) {
            case "time":
              xterm.writeln(
                  LocalTime.now()
                  .truncatedTo(ChronoUnit.SECONDS)
                  .format(DateTimeFormatter.ISO_TIME));
              break;
            case "date":
              xterm.writeln(LocalDate.now().toString());
              break;
            case "beep":
              xterm.write("\u0007");
              break;
            case "color on":
              xterm.setTheme(
                  new TerminalTheme()
                  .withBackground("rgb(103,195,228)")
                  .withForeground("rgb(0,0,0)"));
              break;
            case "color off":
              xterm.setTheme(new TerminalTheme());
              break;
            default:
              xterm.writeln("Bad command");
              Notification.show(ev.getLine());
          }
        });

    xterm.focus();
    xterm.fit();
    add(xterm);
  }
}
