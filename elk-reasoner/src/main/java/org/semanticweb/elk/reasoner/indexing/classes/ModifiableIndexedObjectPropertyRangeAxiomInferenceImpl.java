package org.semanticweb.elk.reasoner.indexing.classes;

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

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.reasoner.indexing.model.IndexedAxiomInference;
import org.semanticweb.elk.reasoner.indexing.model.IndexedObjectPropertyRangeAxiom;
import org.semanticweb.elk.reasoner.indexing.model.IndexedObjectPropertyRangeAxiomInference;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableIndexedObjectPropertyRangeAxiomInference;
import org.semanticweb.elk.reasoner.tracing.TracingInference;
import org.semanticweb.elk.reasoner.tracing.TracingInferencePrinter;

/**
 * Implements {@link ModifiableIndexedObjectPropertyRangeAxiomInference}
 * 
 * @author "Yevgeny Kazakov"
 */
abstract class ModifiableIndexedObjectPropertyRangeAxiomInferenceImpl<A extends ElkAxiom>
		extends
			ModifiableIndexedObjectPropertyRangeAxiomImpl<A>
		implements
			ModifiableIndexedObjectPropertyRangeAxiomInference {

	ModifiableIndexedObjectPropertyRangeAxiomInferenceImpl(A originalAxiom,
			ModifiableIndexedObjectProperty property,
			ModifiableIndexedClassExpression range) {
		super(originalAxiom, property, range);
	}
	
	@Override
	public IndexedObjectPropertyRangeAxiom getConclusion(
			IndexedObjectPropertyRangeAxiom.Factory factory) {
		return factory.getIndexedObjectPropertyRangeAxiom(getOriginalAxiom(),
				getProperty(), getRange());
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}
	
	@Override
	public String toString() {
		return TracingInferencePrinter.toString(this);		
	}
	
	@Override
	public final <O> O accept(TracingInference.Visitor<O> visitor) {
		return accept(
				(IndexedObjectPropertyRangeAxiomInference.Visitor<O>) visitor);
	}

	@Override
	public final <O> O accept(IndexedAxiomInference.Visitor<O> visitor) {
		return accept(
				(IndexedObjectPropertyRangeAxiomInference.Visitor<O>) visitor);
	}

}
