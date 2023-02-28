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
package com.flowingcode.vaadin.addons.xterm.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.openqa.selenium.Dimension;

public class FitFeatureIT extends AbstractViewTest {

  @Test
  public void testFeature() {
    XTermElement term = $(XTermElement.class).first();

    int colsBefore = term.getColumnWidth();
    Dimension dimension = getDriver().manage().window().getSize();

    getDriver().manage().window().setSize(new Dimension(dimension.width / 2, dimension.height));

    Integer colsAfter = waitUntil(driver -> {
      int w = term.getColumnWidth();
      return w != colsBefore ? w : null;
    });

    assertThat(colsAfter * 2, is(lessThanOrEqualTo(colsBefore)));

    getDriver().manage().window().setSize(dimension);

    int colsRestored = waitUntil(driver -> {
      int w = term.getColumnWidth();
      return w != colsAfter ? w : null;
    });

    assertThat(colsRestored, is(colsBefore));
  }
}
