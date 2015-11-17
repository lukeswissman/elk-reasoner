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

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedContextRoot;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectSomeValuesFrom;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedPropertyChain;
import org.semanticweb.elk.reasoner.saturation.conclusions.classes.AbstractClassConclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.model.ClassConclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.model.ForwardLink;
import org.semanticweb.elk.reasoner.saturation.conclusions.model.SubClassInclusionDecomposed;

/**
 * A {@link ForwardLink} obtained from a {@link SubClassInclusionDecomposed}
 * with {@link IndexedObjectSomeValuesFrom} super-class. 
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public class ForwardLinkOfObjectSomeValuesFrom extends AbstractClassConclusion
		implements
			ForwardLinkInference {

	private final IndexedObjectSomeValuesFrom existential_;

	public ForwardLinkOfObjectSomeValuesFrom(IndexedContextRoot inferenceRoot,
			IndexedObjectSomeValuesFrom subsumer) {
		super(inferenceRoot);
		existential_ = subsumer;
	}

	public IndexedObjectSomeValuesFrom getDecomposedExistential() {
		return this.existential_;
	}

	@Override
	public IndexedPropertyChain getForwardChain() {
		return existential_.getProperty();
	}

	@Override
	public IndexedContextRoot getTarget() {
		return IndexedObjectSomeValuesFrom.Helper.getTarget(existential_);
	}

	public SubClassInclusionDecomposed getPremise(SubClassInclusionDecomposed.Factory factory) {
		return factory.getDecomposedSubClassInclusion(getInferenceRoot(), existential_);
	}

	@Override
	public IndexedContextRoot getInferenceRoot() {
		return getConclusionRoot();
	}

	@Override
	public String toString() {
		return super.toString() + " (decomposition)";
	}

	@Override
	public <O> O accept(ClassConclusion.Visitor<O> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public <O> O accept(ForwardLink.Visitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <O> O accept(SaturationInference.Visitor<O> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public <O> O accept(ClassInference.Visitor<O> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public <O> O accept(ForwardLinkInference.Visitor<O> visitor) {
		return visitor.visit(this);
	}
	
	/**
	 * Visitor pattern for instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public static interface Visitor<O> {
		
		public O visit(ForwardLinkOfObjectSomeValuesFrom inference);
		
	}

}
