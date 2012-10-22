/*
 * #%L
 * elk-reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Oxford University Computing Laboratory
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

import java.util.Set;

import org.semanticweb.elk.reasoner.indexing.visitors.IndexedClassExpressionVisitor;
import org.semanticweb.elk.reasoner.saturation.SaturationState;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.rules.ContextRules;
import org.semanticweb.elk.util.collections.ArrayHashSet;
import org.semanticweb.elk.util.collections.chains.AbstractChain;
import org.semanticweb.elk.util.collections.chains.Chain;
import org.semanticweb.elk.util.hashing.HashGenerator;

/**
 * Represents all occurrences of an ElkClassExpression in an ontology. To this
 * end, objects of this class keeps a number of lists to describe the
 * relationships to other (indexed) class expressions. The data structures are
 * optimized for quickly retrieving the relevant relationships during
 * inferencing.
 * 
 * This class is mainly a data container that provides direct public access to
 * its content. The task of updating index structures consistently in a global
 * sense is left to callers.
 * 
 * @author "Frantisek Simancik"
 * @author "Markus Kroetzsch"
 * @author "Yevgeny Kazakov"
 * @author Pavel Klinov
 */
abstract public class IndexedClassExpression {

	private Set<IndexedPropertyChain> posPropertiesInExistentials_;

	ContextRules compositionRules;

	/**
	 * This counts how often this object occurred positively. Some indexing
	 * operations are only needed when encountering objects positively for the
	 * first time.
	 */
	int positiveOccurrenceNo = 0;

	/**
	 * This counts how often this object occurred negatively. Some indexing
	 * operations are only needed when encountering objects negatively for the
	 * first time.
	 */
	int negativeOccurrenceNo = 0;

	/**
	 * This method should always return true apart from intermediate steps
	 * during the indexing.
	 * 
	 * @return true if the represented class expression occurs in the ontology
	 */
	public boolean occurs() {
		return positiveOccurrenceNo > 0 || negativeOccurrenceNo > 0;
	}

	/**
	 * @return {@code true} if the represented class expression occurs
	 *         negatively in the ontology
	 */
	public boolean occursNegatively() {
		return negativeOccurrenceNo > 0;
	}

	/**
	 * @return {@code true} if the represented class expression occurs
	 *         positively in the ontology
	 */
	public boolean occursPositively() {
		return positiveOccurrenceNo > 0;
	}

	/**
	 * Non-recursively. The recursion is implemented in indexing visitors.
	 */
	abstract void updateOccurrenceNumbers(IndexUpdater updater, int increment,
			int positiveIncrement, int negativeIncrement);

	
	public abstract void applyDecompositionRule(SaturationState state, Context context);

	/**
	 * @return the {@link IndexedObjectProperty} objects that occur in positive
	 *         {@link IndexedObjectSomeValuesFrom} that have this
	 *         {@link IndexedClassExpression} as the filler, or {@code null} if
	 *         none is assigned
	 */
	public Set<IndexedPropertyChain> getPosPropertiesInExistentials() {
		return posPropertiesInExistentials_;
	}


	protected boolean addPosPropertyInExistential(IndexedPropertyChain property) {
		if (posPropertiesInExistentials_ == null)
			posPropertiesInExistentials_ = new ArrayHashSet<IndexedPropertyChain>(
					1);
		return posPropertiesInExistentials_.add(property);
	}

	protected boolean removePosPropertyInExistential(
			IndexedPropertyChain property) {
		boolean success = false;
		if (posPropertiesInExistentials_ != null) {
			success = posPropertiesInExistentials_.remove(property);
			if (posPropertiesInExistentials_.isEmpty())
				posPropertiesInExistentials_ = null;
		}
		return success;
	}

	// TODO: replace pointers to contexts by a mapping

	/**
	 * /** the reference to a {@link Context} assigned to this
	 * {@link IndexedClassExpression}
	 */
	private volatile Context context_ = null;

	/**
	 * @return The corresponding context, null if none was assigned.
	 */
	public Context getContext() {
		return context_;
	}

	/**
	 * Sets the corresponding context if none was yet assigned.
	 * 
	 * @param context
	 *            the {@link Context} which will be assigned to this
	 *            {@link IndexedClassExpression}
	 * 
	 * @return {@code true} if the operation succeeded.
	 */
	public boolean setContext(Context context) {
		if (context_ != null)
			return false;
		synchronized (this) {
			if (context_ != null)
				return false;
			context_ = context;
		}
		return true;
	}

	/**
	 * Resets the corresponding context to null.
	 */
	public void resetContext() {
		if (context_ != null)
			synchronized (this) {
				context_ = null;
			}
	}

	/** Hash code for this object. */
	private final int hashCode_ = HashGenerator.generateNextHashCode();

	/**
	 * Get an integer hash code to be used for this object.
	 * 
	 * @return Hash code.
	 */
	@Override
	public final int hashCode() {
		return hashCode_;
	}

	
	Chain<ContextRules> getChainCompositionRules() {
		return new AbstractChain<ContextRules>() {
			@Override
			public ContextRules next() {
				return compositionRules;
			}

			@Override
			public void setNext(ContextRules tail) {
				compositionRules = tail;
			}
		};
	}

	public ContextRules getCompositionRules() {
		return compositionRules;
	}

	@Override
	public abstract String toString();
	
	public abstract <O> O accept(IndexedClassExpressionVisitor<O> visitor);	
}
