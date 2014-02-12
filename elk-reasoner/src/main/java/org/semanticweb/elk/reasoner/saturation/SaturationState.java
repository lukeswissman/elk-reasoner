/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation;

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

import java.util.Collection;

import org.semanticweb.elk.reasoner.indexing.OntologyIndex;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.saturation.conclusions.Conclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.rules.contextinit.ContextInitRuleVisitor;

/**
 * Represents the state of saturation which can be changed by applying reasoning
 * rules and processing their conclusions.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public interface SaturationState {

	/**
	 * @return the {@link Collection} of {@link Context} stored in this
	 *         {@link SaturationState}
	 */
	public Collection<? extends Context> getContexts();

	public Context getContext(IndexedClassExpression ice);

	public OntologyIndex getOntologyIndex();

	public Collection<IndexedClassExpression> getNotSaturatedContexts();

	/**
	 * Creates a new {@link ExtendedSaturationStateWriter} for modifying this
	 * {@link SaturationState} associated with the given
	 * {@link ContextCreationListener}. If {@link ContextCreationListener} is
	 * not thread safe, the calls of the methods should be synchronized
	 * 
	 * The passed {@link ContextInitRuleVisitor} is used to apply initialization
	 * rules to the newly created contexts
	 * 
	 */
	public SaturationStateWriter getExtendedWriter(
			ContextCreationListener contextCreationListener,
			ContextModificationListener contextModificationListener,
			boolean trackNewContextsAsUnsaturated);

	public SaturationStateWriter getWriter(
			ContextModificationListener contextModificationListener);

	/**
	 * @param visitor
	 *            a {@link ConclusionVisitor} which will be invoked for each
	 *            produced {@link Conclusion}
	 * 
	 * @return an {@link SaturationStateWriter} for modifying this
	 *         {@link SaturationState}. The methods of this
	 *         {@link SaturationStateWriter} are thread safe
	 */
	public SaturationStateWriter getWriter();

	public SaturationStateWriter getExtendedWriter();

}
