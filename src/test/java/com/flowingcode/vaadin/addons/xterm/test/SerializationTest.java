package com.flowingcode.vaadin.addons.xterm.test;

import com.flowingcode.vaadin.addons.xterm.XTerm;
import com.flowingcode.vaadin.addons.xterm.XTermClipboard;
import com.flowingcode.vaadin.addons.xterm.XTermConsole;
import com.flowingcode.vaadin.addons.xterm.XTermFit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;

public class SerializationTest {

  private void testSerializationOf(Object obj) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }

    try (ObjectInputStream in =
        new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
      obj.getClass().cast(in.readObject());
    }
  }

  @Test
  public void testSerialization() throws ClassNotFoundException, IOException {
    testSerializationOf(new XTerm());
    testSerializationOf(new XTermClipboard());
    testSerializationOf(new XTermConsole());
    testSerializationOf(new XTermFit());
  }
}
