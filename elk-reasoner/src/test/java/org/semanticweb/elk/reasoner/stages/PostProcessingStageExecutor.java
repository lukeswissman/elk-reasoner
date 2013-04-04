/**
 * 
 */
package org.semanticweb.elk.reasoner.stages;

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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.elk.owl.exceptions.ElkException;
import org.semanticweb.elk.owl.exceptions.ElkRuntimeException;
import org.semanticweb.elk.util.collections.HashListMultimap;
import org.semanticweb.elk.util.collections.Multimap;

/**
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class PostProcessingStageExecutor extends LoggingStageExecutor {

	static final Multimap<Class<?>, Class<?>> postProcesingMap = new HashListMultimap<Class<?>, Class<?>>();

	/*
	 * STATIC INT
	 */
	static {
		// init post processing map
		postProcesingMap.add(
				PropertyHierarchyCompositionComputationStage.class,
				SaturatedPropertyChainCheckingStage.class);
		postProcesingMap.add(IncrementalDeletionStage.class,
				ContextSaturationFlagCheckingStage.class);
		postProcesingMap.add(IncrementalContextCleaningStage.class,
				CheckCleaningStage.class);
		/*postProcesingMap.add(IncrementalContextCleaningStage.class,
				SaturationGraphValidationStage.class);*/
		postProcesingMap.add(IncrementalAdditionInitializationStage.class,
				SaturationGraphValidationStage.class);
		/*
		 * this phase is commented because it uses cleaning to clean up randomly
		 * picked contexts. Cleaning never deletes anything from other contexts,
		 * which could be back-linked from the cleaned ones. However, there's no
		 * guarantee that cleaned contexts, once re-saturated, will have exactly
		 * the same (complex) subsumers as before cleaning. Therefore,
		 * non-cleaned contexts linked from the cleaned (and re-saturated) ones
		 * may end up with (complex) subsumers which seemingly have come from
		 * nowhere.
		 * 
		 * A solution would to be de-saturate randomly picked contexts, then
		 * clean all modified ones, and re-saturate. I.e. do exactly what we do
		 * in the standard chain of incremental reasoning stages.
		 */
		// postProcesingMap.add(IncrementalReSaturationStage.class,
		// RandomContextResaturationStage.class);
		/*postProcesingMap.add(IncrementalClassTaxonomyComputationStage.class,
				ValidateTaxonomyStage.class);*/
		postProcesingMap.add(IncrementalTaxonomyCleaningStage.class,
				ValidateTaxonomyStage.class);
	}

	@Override
	public void complete(ReasonerStage stage) throws ElkException {
		super.complete(stage);

		if (LOGGER_.isInfoEnabled()) {
			LOGGER_.info("Starting post processing...");
		}

		// FIXME: get rid of casts
		try {
			for (PostProcessingStage ppStage : instantiate(
					postProcesingMap.get(stage.getClass()),
					((AbstractReasonerStage) stage).reasoner)) {
				ppStage.execute();
			}
		} catch (Exception e) {
			throw new ElkRuntimeException(e);
		}

		if (LOGGER_.isInfoEnabled()) {
			LOGGER_.info("Post processing finished");
		}

	}

	private Collection<PostProcessingStage> instantiate(
			Collection<Class<?>> collection, AbstractReasonerState reasoner)
			throws Exception {
		List<PostProcessingStage> stages = new ArrayList<PostProcessingStage>(1);

		for (Class<?> stageClass : collection) {
			Constructor<?> constructor = stageClass
					.getConstructor(AbstractReasonerState.class);
			PostProcessingStage stage = (PostProcessingStage) constructor
					.newInstance(reasoner);

			stages.add(stage);
		}

		return stages;
	}

}
