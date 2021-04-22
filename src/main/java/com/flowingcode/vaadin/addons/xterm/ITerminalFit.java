package com.flowingcode.vaadin.addons.xterm;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.dependency.NpmPackage;

@NpmPackage(value = "xterm-addon-fit", version = "0.5.0")
public interface ITerminalFit extends HasElement {

  default void fit() {
    getElement().executeJs("this.fit()");
  }

  default void setFitOnResize(boolean value) {
    getElement().setProperty("fitOnResize", value);
  }

  default boolean isFitOnResize() {
    return getElement().getProperty("fitOnResize", false);
  }
}
