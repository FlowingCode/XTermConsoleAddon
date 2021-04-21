/*-
 * #%L
 * XTerm Addon
 * %%
 * Copyright (C) 2020 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License";
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

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.FieldDefaults;

/** The color theme of the terminal. */
@FieldDefaults(level = AccessLevel.PRIVATE)
@With
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public final class TerminalTheme {

  /** Set the default foreground color */
  String foreground;

  /** Set the default background color */
  String background;

  /** Set the cursor color */
  String cursor;

  /** Set the accent color of the cursor (fg color for a block cursor) */
  String cursorAccent;

  /** Set the selection background color (can be transparent) */
  String selection;

  /** ANSI black (eg. `\x1b[30m`) */
  String black;

  /** ANSI red (eg. `\x1b[31m`) */
  String red;

  /** ANSI green (eg. `\x1b[32m`) */
  String green;

  /** ANSI yellow (eg. `\x1b[33m`) */
  String yellow;

  /** ANSI blue (eg. `\x1b[34m`) */
  String blue;

  /** ANSI magenta (eg. `\x1b[35m`) */
  String magenta;

  /** ANSI cyan (eg. `\x1b[36m`) */
  String cyan;

  /** ANSI white (eg. `\x1b[37m`) */
  String white;

  /** ANSI bright black (eg. `\x1b[1;30m`) */
  String brightBlack;

  /** ANSI bright red (eg. `\x1b[1;31m`) */
  String brightRed;

  /** ANSI bright green (eg. `\x1b[1;32m`) */
  String brightGreen;

  /** ANSI bright yellow (eg. `\x1b[1;33m`) */
  String brightYellow;

  /** ANSI bright blue (eg. `\x1b[1;34m`) */
  String brightBlue;

  /** ANSI bright magenta (eg. `\x1b[1;35m`) */
  String brightMagenta;

  /** ANSI bright cyan (eg. `\x1b[1;36m`) */
  String brightCyan;

  /** ANSI bright white (eg. `\x1b[1;37m`) */
  String brightWhite;

  JsonObject asJsonObject() {
    JsonObject obj = Json.createObject();
    for (Field field : this.getClass().getDeclaredFields()) {
      try {
        obj.put(
            field.getName(),
            Optional.ofNullable((String) field.get(this))
                .<JsonValue>map(Json::create)
                .orElseGet(Json::createNull));
      } catch (Exception e) {
        throw new UndeclaredThrowableException(e);
      }
    }
    return obj;
  }
}
