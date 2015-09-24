/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation.inferences;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2013 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectSomeValuesFrom;
import org.semanticweb.elk.reasoner.saturation.IndexedContextRoot;
import org.semanticweb.elk.reasoner.saturation.conclusions.implementation.ComposedSubsumerImpl;
import org.semanticweb.elk.reasoner.saturation.conclusions.implementation.SubPropertyChainImpl;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.ComposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Propagation;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.SubPropertyChain;
import org.semanticweb.elk.reasoner.saturation.inferences.visitors.PropagationInferenceVisitor;

/**
 * Represents an inference which creates a {@link Propagation} of an
 * {@link IndexedObjectSomeValuesFrom} over a given
 * {@link IndexedObjectProperty}.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 * 
 * @author "Yevgeny Kazakov"
 */
public class GeneratedPropagation extends AbstractPropagationInference {

	public GeneratedPropagation(IndexedContextRoot inferenceRoot,
			IndexedObjectProperty superRelation,
			IndexedObjectSomeValuesFrom carry) {
		super(inferenceRoot, superRelation, carry);
	}

	@Override
	public IndexedContextRoot getInferenceRoot() {
		return getConclusionRoot();
	}

	public ComposedSubsumer getFirstPremise() {
		return new ComposedSubsumerImpl<IndexedClassExpression>(
				getInferenceRoot(), getCarry().getFiller());
	}

	public SubPropertyChain getSecondPremise() {
		return new SubPropertyChainImpl(getRelation(), getCarry().getProperty());
	}

	@Override
	public <I, O> O accept(PropagationInferenceVisitor<I, O> visitor,
			I parameter) {
		return visitor.visit(this, parameter);
	}

}
