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

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedPropertyChain;
import org.semanticweb.elk.reasoner.saturation.rulesystem.Context;
import org.semanticweb.elk.reasoner.saturation.rulesystem.InferenceRule;
import org.semanticweb.elk.reasoner.saturation.rulesystem.RuleApplicationEngine;
import org.semanticweb.elk.util.collections.Multimap;

/**
 * @author Frantisek Simancik
 *
 */
public class RuleBottom<C extends ContextElClassSaturation> implements InferenceRule<C> {

	public void apply(BackwardLink<C> argument, C context,
			RuleApplicationEngine engine) {
		
		if (!context.isSatisfiable())
			engine.enqueue(argument.getTarget(), 
					new PositiveSuperClassExpression<C> (engine.owlNothing));
	}

	public void apply(PositiveSuperClassExpression<C> argument, C context,
			RuleApplicationEngine engine) {

		if (argument.getExpression() != engine.owlNothing)
			return;
		
		context.setSatisfiable(false);

		// propagate over all backward links
		final Multimap<IndexedPropertyChain, ContextElClassSaturation> backLinks = 
			context.getBackwardLinksByObjectProperty();

		if (backLinks != null) {
			for (IndexedPropertyChain relation : backLinks.keySet())
				for (Context target : backLinks.get(relation))
					engine.enqueue(target, 
							new PositiveSuperClassExpression<C> (engine.owlNothing));
		}
	}
	
}
