# XTerm Console Addon — Claude Guide

## Project Overview

A Vaadin Flow addon that integrates [xterm.js](https://xtermjs.org/) into a Java server-side component. It provides a full terminal emulator with console I/O, clipboard, fit-to-container, keyboard selection, command history, and state preservation.

- **Group ID:** `com.flowingcode.addons`
- **Artifact ID:** `xterm-console`
- **Current Version:** `3.3.1-SNAPSHOT` (branch `master` → Vaadin 24)
- **License:** Apache 2.0
- **Custom element tag:** `<fc-xterm>`

## Build & Run

```bash
# Build and launch demo app
mvn clean install jetty:run
# Visit http://localhost:8080/

# Build only (default: Vaadin 24, Java 17)
mvn clean package

# Integration tests (requires Chrome)
mvn clean verify -Pintegration-tests

# Package for Vaadin Directory (sources + javadoc + assembly zip)
mvn clean install -Pdirectory

# Production build (minified frontend)
mvn clean package -Pproduction
```

## Maven Profiles

| Profile | Java | Vaadin | Git Branch |
|---|---|---|---|
| *(default)* | 17 | 24.3.5 | `master` |
| `v25` | 21 | 25.0.3 | `master` |
| `v23` | 11 | 23.x | `2.x` |
| *(default on 1.x)* | 8 | 14.x | `1.x` |

## Project Structure

```
src/
├── main/
│   ├── java/com/flowingcode/vaadin/addons/xterm/
│   │   ├── XTerm.java                    # Public component: extends XTermBase, implements Fit+Console+Clipboard
│   │   ├── XTermBase.java                # Abstract base: proxy bridge, deferred commands, addon registry
│   │   ├── ITerminal.java                # xterm.js terminal operations (write, scroll, select, resize…)
│   │   ├── ITerminalOptions.java         # xterm.js options setters (cursor, font, theme, bell…)
│   │   ├── ITerminalConsole.java         # Console feature: prompt, insertMode, getCurrentLine, LineEvent
│   │   ├── ITerminalClipboard.java       # Clipboard feature: useSystemClipboard, copy/paste settings
│   │   ├── ITerminalFit.java             # Fit feature: fit(), fitOnResize
│   │   ├── ITerminalSelection.java       # Keyboard selection: keyboardSelectionEnabled
│   │   ├── TerminalAddon.java            # Abstract server-side addon base
│   │   ├── ClientTerminalAddon.java      # Server addon with JS counterpart (executeJs in addon context)
│   │   ├── PreserveStateAddon.java       # Preserves scrollback+options across detach/reattach
│   │   ├── TerminalHistory.java          # Arrow-up/down command history (not an addon subclass)
│   │   ├── TerminalTheme.java            # Color theme (all 16 ANSI colors + fg/bg/cursor)
│   │   └── utils/StateMemoizer.java      # Proxy that remembers setter calls and can replay them
│   └── resources/META-INF/frontend/fc-xterm/
│       ├── xterm-element.ts              # XTermElement (LitElement base), TerminalAddon base, CustomKeyEventHandlerRegistry
│       ├── xterm.ts                      # Final composed XTermComponent registered as <fc-xterm>
│       ├── xterm-console-mixin.ts        # ConsoleAddon: CSI handlers, cursor navigation, prompt, line events
│       ├── xterm-clipboard-mixin.ts      # ClipboardAddon: Ctrl-C/V, right/middle click, system clipboard
│       ├── xterm-fit-mixin.ts            # FitAddon: wraps xterm-addon-fit, fitOnResize, viewport width fix
│       ├── xterm-selection-mixin.ts      # SelectionAddon: Shift+Arrow keyboard selection
│       └── xterm-insertfix-mixin.ts      # InsertFixAddon: fixes insert mode + wraparound overflow bug
└── test/java/
    ├── com/flowingcode/vaadin/addons/
    │   └── DemoLayout.java               # Full-size Div RouterLayout for demo routes
    └── com/flowingcode/vaadin/addons/xterm/
        ├── DemoView.java                 # Route "": redirects to XtermDemoView
        ├── XtermDemoView.java            # Route "xterm" (layout DemoLayout): interactive demo with commands
        ├── PreserveStateAddonTest.java   # Unit tests: scrollback buffer, prompt+line tracking, clear/reset
        ├── test/SerializationTest.java   # Serialization tests for XTerm and TerminalHistory
        ├── utils/StateMemoizerTest.java  # Unit tests for StateMemoizer
        └── integration/
            ├── AbstractViewTest.java     # Base: Chrome driver, localhost:8080, screenshot on failure
            ├── IntegrationView.java      # Route "xterm/it": test server-side view
            ├── IntegrationViewCallables.java  # @ClientCallable interface for test→server RPC
            ├── XTermElement.java         # TestBench page object for <fc-xterm>
            ├── Position.java             # (x,y) cursor position helper with advance/plus/adjust
            ├── XTermTestUtils.java       # makeFullLine() helper
            ├── SampleClientTerminalAddon.java   # Minimal ClientTerminalAddon implementation for tests
            ├── XTermIT.java              # Component rendering, text input, CustomKeyEventHandlerRegistry
            ├── ConsoleFeatureIT.java     # Navigation, CSI sequences, wrapped lines, insert mode
            ├── ClipboardFeatureIT.java   # Mouse drag select + right-click paste
            ├── FitFeatureIT.java         # Window resize causes column-count change
            ├── SelectionFeatureIT.java   # Shift+Arrow selection + delete/backspace
            ├── TerminalHistoryIT.java    # Arrow-up/down history navigation edge cases
            └── SampleClientTerminalAddonIT.java  # ClientTerminalAddon round-trip via testbench-rpc
```

## Class Hierarchy and Responsibilities

### `XTermBase` (abstract)
- Annotated: `@NpmPackage("xterm","5.1.0")`, `@JsModule("./fc-xterm/xterm-element.ts")`, `@CssImport("xterm/css/xterm.css")`
- Uses `@ExtensionMethod(JsonMigration.class)` for elemental JSON compatibility
- Two Lombok `@Delegate` fields pointing to the **same** proxy object implementing both `ITerminal` and `ITerminalOptions`
- The proxy class is pre-created in a `static` block via `Proxy.getProxyClass`
- Deferred commands: before `terminal-initialized` event, all `executeJs` calls are queued in `deferredCommands` (a `LinkedList<Command>`). After initialization the queue is drained once and set to `null`.
- Queries with a return value (`CompletableFuture`) throw `IllegalStateException` if called before initialization.
- `addCustomKeyListener`: builds a JSON array of key descriptors, constructs a JS filter string, fires a `CustomKey` DOM event, and calls `this.registerCustomKeyListener($0)` on the client.
- `registerServerSideAddon` is package-private; addons call it from their own constructors.

### `ProxyInvocationHandler` (inner class of `XTermBase`)
Translates Java interface method calls to JavaScript:
- `setXxx(value)` → `this.terminal.options['xxx']=value` (camelCase property name)
  - Exception: `setBellStyle`/`setBellSound` → `this['bellStyle']=value` (element property, not terminal option)
- Enum arguments are converted via `name().toLowerCase(Locale.ENGLISH)`
- `TerminalTheme` argument converted via `asJsonObject()`
- Zero-arg methods → `return this.terminal['name']()`
- N-arg methods → `return this.terminal['name']($1,$2,...)`
- Return type mapping: `void` → no result; `CompletableFuture<String>` → `.asString()`; `CompletableFuture<Boolean>` → `.asBoolean()`; `CompletableFuture<Integer>` → `(int)asNumber()`

### `XTerm`
Thin public class. Extends `XTermBase`, implements `ITerminalFit`, `ITerminalConsole`, `ITerminalClipboard`. Constructor calls `setInsertMode(true)`.

### `ITerminalConsole` (interface with default methods)
- `LineEvent` — `@DomEvent("line")`, carries `event.detail` as the line string. Fire it in unit tests via `ComponentUtil.fireEvent(component, new ITerminalConsole.LineEvent(xterm, true, "text"))`.
- `addLineListener` — subscribes via `ComponentUtil.addListener`
- `setInsertMode` — calls `((XTermBase)this).executeJs("this.insertMode=$0", insertMode)`
- `getCurrentLine` — `getElement().executeJs("return this.currentLine")`, returns `CompletableFuture<String>`
- `setPrompt`/`getPrompt` — element property `"prompt"`. Setting `null` disables the prompt.
- `writePrompt` — `((XTermBase)this).executeJs("this.writePrompt()")`

### `ITerminalClipboard` (interface with default methods)
All methods use `getElement().setProperty()`/`getElement().getProperty()`. Properties: `useSystemClipboard`, `pasteWithRightClick`, `pasteWithMiddleClick`, `copySelection`. `UseSystemClipboard` enum: `FALSE`, `WRITE`, `READWRITE`.

### `ITerminalFit` (interface with default methods)
`@NpmPackage("xterm-addon-fit","0.7.0")`. `fit()` calls `getElement().executeJs("this.fit()")`. `fitOnResize` is an element property.

### `ITerminalSelection` (interface with default methods)
`keyboardSelectionEnabled` is an element property. Defaults to `false` on the Java side (property getter default), but the client-side mixin initializes it to `true`.

### `ITerminalOptions` enums
All uppercase in Java, lowercased by proxy for xterm.js:
- `BellStyle`: `NONE`, `SOUND`
- `CursorStyle`: `BLOCK`, `UNDERLINE`, `BAR`
- `FastScrollModifier`: `ALT`, `CTRL`, `SHIFT`, `UNDEFINED`
- `RendererType`: `DOM`, `CANVAS`

### `TerminalAddon`
Abstract class. Constructor takes `XTermBase`, calls `xterm.registerServerSideAddon(this)`. Subclasses add behavior.

### `ClientTerminalAddon`
Extends `TerminalAddon`. Requires `getName()` (non-null string, used as key in `this.addons[name]` on client). `executeJs(expr, params)`:
1. Wraps params in a JSON array using `encodeWithTypeInfo` (version-sensitive: uses `com.vaadin.flow.internal.JsonCodec` on Vaadin ≤24, `com.vaadin.flow.internal.JacksonCodec` on Vaadin ≥25 — detected via `Version.getMajorVersion()`).
2. Rewrites `$0`, `$1`... placeholders to `$1[0]`, `$1[1]`... (all params passed as a single array `$1`).
3. Executes: `(function(){expr}).apply(this.addons[$0],$1)` — `this` inside the expression is the client-side addon object.

The client-side addon object must be registered in `this.addons[name]` before `executeJs` is called. See `SampleClientTerminalAddon` for the registration pattern using `window.Vaadin.Flow.fcXtermConnector`.

### `PreserveStateAddon`
Extends `TerminalAddon`, implements both `ITerminal` and `ITerminalOptions`. Strategy:
- Options calls go through a `StateMemoizer` proxy so they can be replayed on reattach.
- `write(data)` and `writeln(data)` forward to xterm and append to `scrollbackBuffer` (a `StringBuilder`). Note: `writeln` appends data + `\n`, not data + CRLF.
- `clear()` and `reset()` forward to xterm and clear the buffer.
- `LineEvent` listener appends prompt + line + `\n` to buffer (the prompt at time of submit, not time of `writePrompt`).
- On `AttachEvent` (if previously detached): calls `optionsMemoizer.apply()`, then `xterm.write(scrollbackBuffer)`, then `xterm.writePrompt()`.
- Usage: call all `ITerminal`/`ITerminalOptions` methods **via this addon**, not directly on `XTerm`.

### `TerminalHistory`
Not a `TerminalAddon` subclass. Attached to a terminal via `ComponentUtil.setData`. Static API:
- `TerminalHistory.extend(xterm)` — creates, enables, and registers history on the terminal
- `TerminalHistory.of(xterm)` — retrieves the registered instance (returns `null` if none)
- A terminal can have only one `TerminalHistory`; the constructor throws if one already exists.
- Uses a `LinkedList<String>` with a bidirectional `ListIterator`.
- Arrow-up shows previous entry; Arrow-down shows next (restores the original line when reaching the end).
- When Arrow-up is first pressed, captures the current line as `initialLine` via `getCurrentLine()` async call before showing history. This is restored on Arrow-down past the last entry.
- Empty/whitespace-only lines are not added. Duplicate consecutive entries are allowed.
- History write sequence: `\033[<2K\033[<H\033[G` + prompt + line (custom CSI codes clear the current line, go home, go to column 1).
- Custom serialization (`writeObject`/`readObject`): saves and restores the `ListIterator` cursor position.
- `setMaxSize(Integer)` — caps the number of entries; `null` = unbounded. When `setMaxSize` trims an over-large existing list it removes from the front (`remove(0)`, oldest first). However, `add()` drops the newly-added entry via `removeLast()` when the list is already at capacity — meaning new commands are silently discarded when the cap is full.

### `TerminalTheme`
Lombok `@With`, `@NoArgsConstructor`, `@AllArgsConstructor(PRIVATE)`, `@FieldDefaults(PRIVATE)`. Fields: `foreground`, `background`, `cursor`, `cursorAccent`, `selection`, plus 16 ANSI color fields (`black`…`brightWhite`). Build with `new TerminalTheme().withBackground(…).withForeground(…)`. `asJsonObject()` uses `Field` reflection — field declaration order matches xterm.js theme object keys.

### `StateMemoizer`
Generic proxy wrapper for any set of interfaces. On every `set*(value)` call: stores `(methodName, lastArg)` in a `HashMap`. `apply()` re-invokes each stored setter on the delegate. Only the **last** call per setter name is remembered (calling `setFoo("a")` then `setFoo("b")` → `apply()` uses `"b"`).

## TypeScript Component Details

### `xterm-element.ts` — Base element and infrastructure

**`CustomKeyEventHandlerRegistry`**:
- Sparse array (by integer ID) of `{predicate, handle}` pairs, iterated in insertion order (tracked by `indexes[]`).
- `register(customKey|predicate, handler)` returns `{id, dispose(), unshift()}`.
- `unshift(id)` moves a handler to the front of the processing queue.
- `dispose()` / `remove(id)` removes a handler.
- When handling: all matching predicates are evaluated first, then handlers are called in registration order.
- `ev.stopImmediatePropagation()` stops subsequent handlers in the same registry invocation.
- If `ev.requestCustomEvent` is truthy, fires a `CustomKey` DOM CustomEvent after all handlers run.
- After handling, resets `(core as any)._keyDownSeen = false` (workaround for issue #59 — prevents xterm.js from double-processing the key).

**`XTermElement`** (LitElement):
- Shadow DOM renders only `<slot name="terminal-container">`. The actual terminal div is in light DOM (`slot="terminal-container"`) — created by `XTermBase` Java constructor.
- Terminal is opened in `_slotchange()` → `requestAnimationFrame` → `terminal.open(slotted div)` → dispatches `terminal-initialized`.
- Bell: pre-generated 800 Hz sine WAV (0.1 s, 44100 Hz) encoded as `data:Audio/WAV;base64,...`. `bellStyle` defaults to `'none'`; set to `'sound'` to enable via `setBellStyle(BellStyle.SOUND)`.
- `_onData(e)`: replaces `\r` with `\x1b[<N\n` — routes Enter through the console mixin's custom CSI linefeed handler.
- `disconnectedCallback`: **disposes** the current terminal instance and creates a fresh `new Terminal()`. All client-side state (scrollback, cursor, addons) is lost. Use `PreserveStateAddon` to restore it.
- `disabled` property: sets `terminal.options.disableStdin` and toggles cursor visibility (`\x1b[?25l` hide, `\x1b[?25h` show).
- `addons: Object = {}` — populated by `ClientTerminalAddon` from the server side. Key = addon name, value = the JS addon object.

**`TerminalAddon<T>` (abstract TS class)**:
- `$` — reference to the mixin host (typed as `T extends TerminalMixin`).
- `$node` — the `XTermElement` instance.
- `$core` — `(terminal as any)._core` (xterm.js internal core, not part of public API).
- `activate()` is the xterm.js `ITerminalAddon.activate` implementation — stores refs and calls `activateCallback`.
- `dispose()` calls `dispose()` on all `_disposables`.

### `xterm-console-mixin.ts` — Console feature

Custom CSI escape sequences with `<` prefix to avoid conflicts with standard xterm.js sequences. These are used both by key handlers (sent to `terminal.write`) and directly testable via `XTermElement.write()`:

| Sequence | Param | Action |
|---|---|---|
| `\x1b[<H` | — | Cursor home: moves to start of logical line, then forward past prompt |
| `\x1b[<E` | — | Cursor end: moves to end of logical (possibly wrapped) line |
| `\x1b[<L` | n (default 1) | Cursor backward wrapped: moves left n chars, crossing line boundaries |
| `\x1b[<R` | n (default 1) | Cursor forward wrapped: moves right n chars, crossing line boundaries |
| `\x1b[<B` | — | Backspace (wrapped): moves back then deletes char, handles wrapped lines |
| `\x1b[<D` | n (default 1) | Delete n chars (wrapped): shifts remaining chars left across wrapped lines |
| `\x1b[<K` | 0/1/2 | Erase in logical line (extends standard erase-in-line across wrapped rows) |
| `\x1b[<N` | — | Linefeed/Enter: fires `line` DOM event with current line text |

Key bindings registered in `ConsoleAddon.activateCallback`:
- `Home`/`End` → send `\x1b[<H` / `\x1b[<E`
- `ArrowLeft`/`ArrowRight` → send `\x1b[<L` / `\x1b[<R`
- `Backspace`/`Delete` → send `\x1b[<B` / `\x1b[<D`
- `Insert` → toggles `insertMode`
- `Enter` → sends `\x1b[<N\n`
- `ArrowUp`/`ArrowDown` + `F1`–`F4`/`F7`–`F11` → `preventDefault`, suppressed
- `F5`/`F6`/`F12` → handler registered with no action (still suppressed from terminal)
- `Escape` → always suppressed from xterm.js processing (handler is registered so `handled=true`). The handler returns `this.$.escapeEnabled` but this return value is discarded by the registry — `escapeEnabled` has no effect on key suppression in the current implementation.

`insertMode` setter writes `\x1b[4h\x1b[3 q` (insert mode + bar cursor) or `\x1b[4l\x1b[2 q` (overwrite mode + block cursor).

`currentLine` reads from `buffer.getWrappedRangeForLine()` — concatenates all physical rows in the logical line, strips trailing whitespace, then strips the prompt prefix if `__yPrompt` matches the first row of the wrapped range.

`writePrompt` uses a blocking callback pattern: `terminal.write('', () => addon.writePrompt())` to guarantee all pending writes are flushed before the prompt is positioned.

`__yPrompt` tracks which buffer row the prompt was written on. If `writePrompt` is called again on the same row (re-prompting), it overwrites rather than inserting a new one.

### `xterm-clipboard-mixin.ts` — Clipboard feature

- `useSystemClipboard` defaults to `"write"` (writes to system clipboard, reads from internal buffer only). Set to `"readwrite"` to also paste from system clipboard.
- `Ctrl-C` and `Ctrl-V` are registered with no handler — this suppresses xterm.js default behavior (xterm.js would otherwise let Ctrl-C through to the terminal). Server-side key listeners may handle these if needed.
- Auto-copy on selection: `terminal.onSelectionChange` writes selected text to `writeText()` when `copySelection` is true.
- Right-click (`contextmenu`) pastes if `pasteWithRightClick` is true and `rightClickSelectsWord` option is false.
- Middle-click (`auxclick` with `button == 1`) pastes if `pasteWithMiddleClick` is true.
- Both paste operations check `_internalClipboard !== undefined` before attempting to paste.
- `navigator.clipboard` errors are caught and logged to console (not re-thrown).

### `xterm-fit-mixin.ts` — Fit feature

Subclasses the npm `FitAddonBase` from `xterm-addon-fit`. After each `fit()` call, sets `viewport._viewportElement.style.width = 'unset'` in a `requestAnimationFrame` (fixes a layout overflow bug). `fitOnResize` defaults to `true` — adds a `window.resize` listener. The public `fit()` method calls `proposeDimensions()` synchronously then defers the actual `fit()` call via `setTimeout` (to handle the case where the element is not yet sized).

### `xterm-selection-mixin.ts` — Keyboard selection feature

`keyboardSelectionEnabled` defaults to `true` on the client side.

Selection state:
- `__selectionAnchor`: flat position (`y * cols + x`) where selection started; `undefined` when no selection.
- `__selectionLength`: number of characters selected (always non-negative after direction flip).
- `__selectionRight`: whether the active end is to the right of the anchor.

Shift+Arrow extends the selection in the arrow direction. When selection length would go negative, the direction flips and `__selectionRight` is toggled. Plain Arrow (no shift) clears selection via `terminal.clearSelection()`.

Shift+Home: moves anchor to start of logical line (accounting for prompt). Shift+End: moves to end of logical line.

Delete/Backspace with active selection: if the selection is entirely within the current logical line, uses custom CSI sequences to delete the selected characters (`\x1b[<{len}L` cursor back if selecting leftward, then `\x1b[<{len}D` delete chars). Stops further processing via `ev.stopImmediatePropagation()`. If selection spans multiple lines or is outside the current line, just clears selection.

### `xterm-insertfix-mixin.ts` — Insert mode fix

Patches xterm.js internal `_inputHandler._parser._printHandler` to fix a bug where inserting characters at a wrapped line boundary in insert+wraparound mode causes incorrect behavior. When the condition is met (insert mode + wraparound + overflow), it:
1. Finds the wrapped line range.
2. Wraps overflow characters from the last line to a new line below.
3. Shifts characters from each line in the range to the next.
4. Then calls the original print handler.

The original handler is restored on `dispose()`.

### `xterm.ts` — Final composition

```typescript
@customElement('fc-xterm')
class XTermComponent extends XTermInsertFixMixin(
  XTermClipboardMixin(
    XTermConsoleMixin(
      XTermSelectionMixin(
        XTermFitMixin(XTermElement)
      )
    )
  )
) {}
```

Mixin application order matters for addon loading. Every mixin calls `super.connectedCallback()` before its own setup code, so the innermost class's code runs first. Actual addon loading order:
1. `XTermElement.connectedCallback` — terminal `onData`/bell/custom-key handler setup
2. `XTermFitMixin` — loads `FitAddon`, calls `this.fit()`
3. `XTermSelectionMixin` — loads `SelectionAddon`
4. `XTermConsoleMixin` — loads `ConsoleAddon`
5. `XTermClipboardMixin` — loads `ClipboardAddon`
6. `XTermInsertFixMixin` — loads `InsertFixAddon` (outermost class, but its own code runs last)

## Demo Views

`DemoLayout` — trivial `RouterLayout` (full-size Div). Used as parent layout for demo routes.

`DemoView` (`@Route("")`) — immediately forwards to `XtermDemoView` via `BeforeEnterEvent.forwardTo`.

`XtermDemoView` (`@Route("xterm")`, layout `DemoLayout`) — the actual interactive demo. Demonstrates real-world usage of the API:
- Sets prompt `[user@xterm ~]$ `
- Enables `BellStyle.SOUND`, `CursorStyle.UNDERLINE`, cursor blink
- Enables clipboard (READWRITE), right-click paste, middle-click paste, copy-on-select
- Calls `TerminalHistory.extend(xterm)` for arrow-up/down history
- Commands handled in `addLineListener`: `time`, `date`, `beep` (`\u0007`), `color on`/`color off` (changes `TerminalTheme`), `history` (prints `TerminalHistory.of(xterm).getLines()`), `prompt off`/`prompt <text>`
- `showHistory()` uses `TerminalHistory.of(xterm).getLines()` and formats with index

## Testing

### Unit tests (run without a browser)

**`PreserveStateAddonTest`**:
- `smoke()` — just `new PreserveStateAddon(new XTerm())`
- `writeGoesToScrollbackBuffer` — `write("foo")`, `writeln("bar")`, `write("baz")` → buffer = `"foobar\nbaz"`
- `promptGoesToScrollbackBufferAfterSubmit` — fires `LineEvent` via `ComponentUtil.fireEvent(component, new ITerminalConsole.LineEvent(xterm, true, "line"))` — prompt+line+`\n` are added per submit, not on `writePrompt()`
- `clearClearsScrollbackBuffer` / `resetClearsScrollbackBuffer` — verify buffer is empty after clear/reset

**`SerializationTest`**:
- `testSerialization()` — round-trips `new XTerm()` through Java serialization
- `testTerminalHistorySerialization()` — verifies that `ListIterator` cursor position is preserved through serialization/deserialization (uses reflection to access package-private `listIterator()`)

**`StateMemoizerTest`**: covers empty apply, pass-through, partial replay, sequential setter calls (last value wins).

### Integration tests (`*IT.java`, browser-based)

All extend `AbstractViewTest`:
- Sets up ChromeDriver via `WebDriverManager.chromedriver().setup()`
- Navigates to `http://localhost:8080/xterm/it` (configurable via constructor)
- Calls `getCommandExecutor().waitForVaadin()` after navigation
- `ScreenshotOnFailureRule` saves screenshots on test failure
- Hub mode: if system property `test.use.hub=true`, connects to Selenium Hub at env var `HOSTNAME`

**`XTermElement`** (TestBench page object for `<fc-xterm>`):
- `write(text)` — calls `this.terminal.write(text)` via JS (does not wait for flush)
- `sendKeys(keys)` — sends to `this.terminal.textarea` (hidden input), then waits for xterm.js to finish processing via `executeAsyncScript` with a `terminal.write('', callback)` barrier
- `currentLine()` — reads element property `this.currentLine`
- `lineAtOffset(n)` — reads `buffer.lines.get(ybase+y+n)`, returns trimmed string
- `cursorPosition()` — returns `new Position(buffer.cursorX, buffer.cursorY)`
- `getColumnWidth()` — returns `this.terminal.cols`
- `getSelection()` — returns `this.terminal.getSelection()`
- `setUseSystemClipboard(boolean)` — sets element property `useSystemClipboard`
- `setPrompt(String)` — sets element property `prompt`
- `executeScript(script, args...)` — binds `this` to the element: wraps script as `function(arguments){...}.bind(element)(args)` so `this` is always the `<fc-xterm>` element

**`Position`** — cursor position helper (Lombok `@AllArgsConstructor`, `@EqualsAndHashCode`, `@ToString`):
- `advance(dx, dy)` — **mutates** this instance, returns `this`
- `plus(dx, dy)` — returns a **new** `Position`
- `adjust(columnWidth)` — converts flat index (`x + y*cols`) back to `(col, row)` — used when computing positions that wrap across lines

**`XTermTestUtils.makeFullLine(term, hasPrompt)`** — builds a string of repeating `"0123456789"` that exactly fills from the current cursor x (if `hasPrompt=true`) or from column 0 (if `false`) to the right edge. Used to construct lines that wrap.

**`XTermIT`** — Tests:
- `componentWorks` — verifies `<fc-xterm>` has a shadowRoot and the slotted terminal-container div
- `writeText` — typing text and pressing Enter updates `currentLine` and `cursorPosition` correctly
- `customKeyHandlerLowLevel` — `customKeyEventHandlers.register(predicate, handler)` API: multiple handlers for the same key all fire; `dispose()` removes them
- `customKeyHandlerRegistrationOrder` — handlers fire in registration order (verifies `keyCount` goes `1→12`, not `21`)
- `customKeyHandlerStopImmediatePropagation` — calling `ev.stopImmediatePropagation()` in a handler prevents later handlers from running (but earlier ones already ran)
- `customKeyHandlerHighLevel` — `registerCustomKeyListener({key:'E'})` returns a numeric ID; the `CustomKey` DOM event fires **once** even if the key is registered multiple times; deregister via `customKeyEventHandlers.remove(id)`

**`ConsoleFeatureIT`** — Tests:
- `testWriteWrappedLine` — typing more chars than the column width wraps correctly; `currentLine` returns the full logical line
- `testFeature` — all keyboard navigation: Arrow keys, Home, End, Backspace, Delete, Insert mode toggle; also tests that insert mode correctly pushes characters into a wrapped line
- `testCsiSequences` — directly sends CSI escape sequences via `term.write()` and verifies cursor position / line content
- `testCsiSequencesWrapped` — same but on a multi-row wrapped line, including parameterized `\x1b[<nL]` / `\x1b[<nR]` / `\x1b[<nD]`

**`ClipboardFeatureIT`** — Tests mouse-based clipboard: drag to select text, then right-click pastes it (using internal clipboard). Sets `useSystemClipboard=false` to avoid browser permission dialogs.

**`FitFeatureIT`** — Tests that halving the window width reduces `terminal.cols` by roughly half, and restoring it brings cols back. Waits with `waitUntil` polling.

**`SelectionFeatureIT`** — Tests Shift+Arrow selection and deletion (5 scenarios):
1. Select left → Backspace deletes
2. Select left → Delete deletes
3. Select right → Delete deletes
4. Select right → Backspace deletes
5. Shift+Home/End → Delete deletes entire logical line (including wrapped)

**`TerminalHistoryIT`** — Tests arrow-key navigation:
- Basic up/down through history entries
- Arrow-down past the last entry restores the line that was typed before pressing Arrow-up (`initialLine`)
- Running a command from history (Arrow-up → Enter) resets history position back to end
- Empty command (cleared via backspaces → Enter) does not add an entry; history position resets correctly

**`SampleClientTerminalAddonIT`** — Tests the full `ClientTerminalAddon` round-trip:
- Uses `HasRpcSupport` from `testbench-rpc` to call `@ClientCallable` methods: `IntegrationViewCallables $server = createCallableProxy(IntegrationViewCallables.class)` then `$server.setSampleClientTerminalAddonValue("BAR")`
- Reads the value back from the client: `term.executeScript("return this.addons[arguments[0]].value;", SampleClientTerminalAddon.NAME)`

## Code Conventions

- **Package:** `com.flowingcode.vaadin.addons.xterm`
- **License headers:** Every `.java` and `.ts` file requires the Apache 2.0 Maven license plugin header
- **Lombok:** `@With`, `@Getter`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@FieldDefaults`, `@Delegate`, `@SneakyThrows`, `@ExtensionMethod(JsonMigration.class)`, `@EqualsAndHashCode`, `@ToString`
- **Interfaces first:** New features get their own `ITerminal*.java` interface with default methods before being implemented in `XTerm`
- **Enums in options:** All `ITerminalOptions` enums use uppercase Java names; the proxy converts them to lowercase for xterm.js
- **Vaadin element properties vs. JS terminal options:** clipboard/fit/selection features use Vaadin element properties (`setProperty`/`getProperty`); core terminal options use the proxy bridge to `this.terminal.options`
- **Serializable:** All production classes implement `Serializable` (Vaadin HTTP session requirement)
- **Unit testing events without a browser:** Use `ComponentUtil.fireEvent(component, new SomeEvent(source, true, ...))` to fire `@DomEvent`-annotated events
- **Commits:** Conventional Commits enforced by CI (`commits.yml`)
- **Integration test naming:** `*IT.java` suffix (Maven Failsafe plugin)

## CI/CD

`.github/workflows/`:
- `vaadin24.yml` — default build on `master` (Java 17)
- `vaadin25.yml` — `-Pv25` variant on `master` (Java 21)
- `vaadin23.yml` — runs on `2.x` branch (Java 11)
- `vaadin14.yml` — runs on `1.x` branch (Java 8)
- `commits.yml` — validates conventional commits on all branches/PRs

## Key Internal Details

- **Initialization sequence:** `XTermBase` constructor → appends light-DOM `<div slot="terminal-container">` → client `_slotchange()` fires in `requestAnimationFrame` → `terminal.open(div)` → fires `terminal-initialized` → server drains `deferredCommands`.
- **Deferred commands vs. return values:** All `void` JS calls made before `terminal-initialized` are buffered in `deferredCommands` and replayed. Any call returning a `CompletableFuture` before initialization throws `IllegalStateException` immediately.
- **Custom CSI prefix `<`:** The console mixin uses the `<` intermediate byte (`0x3C`) in CSI sequences to namespace all its operations. Standard xterm.js sequences never use this prefix, so there is no conflict.
- **`_onData` Enter routing:** The LitElement `_onData` handler replaces `\r` with `\x1b[<N\n` before writing to the terminal. This routes the Enter key through the console mixin's CSI handler, which reads `currentLine` and fires the `line` DOM event.
- **Vaadin version compatibility in `ClientTerminalAddon`:** `encodeWithTypeInfo` is resolved at runtime via `MethodHandle` lookup — uses `com.vaadin.flow.internal.JsonCodec` (Vaadin ≤24) or `com.vaadin.flow.internal.JacksonCodec` (Vaadin ≥25). Never hard-code this path.
- **GraalVM native image:** `src/main/resources/META-INF/native-image/…/proxy-config.json` and `reflect-config.json` are pre-configured for the dynamic proxy (`XTermBase` static initializer) and field reflection (`TerminalTheme.asJsonObject()`).
- **`Position.advance()` mutates:** The `Position` helper class's `advance()` method modifies the instance in place and returns `this`. Use `plus()` to get a new instance without mutation.
- **`TerminalHistory` is not a `TerminalAddon`:** It does not extend `TerminalAddon` and is not registered via `registerServerSideAddon`. It is attached to the component via `ComponentUtil.setData`. Only one instance per terminal is allowed.
