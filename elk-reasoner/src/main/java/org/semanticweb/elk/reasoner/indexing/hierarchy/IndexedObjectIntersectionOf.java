/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.reasoner.indexing.hierarchy;

import java.util.Map;

import org.semanticweb.elk.reasoner.indexing.visitors.IndexedClassExpressionVisitor;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedObjectIntersectionOfVisitor;
import org.semanticweb.elk.reasoner.saturation.SaturationState;
import org.semanticweb.elk.reasoner.saturation.conclusions.NegativeSuperClassExpression;
import org.semanticweb.elk.reasoner.saturation.conclusions.PositiveSuperClassExpression;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.rules.ContextRules;
import org.semanticweb.elk.util.collections.ArrayHashMap;
import org.semanticweb.elk.util.collections.LazySetIntersection;
import org.semanticweb.elk.util.collections.chains.Chain;
import org.semanticweb.elk.util.collections.chains.Matcher;
import org.semanticweb.elk.util.collections.chains.ReferenceFactory;
import org.semanticweb.elk.util.collections.chains.SimpleTypeBasedMatcher;

/**
 * Represents all occurrences of an ElkObjectIntersectionOf in an ontology.
 * 
 * @author Frantisek Simancik
 * 
 */
public class IndexedObjectIntersectionOf extends IndexedClassExpression {
	/**
	 * There are only two conjuncts. This reflects the fact that conjunctions
	 * are binarized during index construction. The conjuncts may not correspond
	 * to any ElkClassExpression in the ontology.
	 */
	private final IndexedClassExpression firstConjunct_, secondConjunct_;

	protected IndexedObjectIntersectionOf(IndexedClassExpression firstConjunct,
			IndexedClassExpression secondConjunct) {
		this.firstConjunct_ = firstConjunct;
		this.secondConjunct_ = secondConjunct; 
	}

	public IndexedClassExpression getFirstConjunct() {
		return firstConjunct_;
	}

	public IndexedClassExpression getSecondConjunct() {
		return secondConjunct_;
	}

