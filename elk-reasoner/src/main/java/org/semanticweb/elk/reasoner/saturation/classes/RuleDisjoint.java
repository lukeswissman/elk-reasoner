/*
 * #%L
 * ELK Reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.reasoner.saturation.classes;

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedDisjointnessAxiom;
import org.semanticweb.elk.reasoner.saturation.rulesystem.RuleApplicationFactory;
import org.semanticweb.elk.util.collections.LazySetIntersection;

/**
 * TODO: documentation
 * 
 * @author Frantisek Simancik
 * 
 * @param <C>
 *            the type of contexts that can be used with this inference rule
 */
public class RuleDisjoint<C extends ContextElClassSaturation> implements
		InferenceRuleSCE<C> {

	@Override
	public void applySCE(SuperClassExpression<C> argument, C context,
			RuleApplicationFactory.Engine engine) {

		IndexedClassExpression ice = argument.getExpression();

		if (ice.getDisjointClasses() != null)
			for (@SuppressWarnings("unused")
			IndexedClassExpression common : new LazySetIntersection<IndexedClassExpression>(
					ice.getDisjointClasses(),
					context.getSuperClassExpressions())) {
				engine.enqueue(context, new PositiveSuperClassExpression<C>(
						engine.getOwlNothing()));
				return;
			}

		if (ice.getDisjointnessAxioms() != null)
			for (IndexedDisjointnessAxiom disAxiom : ice
					.getDisjointnessAxioms())
				if (!context.addDisjointessAxiom(disAxiom))
					engine.enqueue(
							context,
							new PositiveSuperClassExpression<C>(engine
									.getOwlNothing()));
	}
}
