package org.semanticweb.elk.reasoner.indexing.implementation;

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

import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyDomainAxiom;
import org.semanticweb.elk.reasoner.indexing.inferences.IndexedSubClassOfAxiomInferenceVisitor;
import org.semanticweb.elk.reasoner.indexing.inferences.ModifiableElkObjectPropertyDomainAxiomConversion;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClassExpression;

/**
 * Implements {@link ModifiableElkObjectPropertyDomainAxiomConversion}
 * 
 * @author "Yevgeny Kazakov"
 */
class ModifiableElkObjectPropertyDomainAxiomConversionImpl
		extends
			ModifiableIndexedSubClassOfAxiomInferenceImpl<ElkObjectPropertyDomainAxiom>
		implements
			ModifiableElkObjectPropertyDomainAxiomConversion {

	ModifiableElkObjectPropertyDomainAxiomConversionImpl(
			ElkObjectPropertyDomainAxiom originalAxiom,
			ModifiableIndexedClassExpression subClass,
			ModifiableIndexedClassExpression superClass) {
		super(originalAxiom, subClass, superClass);
	}

	@Override
	public <I, O> O accept(IndexedSubClassOfAxiomInferenceVisitor<I, O> visitor,
			I input) {
		return visitor.visit(this, input);
	}

}