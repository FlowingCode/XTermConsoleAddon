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
package com.flowingcode.vaadin.addons.xterm.integration;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
class Position {
  int x, y;
  
  /**Increments the position by <code>dx,dy</code>.*/
  public Position advance(int dx, int dy) {
    this.x+=dx;
    this.y+=dy;
    return this;
  }

  /**Return a new position that is equal to this position plus <code>dx,dy</code>.*/
  public Position plus(int dx, int dy) {
    return new Position(this.x+dx, this.y+dy);
  }
  
}
