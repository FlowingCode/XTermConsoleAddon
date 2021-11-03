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
import { customElement } from 'lit-element';

import { XTermElement } from '@vaadin/flow-frontend/fc-xterm/xterm-element';
import { XTermClipboardMixin } from '@vaadin/flow-frontend/fc-xterm/xterm-clipboard-mixin';
import { XTermConsoleMixin } from '@vaadin/flow-frontend/fc-xterm/xterm-console-mixin';
import { XTermFitMixin } from '@vaadin/flow-frontend/fc-xterm/xterm-fit-mixin';
import { XTermInsertFixMixin } from '@vaadin/flow-frontend/fc-xterm/xterm-insertfix-mixin';

@customElement('fc-xterm')
export class XTermComponent extends XTermInsertFixMixin(XTermClipboardMixin(XTermConsoleMixin(XTermFitMixin(XTermElement)))) {
    
}

