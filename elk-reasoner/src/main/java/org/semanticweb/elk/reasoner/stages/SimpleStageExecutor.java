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
package org.semanticweb.elk.reasoner.stages;

import org.semanticweb.elk.util.concurrent.computation.SimpleInterrupter;

/**
 * A simple {@link ReasonerStageExecutor}. If a stage has not been done, first,
 * all its dependencies are executed, and then this stage itself.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
public class SimpleStageExecutor extends SimpleInterrupter implements
		ReasonerStageExecutor {

	@Override
	public void complete(ReasonerStage stage) {
		System.out.println("Running " + stage.getName());
		if (!stage.done()) {
			registerCurrentThreadToInterrupt();
			for (ReasonerStage dependentStage : stage.getDependencies()) {
				complete(dependentStage);
				if (dependentStage.isInterrupted())
					return;
			}
			System.out.println("Starting " + stage.getName());
			stage.execute();
			System.out.println("Finished " + stage.getName());
		}
	}
}
