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
import { Terminal } from 'xterm'
import { TerminalMixin, TerminalAddon } from '@vaadin/flow-frontend/fc-xterm/xterm-element';

interface IConsoleMixin extends TerminalMixin {
	escapeEnabled: Boolean;
	insertMode: Boolean;
}

class ConsoleAddon extends TerminalAddon<IConsoleMixin> {

	get currentLine() : string {
		let inputHandler = ((this.$core) as any)._inputHandler;
		let buffer = inputHandler._bufferService.buffer;
		let range = buffer.getWrappedRangeForLine(buffer.y + buffer.ybase);
		let line = "";
		for (let i=range.first; i<=range.last; i++) {
			line += buffer.lines.get(i).translateToString();
		}
		line = line.replace(/\s+$/,"");
		return line;
	}
	
	activateCallback(terminal: Terminal): void {
		
		var inputHandler = ((this.$core) as any)._inputHandler;
		
		let scanEOL = (function() {
			let buffer = this._bufferService.buffer;
			let col = this._bufferService.buffer.lines.get(buffer.ybase+buffer.y).getTrimmedLength();
			this.cursorCharAbsolute({params:[col+1]});
		}).bind(inputHandler);
		
		let cursorForwardWrapped = (function() {
			let buffer = this._bufferService.buffer;
			if (buffer.x==this._bufferService.cols-1) {
				let next = buffer.lines.get(buffer.y+buffer.ybase+1);
				if (next && next.isWrapped) {
					this.cursorNextLine({params:1});
				}
			} else if (buffer.x<this._bufferService.buffer.lines.get(buffer.y+buffer.ybase).getTrimmedLength()) {
				this.cursorForward({params:[1]});
			}
		}).bind(inputHandler);
			
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
		}).bind(inputHandler);
		
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
					x=this._bufferService.cols-1;
				}
			}
			buffer.y=y;
			buffer.x=x;
		}).bind(inputHandler);

		let cursorEnd = (function() {
			let buffer = this._bufferService.buffer;
			let y, range = buffer.getWrappedRangeForLine(y = buffer.y + buffer.ybase);
			if (range.last!=y) this.cursorNextLine({params:[range.last-y]});
			scanEOL();
		}).bind(inputHandler);
		
		let cursorHome = (function() {
			let buffer = this._bufferService.buffer;
			let y, range = buffer.getWrappedRangeForLine(y = buffer.y + buffer.ybase);
			if (range.first!=y) {
				this.cursorPrecedingLine({params:[y-range.first]});
			} else {
				this.cursorCharAbsolute({params:[1]});
			}
		}).bind(inputHandler);
		
		let backspace = (function() {
			let buffer = this._bufferService.buffer;
			let line = buffer.lines.get(buffer.ybase+buffer.y);
			if (cursorBackwardWrapped()) deleteChar();
			if (buffer.x==this._bufferService.cols-1 && line.getTrimmedLength() == 0) {
				line.isWrapped = false;
			}
		}).bind(inputHandler);
		
		const node = this.$node;
		let linefeed = function() {
			node.dispatchEvent(new CustomEvent('line', {detail: this.currentLine}));
		}.bind(this);
		
		let eraseInLine = (function(params: any) {
			let buffer = this._bufferService.buffer;
			let x = buffer.x;
			let y = buffer.y;

			this.eraseInLine({params});
			let range = buffer.getWrappedRangeForLine(buffer.y + buffer.ybase);
			
			if (params[0] == 1 || params[0] == 2) {
				//Start of line through cursor 
				for (let i=range.first; i<y; i++) {
					buffer.y = i; 
					this.eraseInLine({params: [2]});
				}
			}
			
			if (params[0] == 0 || params[0] == 2) {
				//Cursor to end of line
				for (let i=y+1; i<=range.last; i++) {
					buffer.y = i; 
					this.eraseInLine({params: [2]});
					buffer.lines.get(buffer.ybase+buffer.y).isWrapped=false;
				}
			}

			buffer.x = x;
			buffer.y = y;
		}).bind(inputHandler);
		
		this._disposables = [
		terminal.parser.registerCsiHandler({prefix: '<', final: 'H'}, cursorHome),	
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='Home', ()=> terminal.write('\x1b[<H')),
		
		terminal.parser.registerCsiHandler({prefix: '<', final: 'E'}, cursorEnd),
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='End', ()=> terminal.write('\x1b[<E')),
		
		terminal.parser.registerCsiHandler({prefix: '<', final: 'L'}, cursorBackwardWrapped),
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='ArrowLeft', ()=> terminal.write('\x1b[<L')),
		
		terminal.parser.registerCsiHandler({prefix: '<', final: 'R'}, cursorForwardWrapped),
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='ArrowRight', ()=> terminal.write('\x1b[<R')),
		
		terminal.parser.registerCsiHandler({prefix: '<', final: 'B'}, backspace),
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='Backspace', ()=> terminal.write('\x1b[<B')),
		
		terminal.parser.registerCsiHandler({prefix: '<', final: 'D'}, deleteChar),
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='Delete', ()=> terminal.write('\x1b[<D')),
		
		terminal.parser.registerCsiHandler({prefix: '<', final: 'K'}, eraseInLine),
		
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='Insert', ev=>{
			this.$.insertMode = !this.$.insertMode;
		}),
		
		this.$node.customKeyEventHandlers.register(ev=> ev.key=='Enter', ()=>{
			terminal.write('\x1b[<N\n');
		}),
		
		this.$node.customKeyEventHandlers.register(ev=> [
			'ArrowUp',
			'ArrowDown',
			'F1', 'F2', 'F3', 'F4', 'F7', 'F8', 'F9', 'F10', 'F11',
		].includes(ev.key), ev=>{ev.preventDefault(); return false;}),
		
		this.$node.customKeyEventHandlers.register(ev=> [
			'Escape'
		].includes(ev.key), () => this.$.escapeEnabled),
		
		this.$node.customKeyEventHandlers.register(ev=> [
			'F5',
			'F6',
			'F12'
		].includes(ev.key), null),
		
		terminal.parser.registerCsiHandler({prefix: '<', final: 'N'}, linefeed)

		];
		
	}
	
}

type Constructor<T = {}> = new (...args: any[]) => T;
export function XTermConsoleMixin<TBase extends Constructor<TerminalMixin>>(Base: TBase) {
  return class XTermConsoleMixin extends Base implements IConsoleMixin {
	
	_addon? : ConsoleAddon; 
	escapeEnabled: Boolean;
	
	connectedCallback() {
		super.connectedCallback();
		
		this._addon = new ConsoleAddon();
		this._addon.$=this;
		this.node.terminal.loadAddon(this._addon);
	}
	
	get insertMode(): Boolean {
		return this.node.terminal.modes.insertMode;
	}

	set insertMode(value: Boolean) {
		if (value) {
			this.node.terminal.write('\x1b[4h\x1b[3 q');
		} else {
			this.node.terminal.write('\x1b[4l\x1b[2 q');
		}
	}

	_dispatchCustomKeyEvent(detail : any) {
		detail.currentLine = this._addon.currentLine;
		super._dispatchCustomKeyEvent(detail);
	}
	
 }
}