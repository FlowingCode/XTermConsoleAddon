/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2025 Flowing Code
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyLocation;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonNull;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.Delegate;

/** Server-side component for the XTerm component. */
@SuppressWarnings("serial")
@NpmPackage(value = "xterm", version = "5.1.0")
@JsModule("./fc-xterm/xterm-element.ts")
@CssImport("xterm/css/xterm.css")
public abstract class XTermBase extends Component
    implements ITerminal, ITerminalOptions, HasSize, HasEnabled {

  @Delegate private ITerminalOptions terminalOptionsProxy;

  @Delegate private ITerminal terminalProxy;

  private List<Command> deferredCommands;

  private final List<TerminalAddon> addons = new ArrayList<>();

  private class ProxyInvocationHandler implements InvocationHandler, Serializable {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getDeclaringClass() == Object.class) {
        return method.invoke(this, args);
      }

      Function<JsonValue, Object> mapping = getResultTypeMapper(method);

      String name = method.getName();
      CompletableFuture<JsonValue> result = invoke(mapping!=null, name, args);

      if (mapping != null) {
        return result.thenApply(json -> (json instanceof JsonNull) ? null : mapping.apply(json));
      } else {
        return null;
      }
    }

    private Function<JsonValue, Object> getResultTypeMapper(Method method) {
      if (method.getReturnType() == Void.TYPE) {
        return null;
      } else if (method.getReturnType() == CompletableFuture.class) {
        ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
        Class<?> resultType = (Class<?>) type.getActualTypeArguments()[0];

        if (resultType == Void.class) {
          return x -> null;
        } else if (resultType == String.class) {
          return JsonValue::asString;
        } else if (resultType == Boolean.class) {
          return JsonValue::asBoolean;
        } else if (resultType == Integer.class) {
          return json -> (int) json.asNumber();
        } else {
          throw new AbstractMethodError(method.toString());
        }
      } else {
        throw new AbstractMethodError(method.toString());
      }
    }

    private CompletableFuture<JsonValue> invoke(boolean hasResult, String name, Object[] args) {
      if (name.startsWith("set") && args.length == 1) {
        name = name.substring("set".length());
        name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        Serializable arg;
        if (args[0] instanceof Enum) {
          arg = ((Enum<?>) args[0]).name().toLowerCase(Locale.ENGLISH);
        } else if (args[0] instanceof TerminalTheme) {
          arg = ((TerminalTheme) args[0]).asJsonObject();
        } else {
          arg = (Serializable) args[0];
        }
        if (name.equals("bellStyle") || name.equals("bellSound")) {
          return executeJs(false, "this[$0]=$1", name, arg);
        } else {
          return executeJs(false, "this.terminal.options[$0]=$1", name, arg);
        }
      } else if (args == null || args.length == 0) {
        return executeJs(hasResult, "return this.terminal[$0]()", name);
      } else if (args.length == 1) {
        return executeJs(hasResult, "return this.terminal[$0]($1)", name, (Serializable) args[0]);
      } else {
        Serializable[] sargs = new Serializable[args.length];
        System.arraycopy(args, 0, sargs, 0, args.length);
        String expr =
            IntStream.rangeClosed(1, args.length)
                .mapToObj(i -> "$" + i)
                .collect(Collectors.joining(","));
        return executeJs(hasResult, "return this.terminal[$0](" + expr + ")", name, sargs);
      }
    }
  }

  private static final Class<?> optionsProxyClass;

  static {
    optionsProxyClass =
        Proxy.getProxyClass(
            XTermBase.class.getClassLoader(), ITerminal.class, ITerminalOptions.class);
  }

  /** Constructs a new instance of {@code XTerm} */
  public XTermBase() {
    // initialize delegate proxies
    try {
      Object proxy =
          optionsProxyClass
              .getConstructor(InvocationHandler.class)
              .newInstance(new ProxyInvocationHandler());
      terminalProxy = (ITerminal) proxy;
      terminalOptionsProxy = (ITerminalOptions) proxy;
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    // the inner <div> must be in the light DOM
    Element div = new Element("div");
    div.setAttribute("slot", "terminal-container");
    div.getStyle().set("width", "100%");
    div.getStyle().set("height", "100%");
    getElement().appendChild(div);

    deferredCommands = new LinkedList<>();

    Registration r[] = new Registration[1];
    r[0] = getElement().addEventListener("terminal-initialized", ev -> afterInitialization());
  }

  private void afterInitialization() {
    if (deferredCommands != null) {
      List<Command> deferredCommands = this.deferredCommands;
      this.deferredCommands = null;
      deferredCommands.forEach(Command::execute);
    }
  }

  private CompletableFuture<JsonValue> executeJs(boolean hasResult, String expression, Serializable... parameters) {
    if (!hasResult) {
      executeJs(expression, parameters);
      return CompletableFuture.completedFuture(null);
    } else if (deferredCommands == null) {
      return getElement().executeJs(expression, parameters).toCompletableFuture();
    } else {
      throw new IllegalStateException("Terminal is not initialized");
    }
  }

  protected void executeJs(String expression, Serializable... parameters) {
    if (deferredCommands == null) {
      getElement().executeJs(expression, parameters);
    } else {
      deferredCommands.add(() -> getElement().executeJs(expression, parameters));
    }
  }

  /**
   * Add a server-side key listener. This method is equivalent to calling {@link
   * #addCustomKeyListener(DomEventListener, Key, KeyModifier...)} with a {@link KeyLocation} of
   * {@code null}.
   *
   * @return a registration for the listener.
   */
  public Registration addCustomKeyListener(
      DomEventListener listener, Key key, KeyModifier... modifiers) {
    return addCustomKeyListener(listener, key, null, modifiers);
  }

  /**
   * Add a server-side key listener.
   *
   * @return a registration for the listener.
   */
  public Registration addCustomKeyListener(
      DomEventListener listener, Key key, KeyLocation location, KeyModifier... modifiers) {
    return addCustomKeyListener(
        listener, key.getKeys(), location, new HashSet<>(Arrays.asList(modifiers)));
  }

  private Registration addCustomKeyListener(
      DomEventListener listener,
      List<String> keys,
      KeyLocation location,
      Set<KeyModifier> modifiers) {
    JsonArray array = Json.createArray();
    for (String key : keys) {
      JsonObject json = Json.createObject();
      json.put("code", key);
      if (location != null) {
        json.put("location", location.getLocation());
      }
      json.put("ctrlKey", modifiers.contains(KeyModifier.CONTROL));
      json.put("altKey", modifiers.contains(KeyModifier.ALT));
      json.put("metaKey", modifiers.contains(KeyModifier.META));
      json.put("shiftKey", modifiers.contains(KeyModifier.SHIFT));
      array.set(array.length(), json);
    }

    StringBuilder sb = new StringBuilder("");
    sb.append(
            IntStream.range(0, array.length())
                .mapToObj(i -> array.getObject(i).getString("code"))
                .map(s -> String.format("'%s'", s))
                .collect(Collectors.joining(",", "[", "]")))
        .append(".includes(event.detail.code)");

    JsonObject json = array.getObject(0);
    if (location != null) {
      sb.append(" && event.detail.location=").append(json.getNumber("location"));
    }

    for (String modifier : Arrays.asList("ctrlKey", "altKey", "metaKey", "shiftKey")) {
      sb.append(json.getBoolean(modifier) ? "&& " : "&& !")
          .append("event.detail.")
          .append(modifier);
    }

    String filter = sb.toString();
    Registration r = getElement().addEventListener("CustomKey", listener).setFilter(filter);
    executeJs("this.registerCustomKeyListener($0)", json);
    return r::remove;
  }

  @Override
  public void setEnabled(boolean enabled) {
    HasEnabled.super.setEnabled(enabled);
  }

  /**
   * Retrieves a registered server-side add-on instance of a specific type.
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>{@code
   * MySpecificAddon addon = terminal.getAddon(MySpecificAddon.class);
   * if (addon != null) {
   *   addon.doSomethingSpecific();
   * }
   * }</pre>
   *
   * @param <T> the type of the add-on to retrieve. This is inferred from the {@code clazz}
   *        parameter.
   * @param clazz the {@code Class} object representing the type of the add-on to retrieve. Must not
   *        be {@code null}.
   * @return the registered add-on instance that is of the specified {@code Class<T>}, or
   *         {@code null} if no such add-on is found
   * @throws NullPointerException if {@code clazz} is {@code null}
   */
  public <T extends TerminalAddon> T getAddon(Class<? extends T> clazz) {
    return addons.stream().filter(clazz::isInstance).map(clazz::cast).findFirst().orElse(null);
  }

  /**
   * Registers a server-side add-on with this terminal instance. This method is called by the add-on
   * itself during its construction.
   *
   * @param addon the add-on to register. Must not be {@code null}.
   * @throws NullPointerException if {@code addon} is {@code null}
   * @throws IllegalStateException if an add-on of the same class as the provided {@code addon} is
   *         already registered with this terminal instance
   */
  final <T extends TerminalAddon> void registerServerSideAddon(T addon) {
    if (getAddon(addon.getClass()) != null) {
      throw new IllegalStateException("Addon already registered: " + addon.getClass().getName());
    }
    addons.add(addon);
  }

}
