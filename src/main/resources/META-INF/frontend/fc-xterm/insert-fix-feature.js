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

export class InsertFixFeature {

	activate(terminal) {
		let term = terminal;
		
		let printHandler = term._core._inputHandler._parser._printHandler;
		term._core._inputHandler._parser.setPrintHandler((function(data, start, end) {
			if (this._terminal.insertMode && this._terminal._coreService.decPrivateModes.wraparound && !this._optionsService.options.windowsMode) {
				const buffer = this._bufferService.buffer;
				const bufferRow = buffer.lines.get(buffer.y + buffer.ybase);
				
				const printedLength = end-start;
				let  trimmedLength = bufferRow.getTrimmedLength();
				
				if (trimmedLength+printedLength > bufferRow.length) {
					let range = buffer.getWrappedRangeForLine(buffer.y + buffer.ybase)
					range.first = buffer.y + buffer.ybase;
					
					let src;
					if (range.first==range.last) {
						src = bufferRow;
					} else {
						src = buffer.lines.get(range.last);
						trimmedLength = src.getTrimmedLength();
					}
					if (trimmedLength+printedLength > src.length) {
						if (range.last == buffer._rows - 1) {
							this._terminal.scroll();
						}
						const dst = buffer.lines.get(range.last+1);
						dst.isWrapped = true;
						dst.copyCellsFrom(src, trimmedLength-printedLength, 0, printedLength);
						this._dirtyRowService.markDirty(buffer.y+1);
					}
					
					for (let y=range.last;y>range.first;y--) {
						let dst = src;
						src= buffer.lines.get(y-1);
						dst.insertCells(0, printedLength, this._terminal.buffer.getNullCell());
						dst.copyCellsFrom(src, buffer._cols-printedLength, 0, printedLength);
						this._dirtyRowService.markDirty(y);
					}
					
				}
			} 
			
			printHandler(data, start, end);
		
		}).bind(terminal._core._inputHandler));

		let _disposables = [
		{dispose : () => {term._core._inputHandler._parser._printHandler = printHandler;}},
		
		];
		
		this.dispose = () => {
			_disposables.forEach(d => d.dispose());
			_disposables.length = 0;
		}
		
	}
	
}
