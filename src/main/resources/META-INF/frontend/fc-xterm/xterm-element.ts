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
import { LitElement, html, TemplateResult } from 'lit-element';

import { Terminal, ITerminalAddon, IDisposable } from 'xterm'
import { ITerminal } from 'xterm/src/browser/Types';

type integer = number;
type KeyboardEventHandler = (event: KeyboardEvent) => void;
type KeyboardEventPredicate = (event: KeyboardEvent) => boolean;

interface CustomKey {
  key?: string;
  code?: string;
  location?: number;
  ctrlKey?: boolean;
  altKey?: boolean;
  metaKey?: boolean;
  shiftKey?: boolean;
}

interface CustomKeyEventHandler {
  predicate:KeyboardEventPredicate;
  handle?: KeyboardEventHandler;
}

interface CustomKeyEventHandlerRegistryDisposable extends IDisposable {
  id: integer;    
}

//sparse array of CustomKeyEventHandler
class CustomKeyEventHandlerRegistry {
  private handlers: CustomKeyEventHandler[] = [];
  private next:integer=0;

  register(customKey: CustomKey, handle: (event: KeyboardEvent) => void): CustomKeyEventHandlerRegistryDisposable;
  register(predicate: KeyboardEventPredicate, handle: KeyboardEventHandler) : CustomKeyEventHandlerRegistryDisposable;
  register(arg: CustomKey | KeyboardEventPredicate, handle: KeyboardEventHandler) : CustomKeyEventHandlerRegistryDisposable {    
    let predicate : KeyboardEventPredicate;
    if ((typeof arg) === 'object') {
        let customKey = arg as any;
        //(C) T.J. Crowder  cc-by-sa 4.0
        //https://stackoverflow.com/a/35430956/1297272
        let properties = ['key', 'code', 'location', 'ctrlKey', 'altKey', 'metaKey', 'shiftKey'];      
        predicate  = (ev : KeyboardEvent) => properties.every(property => customKey[property] === (ev as any)[property] || customKey[property]===undefined);
    } else {
        predicate = arg as KeyboardEventPredicate;    
    }
    
    const id = this.next++;
    if (!handle) handle = () => {};
    this.handlers[id] = {predicate, handle};
    return {id, dispose : () => delete this.handlers[id]};            
  }

  remove(id: integer) : void {
    delete this.handlers[id];
  }

  handle(context: any, ev: KeyboardEvent) : void {
    //invoke latest applicable handler for event
    let j=-1;
    for(const i in this.handlers) {
      if (/\d+/.test(i) && this.handlers[i].predicate(ev)) j=Math.max(j, parseInt(i));              
    }
    if (j>=0) {
        ev.cancelBubble=true;
        this.handlers[j].handle?.call(context, ev);        
    }
  }
}
	
export interface TerminalMixin {
  _dispatchCustomKeyEvent(detail : any) : void;
  connectedCallback(): void;  
  node: XTermElement;
}

export abstract class TerminalAddon<T extends TerminalMixin> implements ITerminalAddon {
	public $ : T;
	protected $node: XTermElement;
	protected $core: ITerminal;
	protected _disposables : IDisposable[];	
	
	public readonly activate = (terminal: Terminal): void => {
		this.$node = this.$.node;
		this.$core = (terminal as any)._core as ITerminal;
		this._disposables=[];
		this.activateCallback(terminal);
    };
	
	public readonly dispose = (): void => {
        this._disposables.forEach(d => d.dispose());
    };

	protected abstract activateCallback(terminal: Terminal) : void;
}
	
	
export class XTermElement extends LitElement implements TerminalMixin {
  
  terminal: Terminal;
  disabled: boolean = false;
  node: XTermElement;

  customKeyEventHandlers: CustomKeyEventHandlerRegistry;

  render(): TemplateResult {
    return html`
        <slot name="terminal-container" @slotchange=${this._slotchange}></slot>        
    `;
  }

  constructor() {
    super();        
    this.customKeyEventHandlers = new CustomKeyEventHandlerRegistry();
    this.terminal = new Terminal();	
	this.node = this;
  }

  connectedCallback() {
    super.connectedCallback();

    let term = this.terminal;
    term.setOption('convertEol', true);
    
    //onLineFeed doesn't distinguish lines from user input and lines from terminal.write
    //<N CSI is handled by console-feature
    term.onData(e => {
        term.write(e.replace(/\r/g,'\x1b[<N\n'));
    });
            
    //https://gist.github.com/xem/670dec8e70815842eb95
    term.setOption('bellSound','data:audio/wav;base64,UklGRl9vT19XQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YU'+Array(500).join('123'));

    term.attachCustomKeyEventHandler(ev => {
      if (ev.type!=='keydown') return false;
      
      this.customKeyEventHandlers.handle(this, ev);
      if (ev.cancelBubble) return false;
      
      if (ev.key==' ') {
          term.write(' ');
          return false;
      } else {
          return true;
      }
 
    });   
  }
 
  disconnectedCallback() {
    this.terminal.dispose();
    this.terminal = new Terminal();
    super.disconnectedCallback();
  }

  _slotchange() {
    let slot = this.shadowRoot?.querySelector("slot[name='terminal-container']") as HTMLSlotElement;
    requestAnimationFrame(()=>{
        this.terminal.open(slot.assignedNodes()[0] as HTMLElement);
        this.dispatchEvent(new CustomEvent('terminal-initialized'));
    });
  }

  updated(changedProps : any) {
    if (changedProps.has('disabled')) {
      this._disabledChanged(this.disabled);
    }
  }

  _disabledChanged(disabled: boolean) {
    this.terminal.setOption('disableStdin', disabled);
    this.terminal.write(disabled?"\x1b[?25l":"\x1b[?25h");
  }
    
  registerCustomKeyListener(customKey: CustomKey) : integer {
    let handler : KeyboardEventHandler = (ev: KeyboardEvent) => this._dispatchCustomKeyEvent(ev);
    return this.customKeyEventHandlers.register(customKey, handler).id;
  }

  _dispatchCustomKeyEvent(detail : any) {
    this.dispatchEvent(new CustomEvent('CustomKey', {detail}));
  }

}