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

export class ClipboardFeature extends PolymerElement {
	
	static get is() { return 'fc-xterm-clipboard'; }

	static get properties() {
		return {
			useSystemClipboard: {
				type: String,
				value: 'write'
			},
			pasteWithRightClick: {
				type: Boolean,
				value: false
			},
			pasteWithMiddleClick: {
				type: Boolean,
				value: false
			},
			copySelection: {
				type: Boolean,
				value: false
			}
		};
	}
	
	activate(terminal) {
		let term = terminal;
		
		let _internalClipboard = undefined;
		
		//function (String) : void
		let writeText = s => {
			_internalClipboard = s;
			if (this.useSystemClipboard=='readwrite' || this.useSystemClipboard=='write') {
				try {
					navigator.clipboard.writeText(s);
				} catch(error) {
					console.error(error);
				}
			}
		}

		//function () : Promise<String>
		let readText = () => {
			if (this.useSystemClipboard=='readwrite') {
				try {
					return navigator.clipboard.readText();
				} catch(error) {
					console.error(error);
				}
			}
			return Promise.resolve(_internalClipboard);
		}
		
		let onEvent = (event, listener) => {
			terminal.element.addEventListener(event, listener);
			return {dispose : () => terminal.element.removeEventListener(event, listener)};
		};
		
		this._disposables = [
		
		//ctrl-C ctrl-V
		term.addCustomKeyEventHandler(ev=> ev.key=='c' && ev.ctrlKey, null),
		term.addCustomKeyEventHandler(ev=> ev.key=='v' && ev.ctrlKey, null),
		
		//copy selection
		terminal.onSelectionChange(() => {
			if (this.copySelection && terminal.hasSelection()) {
				writeText(terminal.getSelection());
			}
		}),
		
		//paste with right click
		onEvent('contextmenu', ev => {
			if (this.pasteWithRightClick && !terminal.getOption('rightClickSelectsWord')) {
				ev.preventDefault();
				if (_internalClipboard!==undefined) readText().then(text=>term.paste(text));
			}
		}),
		
		//paste with middle click
		onEvent('auxclick', ev => {
			if (this.pasteWithMiddleClick && ev.button == 1) {
				if (_internalClipboard!==undefined) readText().then(text=>term.paste(text));
			}
		}),
		
		];
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
	    terminal._loadFeature('clipboard', this);
	}
			
}

customElements.define(ClipboardFeature.is, ClipboardFeature);