	public <O> O accept(IndexedObjectIntersectionOfVisitor<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <O> O accept(IndexedClassExpressionVisitor<O> visitor) {
		return accept((IndexedObjectIntersectionOfVisitor<O>) visitor);
	}

	@Override
	protected void updateOccurrenceNumbers(IndexUpdater indexUpdater, int increment,
			int positiveIncrement, int negativeIncrement) {

		if (negativeOccurrenceNo == 0 && negativeIncrement > 0) {
			// first negative occurrence of this expression
			indexUpdater.add(firstConjunct_, new ThisRegistrationRule(secondConjunct_));
			indexUpdater.add(secondConjunct_, new ThisRegistrationRule(firstConjunct_));
		}

		positiveOccurrenceNo += positiveIncrement;
		negativeOccurrenceNo += negativeIncrement;

		if (negativeOccurrenceNo == 0 && negativeIncrement < 0) {
			// no negative occurrences of this conjunction left
			indexUpdater.remove(firstConjunct_, new ThisRegistrationRule(secondConjunct_));
			indexUpdater.remove(secondConjunct_, new ThisRegistrationRule(firstConjunct_));
		}

	}

	@Override
	public String toString() {
		return "ObjectIntersectionOf(" + this.firstConjunct_ + ' '
				+ this.secondConjunct_ + ')';
	}

	@Override
	public void applyDecompositionRule(SaturationState state, Context context) {
		/*RuleStatistics stats = ruleEngine.getRulesTimer();

		stats.timeObjectIntersectionOfDecompositionRule -= CachedTimeThread.currentTimeMillis;
		stats.countObjectIntersectionOfDecompositionRule++;*/

		try {
			state.produce(context, new PositiveSuperClassExpression(
					firstConjunct_));
			state.produce(context, new PositiveSuperClassExpression(
					secondConjunct_));
		} finally {
			//stats.timeObjectIntersectionOfDecompositionRule += CachedTimeThread.currentTimeMillis;
		}
	}


	/**
	 * 
	 */
	private static class ThisCompositionRule extends ContextRules {

		private final Map<IndexedClassExpression, IndexedObjectIntersectionOf> conjunctionsByConjunct_;

		ThisCompositionRule(ContextRules tail) {
			super(tail);
			this.conjunctionsByConjunct_ = new ArrayHashMap<IndexedClassExpression, IndexedObjectIntersectionOf>(
					4);
		}
		
		private boolean addConjunctionByConjunct(
				IndexedObjectIntersectionOf conjunction,
				IndexedClassExpression conjunct) {
			Object previous = conjunctionsByConjunct_.put(conjunct, conjunction);
			
			return previous == null || previous != conjunction;
		}

		private boolean removeConjunctionByConjunct(IndexedClassExpression conjunct) {
			return conjunctionsByConjunct_.remove(conjunct) != null;
		}

		/**
		 * @return {@code true} if this rule never does anything
		 */
		private boolean isEmpty() {
			return conjunctionsByConjunct_.isEmpty();
		}

		@Override
		public void apply(SaturationState state, Context context) {

			/*RuleStatistics stats = ruleEngine.getRulesTimer();

			stats.timeObjectIntersectionOfCompositionRule -= CachedTimeThread.currentTimeMillis;
			stats.countObjectIntersectionOfCompositionRule++;*/

			try {

				for (IndexedClassExpression common : new LazySetIntersection<IndexedClassExpression>(
						conjunctionsByConjunct_.keySet(),
						context.getSuperClassExpressions()))
					state.produce(context,
							new NegativeSuperClassExpression(
									conjunctionsByConjunct_.get(common)));
			} finally {
				//stats.timeObjectIntersectionOfCompositionRule += CachedTimeThread.currentTimeMillis;
			}

		}

		private static Matcher<ContextRules, ThisCompositionRule> MATCHER_ = new SimpleTypeBasedMatcher<ContextRules, ThisCompositionRule>(
				ThisCompositionRule.class);

		private static ReferenceFactory<ContextRules, ThisCompositionRule> FACTORY_ = new ReferenceFactory<ContextRules, ThisCompositionRule>() {
			@Override
			public ThisCompositionRule create(ContextRules tail) {
				return new ThisCompositionRule(tail);
			}
		};

		@Override
		public boolean addTo(Chain<ContextRules> ruleChain) {
			ThisCompositionRule rule = ruleChain.getCreate(MATCHER_, FACTORY_);
			boolean changed = false;
			
			for (Map.Entry<IndexedClassExpression, IndexedObjectIntersectionOf> entry : conjunctionsByConjunct_.entrySet()) {
				changed |= rule.addConjunctionByConjunct(entry.getValue(), entry.getKey());
			}
			
			return changed;
		}

		@Override
		public boolean removeFrom(Chain<ContextRules> ruleChain) {
			ThisCompositionRule rule = ruleChain.find(MATCHER_);
			boolean changed = false;
			
			if (rule != null) {
				for (IndexedClassExpression conjunct : conjunctionsByConjunct_.keySet()) {
					changed |= rule.removeConjunctionByConjunct(conjunct);
				}
				
				if (rule.isEmpty()) {
					ruleChain.remove(MATCHER_);
				}
			}
			
			return changed;
		}
	}
	
	/**
	 * Used only to add or remove this conjunct to an existing composition rule.
	 */
	private class ThisRegistrationRule extends ContextRules {

		private final IndexedClassExpression conjunct_;
		
		private ThisRegistrationRule(IndexedClassExpression conjunct) {
			super(null);
			conjunct_ = conjunct;
		}
		
		@Override
		public void apply(SaturationState state, Context element) {}

		@Override
		public boolean addTo(Chain<ContextRules> ruleChain) {
			return ruleChain.getCreate(ThisCompositionRule.MATCHER_, ThisCompositionRule.FACTORY_).addConjunctionByConjunct(IndexedObjectIntersectionOf.this, conjunct_);			
		}

		@Override
		public boolean removeFrom(Chain<ContextRules> ruleChain) {
			ThisCompositionRule rule = ruleChain.find(ThisCompositionRule.MATCHER_);
			boolean changed = false;
			
			if (rule != null) {
				changed = rule.removeConjunctionByConjunct(conjunct_);
				
				if (rule.isEmpty()) {
					ruleChain.remove(ThisCompositionRule.MATCHER_);
				}
			}
			
			return changed;
		}
	}
}
