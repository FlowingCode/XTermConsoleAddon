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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyLocation;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.Delegate;

/** Server-side component for the XTerm component. */
@NpmPackage(value = "xterm", version = "4.11.0")
@JsModule("./fc-xterm/xterm.js")
@Tag("fc-xterm")
@CssImport("xterm/css/xterm.css")
public class XTerm extends Component implements ITerminal, ITerminalOptions, HasSize, HasEnabled {

  private static final long serialVersionUID = 1L;

  @Delegate private ITerminalOptions terminalOptionsProxy;

  @Delegate private ITerminal terminalProxy;

  private class ProxyInvocationHandler implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getDeclaringClass() == Object.class) {
        return method.invoke(this, args);
      }

      Function<JsonValue, Object> mapping = getResultTypeMapper(method);

      String name = method.getName();
      PendingJavaScriptResult result = invoke(name, args);

      if (mapping != null) {
        return result
            .toCompletableFuture()
            .thenApply(json -> (json instanceof JsonNull) ? null : mapping.apply(json));
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

    private PendingJavaScriptResult invoke(String name, Object[] args) {
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
        return getElement().executeJs("this.terminal.setOption($0,$1)", name, arg);
      } else if (args == null || args.length == 0) {
        return getElement().executeJs("this.terminal[$0]()", name);
      } else if (args.length == 1) {
        return getElement().executeJs("this.terminal[$0]($1)", name, (Serializable) args[0]);
      } else {
        Serializable[] sargs = new Serializable[args.length];
        System.arraycopy(args, 0, sargs, 0, args.length);
        String expr =
            IntStream.rangeClosed(1, args.length)
                .mapToObj(i -> "$" + i)
                .collect(Collectors.joining(","));
        return getElement().executeJs("this.terminal[$0](" + expr + ")", name, sargs);
      }
    }
  }

  private static final Class<?> optionsProxyClass;

  static {
    optionsProxyClass =
        Proxy.getProxyClass(XTerm.class.getClassLoader(), ITerminal.class, ITerminalOptions.class);
  }

  /** Constructs a new instance of {@code XTerm} */
  public XTerm() {
    // initialize delegate proxies
    try {
      Object proxy =
          optionsProxyClass
              .getConstructor(InvocationHandler.class)
              .newInstance(new ProxyInvocationHandler());
      this.terminalProxy = (ITerminal) proxy;
      this.terminalOptionsProxy = (ITerminalOptions) proxy;
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

    loadFeature(new XTermFit());
  }

  /** Install a pluggable feature in the terminal */
  public void loadFeature(XTermFeature feature) {
    getElement().appendChild(feature.getElement());
  }

  /** Install and initializes pluggable feature in the terminal */
  public <T extends XTermFeature> void loadFeature(T feature, Consumer<T> initializer) {
    loadFeature(feature);
    initializer.accept(feature);
  }

  /** Return a installed feature */
  public <T extends XTermFeature> Optional<T> getFeature(Class<T> featureType) {
    return XTermFeature.getFeatures(this, featureType).findAny();
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
    getElement().executeJs("this.customKeys.push($0)", json);
    return r::remove;
  }

  @Override
  public void setEnabled(boolean enabled) {
    HasEnabled.super.setEnabled(enabled);
  }
}
