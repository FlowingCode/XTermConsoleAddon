package com.flowingcode.vaadin.addons.xterm.utils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Remembers the values passed to all setters. At any time you can reapply
 * those calls by using {@link #apply()}.
 */
public class StateMemoizer implements Serializable {
    /**
     * Remember all calls to all setters; also remember what args were passed to those setters.
     */
    private final Map<String, Serializable> setterCalls = new HashMap<>();
    /**
     * Pass-through the setters here.
     */
    private final Object delegate;
    /**
     * Setters invoked on this proxy will have the args remembered; the methods invocations
     * will then be passed on to {@link #delegate}.
     */
    private final Object proxy;

    /**
     * Creates the memoizer. Remember to invoke interface methods via {@link #getProxy()} in
     * order for this to work.
     * @param delegate pass-through the setters here.
     * @param interfaces used to create the memoizing proxy.
     */
    public StateMemoizer(Object delegate, Class<?>... interfaces) {
        this.delegate = Objects.requireNonNull(delegate);
        proxy = Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, (proxy, method, args) -> {
            if (method.getName().startsWith("set") && args.length == 1) {
                // remember the state
                setterCalls.put(method.getName(), (Serializable) args[0]);
            }
            return method.invoke(delegate, args);
        });
    }

    /**
     * Setters invoked on this proxy will have the args remembered; the methods invocations
     * will then be passed on to {@link #delegate}.
     * @return the proxy, not null.
     */
    public Object getProxy() {
        return proxy;
    }

    /**
     * Calls all setters again on {@link #delegate}.
     */
    public void apply() {
        setterCalls.forEach((k, v) -> {
            final Method method = Arrays.stream(delegate.getClass().getMethods()).filter(it -> it.getName().equals(k)).findAny().get();
            try {
                method.invoke(delegate, v);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
