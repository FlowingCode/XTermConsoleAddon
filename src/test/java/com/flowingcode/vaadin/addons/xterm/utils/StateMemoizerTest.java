package com.flowingcode.vaadin.addons.xterm.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StateMemoizerTest {

    private StateMemoizer memoizer;
    private BunchOfSetters proxy;
    private MyBean bean;

    public static interface BunchOfSetters {
        void setFoo(String value);
        void setBar(int value);
    }

    public static class MyBean implements BunchOfSetters {
        private String foo;
        private int bar;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public int getBar() {
            return bar;
        }

        public void setBar(int bar) {
            this.bar = bar;
        }
    }

    @Before
    public void setupTestValues() {
        bean = new MyBean();
        memoizer = new StateMemoizer(bean, BunchOfSetters.class);
        proxy = (BunchOfSetters) memoizer.getProxy();
    }

    @Test
    public void emptyMemoizerApplySucceedsButDoesNothing() {
        memoizer.apply();
        assertNull(bean.getFoo());
        assertEquals(0, bean.getBar());
    }

    @Test
    public void proxyModificationPassesValuesThrough() {
        proxy.setFoo("foo");
        assertEquals("foo", bean.getFoo());
        proxy.setBar(25);
        assertEquals(25, bean.getBar());
    }

    @Test
    public void applyAppliesInvokedSettersOnly() {
        proxy.setFoo("foo");
        bean.setFoo("bar");
        bean.setBar(25);
        memoizer.apply();
        assertEquals("foo", bean.getFoo());
        assertEquals(25, bean.getBar());
    }

    @Test
    public void applyBasicTest() {
        proxy.setFoo("foo");
        proxy.setBar(25);
        bean.setFoo("FOO");
        bean.setBar(26);
        memoizer.apply();
        assertEquals("foo", bean.getFoo());
        assertEquals(25, bean.getBar());
    }

    @Test
    public void consequentSetterCallsAppliedProperly() {
        proxy.setFoo("foo");
        assertEquals("foo", bean.getFoo());
        proxy.setFoo("bar");
        assertEquals("bar", bean.getFoo());
        proxy.setBar(25);
        bean.setFoo("FOO");
        bean.setBar(26);
        memoizer.apply();
        assertEquals("bar", bean.getFoo());
        assertEquals(25, bean.getBar());
    }
}
