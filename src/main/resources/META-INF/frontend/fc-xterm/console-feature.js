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

export class LineEditorFeature extends PolymerElement {

	static get is() { return 'fc-xterm-line-editor'; }
	
	static get properties() {
		return {
			escapeEnabled: {
				type: Boolean,
				value: false
			}
		};
	}
	
	activate(terminal) {

		const self = this;
		
		let cursorPosition = (function(row,col) {
			this.cursorPosition({params:[row, col]});
		}).bind(terminal._core._inputHandler);

		let scanEOL = (function() {
			let buffer = this._bufferService.buffer;
			let col = this._bufferService.buffer.lines.get(buffer.ybase+buffer.y).getTrimmedLength();
			this.cursorCharAbsolute({params:[col+1]});
		}).bind(terminal._core._inputHandler);
		
		let cursorForwardWrapped = (function() {
			let buffer = this._bufferService.buffer;
			if (buffer.x==this._terminal.cols-1) {
				let next = buffer.lines.get(buffer.y+buffer.ybase+1);
				if (next && next.isWrapped) {
					this.cursorNextLine({params:1});
				}
			} else if (buffer.x<this._bufferService.buffer.lines.get(buffer.y+buffer.ybase).getTrimmedLength()) {
				this.cursorForward({params:[1]});
			}
		}).bind(terminal._core._inputHandler);
			
		let cursorBackwardWrapped = (function() {
			let buffer = this._bufferService.buffer;
			if (buffer.x>0) {
				this.cursorBackward({params:[1]});
				return true;
			} else if (buffer.lines.get(buffer.y+buffer.ybase).isWrapped) {	
				this.cursorPrecedingLine({params:[1]});
				scanEOL();
				return true;
			} else {
				return false;
			}
		}).bind(terminal._core._inputHandler);
		
		let deleteChar = (function() {
			let buffer = this._bufferService.buffer;
			this.deleteChars({params:[1]});
			let x = buffer.x;
			let y = buffer.y;
			let line = buffer.lines.get(buffer.ybase+buffer.y);
			let range = buffer.getWrappedRangeForLine(buffer.y + buffer.ybase)
			for (let i=buffer.y+buffer.ybase; i<range.last; i++) {
				let next = buffer.lines.get(buffer.ybase+buffer.y+1);
				line.set(line.length-1, next.get(0));
				this.cursorNextLine({params:1});
				this.deleteChars({params:[1]});
				line = next;
			}
			if (line.isWrapped && line.getTrimmedLength()==0) {
				line.isWrapped=false;
				if (y==range.last) {
					y--;
					x=this._terminal.cols-1;
				}
			}
			buffer.y=y;
			buffer.x=x;
		}).bind(terminal._core._inputHandler);

		let cursorEnd = (function() {
			let buffer = this._bufferService.buffer;
			let y, range = buffer.getWrappedRangeForLine(y = buffer.y + buffer.ybase);
			if (range.last!=y) this.cursorNextLine({params:[range.last-y]});
			scanEOL();
		}).bind(terminal._core._inputHandler);
		
		let cursorHome = (function() {
			let buffer = this._bufferService.buffer;
			let y, range = buffer.getWrappedRangeForLine(y = buffer.y + buffer.ybase);
			if (range.first!=y) {
				this.cursorPrecedingLine({params:[y-range.first]});
			} else {
				this.cursorCharAbsolute({params:[1]});
			}
		}).bind(terminal._core._inputHandler);
		
		let backspace = (function() {
			let buffer = this._bufferService.buffer;
			let line = buffer.lines.get(buffer.ybase+buffer.y);
			if (cursorBackwardWrapped()) deleteChar();
			if (buffer.x==this._terminal.cols-1 && line.getTrimmedLength() == 0) {
				line.isWrapped = false;
			}
		}).bind(terminal._core._inputHandler);
		
		let linefeed = (function() {
			let buffer = this._bufferService.buffer;
			let range = buffer.getWrappedRangeForLine(buffer.y + buffer.ybase);
			let line = "";
			for (let i=range.first; i<=range.last; i++) {
				line += buffer.lines.get(i).translateToString();
			}
			line = line.replace(/\s+$/,"");
			self.parentNode.dispatchEvent(new CustomEvent('line', {detail: line}));
		}).bind(terminal._core._inputHandler);
		
		this._disposables = [
		
		terminal.parser.addCsiHandler({prefix: '<', final: 'H'}, cursorHome),	
		terminal.addCustomKeyEventHandler(ev=> ev.key=='Home', ()=> terminal.write('\x1b[<H')),
		
		terminal.parser.addCsiHandler({prefix: '<', final: 'E'}, cursorEnd),
		terminal.addCustomKeyEventHandler(ev=> ev.key=='End', ()=> terminal.write('\x1b[<E')),
		
		terminal.parser.addCsiHandler({prefix: '<', final: 'L'}, cursorBackwardWrapped),
		terminal.addCustomKeyEventHandler(ev=> ev.key=='ArrowLeft', ()=> terminal.write('\x1b[<L')),
		
		terminal.parser.addCsiHandler({prefix: '<', final: 'R'}, cursorForwardWrapped),
		terminal.addCustomKeyEventHandler(ev=> ev.key=='ArrowRight', ()=> terminal.write('\x1b[<R')),
		
		terminal.parser.addCsiHandler({prefix: '<', final: 'B'}, backspace),
		terminal.addCustomKeyEventHandler(ev=> ev.key=='Backspace', ()=> terminal.write('\x1b[<B')),
		
		terminal.parser.addCsiHandler({prefix: '<', final: 'D'}, deleteChar),
		terminal.addCustomKeyEventHandler(ev=> ev.key=='Delete', ()=> terminal.write('\x1b[<D')),
		
		terminal.addCustomKeyEventHandler(ev=> ev.key=='Insert', ev=>{
			let ins = terminal._core._inputHandler._terminal.insertMode;
			console.log("INS");
			if (ins) {
				terminal.write('\x1b[4l\x1b[2 q');
			} else {
				terminal.write('\x1b[4h\x1b[3 q');
			}
		}),
		
		terminal.addCustomKeyEventHandler(ev=> ev.key=='Enter', ev=>{
			terminal.write('\x1b[<N\n');
		}),
		
		terminal.addCustomKeyEventHandler(ev=> [
			'ArrowUp',
			'ArrowDown',
			'F1', 'F2', 'F3', 'F4', 'F7', 'F8', 'F9', 'F10', 'F11',
		].includes(ev.key), ev=>ev.preventDefault() && false),
		
		terminal.addCustomKeyEventHandler(ev=> [
			'Escape'
		].includes(ev.key), () => this.escapeEnabled),
		
		terminal.addCustomKeyEventHandler(ev=> [
			'F5',
			'F6',
			'F12'
		].includes(ev.key), null),
		
		terminal.parser.addCsiHandler({prefix: '<', final: 'N'}, linefeed)

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
		terminal._loadFeature('console', this);
	}
	
}

customElements.define(LineEditorFeature.is, LineEditorFeature);
//https://github.com/xtermjs/xterm.js/blob/master/src/InputHandler.ts
