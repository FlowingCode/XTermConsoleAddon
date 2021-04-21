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

import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import { FitAddon } from 'xterm-addon-fit';


export class FitFeature extends PolymerElement {

	static get is() { return 'fc-xterm-fit'; }
	
	static get properties() {
		return {
			fitOnResize: {
				type: Boolean,
				value: 'true'
			},
		}
	}
	
	activate(terminal) {
		let term = terminal;
		
		this._fitAddon = new FitAddon();
		terminal.loadAddon(this._fitAddon);
		
	 	this.fit();
	 	
	 	let _fitOnResize = () => {
	 		if (this.fitOnResize) this.fit();
	 	}
	 	
	 	window.addEventListener('resize', _fitOnResize);
	 	
		this._disposables = [
			{dispose : () => {window.removeEventListener('resize', _fitOnResize);}},
			{dispose : () => {this._fitAddon.dispose(); this._fitAddon=null}}
		];

	}

	fit() {
		let dims = this._fitAddon.proposeDimensions();
		try {
	 		this._fitAddon.fit();
	 	} catch (error) {
	 		
	 	} 
	}
	
	dispose() {
		this._disposables.forEach(d => d.dispose());
		this._disposables.length = 0;
	}

	disconnectedCallback() {
 		super.disconnectedCallback();
 		this.dispose();
	}
	
	connectedCallback () {
		super.connectedCallback();
		const terminal = this.parentNode;
		terminal._loadFeature('fit', this);
	}
	  	
}

customElements.define(FitFeature.is, FitFeature);
