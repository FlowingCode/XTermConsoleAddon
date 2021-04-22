import { Terminal, IDisposable } from 'xterm'
import { TerminalMixin } from '@vaadin/flow-frontend/fc-xterm/xterm-element';
import { FitAddon as FitAddonBase } from 'xterm-addon-fit/src/FitAddon';


interface FitMixin extends TerminalMixin {
	fitOnResize: boolean;	
};

class FitAddon extends FitAddonBase {
  _disposables : IDisposable[];
  $ : FitMixin;
    
  activate(terminal: Terminal): void {
  	super.activate(terminal);
           
    let _fitOnResize = () => {
        if (this.$.fitOnResize) this.fit();
    }
         
    window.addEventListener('resize', _fitOnResize);

	this._disposables = [];
    this._disposables.push({dispose : () => {
	  window.removeEventListener('resize', _fitOnResize);
    }});
  }
  
  dispose(): void {
     this._disposables.forEach(d => d.dispose());
	 super.dispose();
  }	
}

type Constructor<T = {}> = new (...args: any[]) => T;
export function XTermFitMixin<TBase extends Constructor<TerminalMixin>>(Base: TBase) {
  return class XTermFitMixin extends Base {
        
    _fitAddon? : FitAddon;
    fitOnResize: boolean = true;
    
    connectedCallback() {
      super.connectedCallback();   
      
      let addon = new FitAddon();
	  addon.$=this;
	  this.node.terminal.loadAddon(addon);
      
	  this._fitAddon = addon;
      this.fit();
    }

    fit() {
      this._fitAddon.proposeDimensions();
	  window.requestAnimationFrame(()=>{ 
	      try {
	        this._fitAddon?.fit();
	      } catch (e) {
	        console.warn(e);
	      }
	  });
    }
       
  };
}
