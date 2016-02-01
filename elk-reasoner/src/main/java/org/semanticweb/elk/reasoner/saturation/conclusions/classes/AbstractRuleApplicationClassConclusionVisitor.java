package org.semanticweb.elk.reasoner.saturation.conclusions.classes;

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

import org.semanticweb.elk.Reference;
import org.semanticweb.elk.reasoner.indexing.model.IndexedClassExpression;
import org.semanticweb.elk.reasoner.saturation.conclusions.model.ClassConclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.model.SubClassInclusion;
import org.semanticweb.elk.reasoner.saturation.context.ContextPremises;
import org.semanticweb.elk.reasoner.saturation.rules.ClassInferenceProducer;
import org.semanticweb.elk.reasoner.saturation.rules.RuleVisitor;
import org.semanticweb.elk.reasoner.saturation.rules.subsumers.LinkedSubsumerRule;
import org.semanticweb.elk.reasoner.saturation.rules.subsumers.SubsumerDecompositionVisitor;

public abstract class AbstractRuleApplicationClassConclusionVisitor extends
		AbstractClassConclusionVisitor<Boolean> implements Reference<ContextPremises> {

	/**
	 * {@link ContextPremises} to which the rules are applied
	 */
	private final Reference<? extends ContextPremises> premisesRef_;
	
	/**
	 * {@link RuleVisitor} to track rule applications
	 */
	final RuleVisitor<?> ruleAppVisitor;

	/**
	 * {@link ClassInferenceProducer} to produce the {@link ClassConclusion}s of the
	 * applied rules
	 */
	final ClassInferenceProducer producer;

	AbstractRuleApplicationClassConclusionVisitor(
			Reference<? extends ContextPremises> premisesRef,
			RuleVisitor<?> ruleAppVisitor,
			ClassInferenceProducer producer) {
		this.premisesRef_ = premisesRef;
		this.ruleAppVisitor = ruleAppVisitor;
		this.producer = producer;
	}
	
	@Override
	public ContextPremises get() {
		return premisesRef_.get();
	}
	
	void applyCompositionRules(SubClassInclusion conclusion) {
		IndexedClassExpression subsumer = conclusion.getSuperExpression();
		LinkedSubsumerRule compositionRule = subsumer.getCompositionRuleHead();
		while (compositionRule != null) {
			compositionRule.accept(ruleAppVisitor, subsumer, get(), producer);
			compositionRule = compositionRule.next();
		}
	}

	void applyDecompositionRules(SubClassInclusion conclusion) {
		IndexedClassExpression subsumer = conclusion.getSuperExpression();
		subsumer.accept(new SubsumerDecompositionVisitor(ruleAppVisitor, get(),
				producer));
	}

}