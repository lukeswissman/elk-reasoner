package org.semanticweb.elk.reasoner.tracing;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2015 Department of Computer Science, University of Oxford
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

/**
 * An object using which {@link Inference}s can be produced
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public interface InferenceProducer<I extends Inference> {

	/**
	 * Notifies about a new {@link Inference}.
	 * 
	 * @param inference
	 */
	public void produce(I inference);

	public static InferenceProducer<Inference> DUMMY = new InferenceProducer<Inference>() {
		@Override
		public void produce(Inference inference) {
			// no-op
		}
	};
	
}