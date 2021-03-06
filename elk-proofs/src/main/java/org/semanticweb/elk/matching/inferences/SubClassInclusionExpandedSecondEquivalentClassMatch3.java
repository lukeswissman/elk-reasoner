package org.semanticweb.elk.matching.inferences;

/*
 * #%L
 * ELK Proofs Package
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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

import org.semanticweb.elk.matching.conclusions.ConclusionMatchExpressionFactory;
import org.semanticweb.elk.matching.conclusions.SubClassInclusionComposedMatch2;
import org.semanticweb.elk.matching.conclusions.SubClassInclusionDecomposedMatch2;
import org.semanticweb.elk.matching.root.IndexedContextRootMatch;

public class SubClassInclusionExpandedSecondEquivalentClassMatch3 extends
		AbstractInferenceMatch<SubClassInclusionExpandedSecondEquivalentClassMatch2> {

	private final IndexedContextRootMatch extendedOriginMatch_;

	SubClassInclusionExpandedSecondEquivalentClassMatch3(
			SubClassInclusionExpandedSecondEquivalentClassMatch2 parent,
			SubClassInclusionComposedMatch2 firstPremiseMatch) {
		super(parent);
		this.extendedOriginMatch_ = firstPremiseMatch
				.getExtendedDestinationMatch();
		checkEquals(firstPremiseMatch, getFirstPremiseMatch(DEBUG_FACTORY));
	}

	public IndexedContextRootMatch getExtendedOriginMatch() {
		return extendedOriginMatch_;
	}

	SubClassInclusionComposedMatch2 getFirstPremiseMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getSubClassInclusionComposedMatch2(
				getParent().getFirstPremiseMatch(factory),
				getExtendedOriginMatch());
	}

	public SubClassInclusionDecomposedMatch2 getConclusionMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getSubClassInclusionDecomposedMatch2(
				getParent().getParent().getConclusionMatch(factory),
				getExtendedOriginMatch(),
				getParent().getConclusionSubsumerMatch());

	}

	@Override
	public <O> O accept(InferenceMatch.Visitor<O> visitor) {
		return visitor.visit(this);
	}

	/**
	 * The visitor pattern for instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <O>
	 *            the type of the output
	 */
	public interface Visitor<O> {

		O visit(SubClassInclusionExpandedSecondEquivalentClassMatch3 inferenceMatch3);

	}

	/**
	 * A factory for creating instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public interface Factory {

		SubClassInclusionExpandedSecondEquivalentClassMatch3 getSubClassInclusionExpandedSecondEquivalentClassMatch3(
				SubClassInclusionExpandedSecondEquivalentClassMatch2 parent,
				SubClassInclusionComposedMatch2 firstPremiseMatch);

	}

}
