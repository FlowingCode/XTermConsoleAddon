/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2022 Flowing Code
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
