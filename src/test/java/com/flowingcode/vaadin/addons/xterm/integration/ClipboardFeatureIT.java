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
package com.flowingcode.vaadin.addons.xterm.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openqa.selenium.interactions.Actions;

import com.flowingcode.vaadin.addons.xterm.XTermClipboard;
import com.vaadin.testbench.TestBenchElement;

public class ClipboardFeatureIT extends AbstractXTermTest {
	
	@Test 
	public void testFeature() {
		TestBenchElement term = $("fc-xterm").first();
		assertFeatureHasBeenLoaded(term, XTermClipboard.class, "clipboard");
		
		write(term, "\\x1bcTEXT");
		
		int[] size = intArray(getCommandExecutor().executeScript("return [arguments[0].clientWidth, arguments[0].clientHeight]", term));
		getCommandExecutor().executeScript("arguments[0].useSystemClipboard='false'", $("fc-xterm-clipboard").first());
		
		new Actions(driver)
			.moveToElement(term, -size[0]/2, -size[1]/2)
			.clickAndHold()
			.moveByOffset(100,0)
			.release()
			.perform();
		
		new Actions(driver).contextClick().perform();
		assertThat(currentLine(term), is("TEXTTEXT"));
	}

}
