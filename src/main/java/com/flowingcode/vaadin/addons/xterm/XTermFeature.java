/*-
 * #%L
 * XTerm Addon
 * %%
 * Copyright (C) 2020 Flowing Code
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

import java.util.stream.Stream;

import com.vaadin.flow.component.Component;

/**Pluggable module that extends the XTerm behavior.*/
public abstract class XTermFeature extends Component {

	private static final long serialVersionUID = 1L;
	
 	private static final String FEATURE_SLOT = "feature";
	
	public XTermFeature() {
		getElement().setAttribute("slot", FEATURE_SLOT);
	}
	
	protected XTerm getTerminal() {
		return (XTerm) getParent().orElse(null); 
	}
	
	static <T extends XTermFeature> Stream<T> getFeatures(XTerm xterm, Class<T> featureType) {
		return xterm.getElement().getChildren()
			.filter(e->e.getAttribute("slot").equals(FEATURE_SLOT))
			.map(e->e.getComponent().orElse(null))
			.filter(featureType::isInstance)
			.map(featureType::cast);
	}
}
