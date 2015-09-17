package org.semanticweb.elk.reasoner.saturation.tracing.factories;

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

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Conclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.AbstractConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.inferences.ClassInference;
import org.semanticweb.elk.reasoner.saturation.inferences.visitors.ClassInferencePremiseVisitor;
import org.semanticweb.elk.reasoner.saturation.inferences.visitors.ClassInferenceVisitor;
import org.semanticweb.elk.util.collections.ArrayHashSet;

class ProofUnwindingState<I extends Conclusion, J extends ProofUnwindingJob<I>> {

	public final static ConclusionVisitor<ProofUnwindingState<?, ?>, Void> CONCLUSION_INSERTION_VISITOR = new ConclusionInsertionVisitor();

	public final static ClassInferenceVisitor<ProofUnwindingState<?, ?>, Void> PREMISE_INSERTION_VISITOR = new ClassInferencePremiseVisitor<ProofUnwindingState<?, ?>, Void>(
			CONCLUSION_INSERTION_VISITOR);

	final J initiatorJob;

	final Set<ClassInference> processedInferences;

	final Queue<Conclusion> todoConclusions;

	final Queue<ClassInference> todoInferences;

	ProofUnwindingState(J initiatorJob) {
		this.initiatorJob = initiatorJob;
		this.processedInferences = new ArrayHashSet<ClassInference>();
		this.todoInferences = new LinkedList<ClassInference>();
		this.todoConclusions = new LinkedList<Conclusion>();
		todoConclusions.add(initiatorJob.getInput());
	}

	private static class ConclusionInsertionVisitor extends
			AbstractConclusionVisitor<ProofUnwindingState<?, ?>, Void> {

		@Override
		protected Void defaultVisit(Conclusion conclusion,
				ProofUnwindingState<?, ?> input) {
			input.todoConclusions.add(conclusion);
			return null;
		}

	}

}