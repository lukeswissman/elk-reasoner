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
import org.semanticweb.elk.matching.conclusions.ForwardLinkMatch1;
import org.semanticweb.elk.matching.conclusions.IndexedContextRootMatch;
import org.semanticweb.elk.matching.conclusions.SubClassInclusionDecomposedMatch1;
import org.semanticweb.elk.matching.conclusions.SubClassInclusionDecomposedMatch1Watch;
import org.semanticweb.elk.owl.interfaces.ElkSubObjectPropertyExpression;
import org.semanticweb.elk.reasoner.saturation.inferences.ForwardLinkOfObjectSomeValuesFrom;

public class ForwardLinkOfObjectSomeValuesFromMatch1
		extends AbstractInferenceMatch<ForwardLinkOfObjectSomeValuesFrom>
		implements SubClassInclusionDecomposedMatch1Watch {

	private final IndexedContextRootMatch originMatch_;
	private final ElkSubObjectPropertyExpression fullChainMatch_;
	private final int chainStartPos_;

	ForwardLinkOfObjectSomeValuesFromMatch1(
			ForwardLinkOfObjectSomeValuesFrom parent,
			ForwardLinkMatch1 conclusionMatch) {
		super(parent);
		originMatch_ = conclusionMatch.getDestinationMatch();
		fullChainMatch_ = conclusionMatch.getFullChainMatch();
		chainStartPos_ = conclusionMatch.getChainStartPos();
	}

	public IndexedContextRootMatch getOriginMatch() {
		return originMatch_;
	}

	public ElkSubObjectPropertyExpression getFullChainMatch() {
		return fullChainMatch_;
	}

	public int getChainStartPos() {
		return chainStartPos_;
	}

	public ForwardLinkMatch1 getConclusionMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getForwardLinkMatch1(getParent().getConclusion(factory),
				originMatch_, fullChainMatch_, chainStartPos_);
	}

	public SubClassInclusionDecomposedMatch1 getPremiseMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getSubClassInclusionDecomposedMatch1(
				getParent().getPremise(factory), originMatch_);
	}

	@Override
	public <O> O accept(InferenceMatch.Visitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <O> O accept(
			SubClassInclusionDecomposedMatch1Watch.Visitor<O> visitor) {
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

		O visit(ForwardLinkOfObjectSomeValuesFromMatch1 inferenceMatch1);

	}

	/**
	 * A factory for creating instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public interface Factory {

		ForwardLinkOfObjectSomeValuesFromMatch1 getForwardLinkOfObjectSomeValuesFromMatch1(
				ForwardLinkOfObjectSomeValuesFrom parent,
				ForwardLinkMatch1 conclusionMatch);

	}

}