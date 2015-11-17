/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation.properties.inferences;

/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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
import org.semanticweb.elk.reasoner.indexing.factories.IndexedSubObjectPropertyOfAxiomFactory;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedPropertyChain;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedSubObjectPropertyOfAxiom;
import org.semanticweb.elk.reasoner.saturation.conclusions.model.SubPropertyChain;
import org.semanticweb.elk.util.hashing.HashGenerator;

/**
 * A {@link SubPropertyChain} obtained from a {@link SubPropertyChain} by
 * unfolding a super-property under an {@link IndexedSubObjectPropertyOfAxiom}.
 * 
 * @author Pavel Klinov
 *
 *         pavel.klinov@uni-ulm.de
 * @author "Yevgeny Kazakov"
 */
public class SubPropertyChainExpandedSubObjectPropertyOf extends AbstractSubPropertyChainInference {

	/**
	 * The inferred sub-property of the super-chain for which the inference is
	 * performed by unfolding under told sub-chain of this property
	 */
	private final IndexedObjectProperty middleChain_;

	/**
	 * The {@link ElkAxiom} responsible for the told sub-chain of the premise
	 */
	private final ElkAxiom reason_;

	public SubPropertyChainExpandedSubObjectPropertyOf(IndexedPropertyChain firstChain,
			IndexedObjectProperty secondChain,
			IndexedObjectProperty thirdChain, ElkAxiom reason) {
		super(firstChain, thirdChain);
		this.middleChain_ = secondChain;
		this.reason_ = reason;
	}

	public IndexedObjectProperty getToldSuperProperty() {
		return middleChain_;
	}

	public ElkAxiom getReason() {
		return this.reason_;
	}

	public SubPropertyChain getPremise(SubPropertyChain.Factory factory) {
		return factory.getSubPropertyChain(middleChain_, getSuperChain());
	}
	
	public IndexedSubObjectPropertyOfAxiom getSideCondition(
			IndexedSubObjectPropertyOfAxiomFactory factory) {
		return factory.getIndexedSubObjectPropertyOfAxiom(reason_,
				getSubChain(), middleChain_);
	}

	@Override
	public String toString() {
		return "Expanded sub-chain: " + getSubChain() + " => "
				+ getSuperChain() + ", premise: " + middleChain_ + " => "
				+ getSuperChain() + ", reason: " + reason_;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof SubPropertyChainExpandedSubObjectPropertyOf)) {
			return false;
		}

		SubPropertyChainExpandedSubObjectPropertyOf inf = (SubPropertyChainExpandedSubObjectPropertyOf) obj;

		return middleChain_.equals(inf.middleChain_)
				&& getSubChain().equals(inf.getSubChain())
				&& getSuperChain().equals(inf.getSuperChain());
	}

	@Override
	public int hashCode() {
		return HashGenerator.combineListHash(middleChain_.hashCode(),
				getSubChain().hashCode(), getSuperChain().hashCode());
	}

	@Override
	public <O> O accept(SubPropertyChainInference.Visitor<O> visitor) {
		return visitor.visit(this);
	}
	
	
	/**
	 * Visitor pattern for instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public static interface Visitor<O> {
		
		public O visit(SubPropertyChainExpandedSubObjectPropertyOf inference);
		
	}
	
}
