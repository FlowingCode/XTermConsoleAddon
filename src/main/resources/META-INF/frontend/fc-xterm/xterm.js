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
import {PolymerElement} from '@polymer/polymer/polymer-element.js';
import {html} from '@polymer/polymer/lib/utils/html-tag.js';

import { Terminal } from 'xterm';
import { InsertFixFeature } from '@vaadin/flow-frontend/fc-xterm/insert-fix-feature.js';

export class XTerm extends PolymerElement {
	
	static get is() { return 'fc-xterm'; }
	
	static get template() {
		return html`
		<slot id="slot" name="terminal-container"></slot>
		<slot name="feature"></slot>
		`;
	}
	
	static get properties() {
		return {
			disabled: {
				type: Boolean,
				observer: '_disabledChanged'
			}
		};
	}
	
	connectedCallback() {
 		super.connectedCallback();
 		
		this.terminal = new Terminal();
		this.terminal.open(this.$.slot.assignedNodes()[0]);
		
		this.features = {};
		this.customKeys = []; //keys that should NOT be processed by the terminal
		
		let term = this.terminal;
		term.setOption('convertEol', true);
		
		//sparse array of {predicate: function(KeyEvent):boolean, handler: function(KeyEvent):boolean}
		this.__customKeyEventHandlers = [];
		this.__customKeyEventHandlers.next = 0;
		
		term.addCustomKeyEventHandler = (predicate, handler) => {
			let id = ++this.__customKeyEventHandlers.next;
			if (!handler) handler= ()=>false;
			this.__customKeyEventHandlers[id] = {predicate, handler};
			return {dispose : () => term.removeCustomKeyEventHandler(id)};
		};
		
		term.removeCustomKeyEventHandler = (id) => Number.isInteger(id) && delete this.__customKeyEventHandlers[id];	
		
		term.attachCustomKeyEventHandler(ev => {
			if (ev.type!=='keydown') return false;

			//test latest applicable handler for event
			let j=-1;
			for(const i in this.__customKeyEventHandlers) {
				if (/\d+/.test(i) && this.__customKeyEventHandlers[i].predicate(ev)) j=Math.max(j, parseInt(i));
			}
			if (j>=0 && !this.__customKeyEventHandlers[j].handler(ev)) return false;
			
			//(C) T.J. Crowder  cc-by-sa 4.0
			//https://stackoverflow.com/a/35430956/1297272
			let properties = ['key', 'code', 'location', 'ctrlKey', 'altKey', 'metaKey', 'shiftKey'];
			if (this.customKeys.find(customKey => properties.every(property => customKey[property] === ev[property] || customKey[property]===undefined))) {
				this.dispatchEvent(new CustomEvent('CustomKey', {detail: ev}));
			} else if (ev.key==' ') {
				term.write(' ');
			} else {
				return true;
			}
		});
		
		//onLineFeed doesn't distinguish lines from user input and lines from terminal.write
		//<N CSI is handled by console-feature
		term.onData(e => {
			term.write(e.replace(/\r/g,'\x1b[<N\n'));
		});
		
		//https://gist.github.com/xem/670dec8e70815842eb95
		term.setOption('bellSound','data:audio/wav;base64,UklGRl9vT19XQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YU'+Array(500).join(123));
	 		 	
	 	this._loadFeature('insertFix', new InsertFixFeature());
	}
	
	_loadFeature(name, feature) {
		this.terminal.loadAddon(feature);
		if (name) {
			let dispose = feature.dispose.bind(feature);
			this.features[name] = feature;
			feature.dispose = () => {
				dispose();
				if (this.features[name] === feature) {
					delete this.features[name];
				}
			}
		}
	}
	 	
	disconnectedCallback() {
 		super.disconnectedCallback();
 		this.terminal.dispose();
 	}

	_disabledChanged(disabled) {
		this.terminal.setOption('disableStdin', disabled);
		this.terminal.write(disabled?"\x1b[?25l":"\x1b[?25h");
	}
 	
}

customElements.define(XTerm.is, XTerm);
