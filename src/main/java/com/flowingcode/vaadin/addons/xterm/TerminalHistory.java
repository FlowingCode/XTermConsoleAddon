package com.flowingcode.vaadin.addons.xterm;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.shared.Registration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Manages a command history buffer for {@link XTerm}. */
@SuppressWarnings("serial")
public class TerminalHistory implements Serializable {

  private LinkedList<String> history = new LinkedList<>();

  private transient ListIterator<String> iterator;

  private final XTermBase terminal;

  private List<Registration> registrations;

  private String prefix;

  private String lastRet;

  private Integer maxSize;

  public <T extends XTermBase & ITerminalConsole> TerminalHistory(T terminal) {
    this.terminal = terminal;
  }

  /**
   * Set the number of elements to retain. If {@code null} the history is unbounded.
   *
   * @throws IllegalArgumentException if the argument is negative.
   */
  public void setMaxSize(Integer maxSize) {
    if (maxSize != null && maxSize < 0) {
      throw new IllegalArgumentException();
    }
    this.maxSize = maxSize;
    iterator = null;

    if (maxSize != null) {
      while (history.size() > maxSize) {
        history.remove(0);
      }
    }
  }

  /** Sets the enabled state of the history. */
  public void setEnabled(boolean enabled) {
    if (!enabled && registrations != null) {
      registrations.forEach(Registration::remove);
      registrations = null;
    } else if (enabled && registrations == null) {
      registrations = new ArrayList<>();
      registrations.add(((ITerminalConsole) terminal).addLineListener(ev -> add(ev.getLine())));
      registrations.add(terminal.addCustomKeyListener(ev -> handleArrowUp(), Key.ARROW_UP));
      registrations.add(terminal.addCustomKeyListener(ev -> handleArrowDown(), Key.ARROW_DOWN));
      registrations.add(terminal.addCustomKeyListener(ev -> handlePageUp(), Key.PAGE_UP));
      registrations.add(terminal.addCustomKeyListener(ev -> handlePageDown(), Key.PAGE_DOWN));
    }
  }

  /** Gets the enabled state of the history. */
  public boolean isEnabled() {
    return registrations != null;
  }

  private void handleArrowUp() {
    write(previous());
  }

  private void handleArrowDown() {
    write(next());
  }

  private void handlePageUp() {
    ((ITerminalConsole) terminal).getCurrentLine().thenApply(this::findPrevious)
        .thenAccept(this::write);
  }

  private void handlePageDown() {
    ((ITerminalConsole) terminal).getCurrentLine().thenApply(this::findNext)
        .thenAccept(this::write);
  }

  private void write(String line) {
    if (line != null) {
      // erase logical line, cursor home in logical line
      terminal.write("\033[<2K\033[<H" + line);
      lastRet = line;
    }
  }

  private ListIterator<String> listIterator() {
    if (iterator == null) {
      iterator = history.listIterator(history.size());
    }
    return iterator;
  }

  private Iterator<String> forwardIterator() {
    return listIterator();
  }

  private Iterator<String> reverseIterator() {
    if (iterator == null) {
      iterator = history.listIterator(history.size());
    }
    return new Iterator<String>() {
      @Override
      public boolean hasNext() {
        return iterator.hasPrevious();
      }

      @Override
      public String next() {
        return iterator.previous();
      }
    };
  }


  /** Add a line to the history */
  public void add(String line) {
    line = line.trim();
    if (!line.isEmpty()) {
      history.add(Objects.requireNonNull(line));
      iterator = null;
    }
    if (maxSize != null && history.size() > maxSize) {
      history.removeLast();
    }
  }

  private void setCurrentLine(String currentLine) {
    if (!currentLine.equals(lastRet)) {
      prefix = currentLine;
      iterator = null;
    }
  }

  private Optional<String> find(Iterator<String> iterator, Predicate<String> predicate) {
    while (iterator.hasNext()) {
      String line = iterator.next();
      if (predicate.test(line) && !line.equals(lastRet)) {
        return Optional.of(line);
      }
    }
    return Optional.empty();
  }

  private String previous() {
    return find(reverseIterator(), line -> true).orElse(null);
  }

  private String next() {
    return find(forwardIterator(), line -> true).orElse("");
  }

  private String findPrevious(String currentLine) {
    setCurrentLine(currentLine);
    return find(reverseIterator(), line -> line.startsWith(prefix)).orElse(null);
  }

  private String findNext(String currentLine) {
    setCurrentLine(currentLine);
    return find(forwardIterator(), line -> line.startsWith(prefix)).orElse("");
  }

  /** Clears the history. */
  public void clear() {
    history.clear();
  }

  /** Returns the lines in the history. */
  public List<String> getLines() {
    return Collections.unmodifiableList(history);
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    int cursor = Optional.ofNullable(iterator).map(ListIterator::nextIndex).orElse(history.size());
    out.defaultWriteObject();
    out.writeInt(cursor);
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    int cursor = in.readInt();
    if (cursor != history.size()) {
      iterator = history.listIterator(cursor);
    }
  }

}