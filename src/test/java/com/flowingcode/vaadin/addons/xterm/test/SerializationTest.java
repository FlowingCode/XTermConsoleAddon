/*-
 * #%L
 * XTerm Console Addon
 * %%
 * Copyright (C) 2020 - 2023 Flowing Code
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
package com.flowingcode.vaadin.addons.xterm.test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import com.flowingcode.vaadin.addons.xterm.TerminalHistory;
import com.flowingcode.vaadin.addons.xterm.XTerm;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ListIterator;
import java.util.stream.IntStream;
import org.junit.Test;

public class SerializationTest {

  private <T> T shake(T obj) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }

    try (ObjectInputStream in =
        new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
      obj.getClass().cast(in.readObject());
    }

    return obj;
  }

  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {
    shake(new XTerm());
  }

  @SuppressWarnings("serial")
  private final static class TestTerminalHistory extends TerminalHistory {
    public TestTerminalHistory(XTerm terminal) {
      super(terminal);
    }

    @SuppressWarnings("unchecked")
    public ListIterator<String> listIterator() {
      try {
        Method method = TerminalHistory.class.getDeclaredMethod("listIterator");
        method.setAccessible(true);
        return (ListIterator<String>) method.invoke(this);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  public void testTerminalHistorySerialization() throws ClassNotFoundException, IOException {
    // prepare
    TestTerminalHistory history = new TestTerminalHistory(new XTerm());

    assertThat(history.listIterator(), is(notNullValue()));
    assertThat(history.listIterator().nextIndex(), is(0));

    shake(history);
    assertThat(history.listIterator(), is(notNullValue()));
    assertThat(history.listIterator().previousIndex(), is(-1));

    int n = 5;
    IntStream.range(0, n).mapToObj(Integer::toString).forEach(history::add);
    assertThat(history.listIterator().nextIndex(), is(n));

    history.listIterator().previous();
    history.listIterator().previous();
    assertThat(history.listIterator().nextIndex(), is(n - 2));

    // assert
    shake(history);
    assertThat(history.listIterator().nextIndex(), is(n - 2));
  }

}
