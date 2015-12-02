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

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.reasoner.indexing.inferences.IndexedAxiomInference;
import org.semanticweb.elk.reasoner.indexing.inferences.IndexedDeclarationAxiomInference;
import org.semanticweb.elk.reasoner.indexing.inferences.ModifiableIndexedDeclarationAxiomInference;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedEntity;

abstract class ModifiableIndexedDeclarationAxiomInferenceImpl<A extends ElkAxiom>
		extends
			ModifiableIndexedDeclarationAxiomImpl<A>
		implements
			ModifiableIndexedDeclarationAxiomInference {

	ModifiableIndexedDeclarationAxiomInferenceImpl(A originalAxiom,
			ModifiableIndexedEntity entity) {
		super(originalAxiom, entity);
	}

	@Override
	public <O> O accept(IndexedAxiomInference.Visitor<O> visitor) {
		return accept((IndexedDeclarationAxiomInference.Visitor<O>) visitor);
	}

}
