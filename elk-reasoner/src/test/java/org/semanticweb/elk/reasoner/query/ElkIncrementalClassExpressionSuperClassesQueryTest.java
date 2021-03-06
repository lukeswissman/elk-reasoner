/*
 * #%L
 * ELK OWL API Binding
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
package org.semanticweb.elk.reasoner.query;

import java.util.Arrays;
import java.util.Set;

import org.junit.runner.RunWith;
import org.semanticweb.elk.ElkTestUtils;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.reasoner.incremental.ElkIncrementalReasoningTestDelegate;
import org.semanticweb.elk.reasoner.taxonomy.ElkClassKeyProvider;
import org.semanticweb.elk.reasoner.taxonomy.model.Node;
import org.semanticweb.elk.testing.PolySuite;
import org.semanticweb.elk.testing.TestManifest;
import org.semanticweb.elk.testing.TestUtils;

@RunWith(PolySuite.class)
public class ElkIncrementalClassExpressionSuperClassesQueryTest extends
		ElkIncrementalClassExpressionQueryTest<RelatedEntitiesTestOutput<ElkClass>> {

	// @formatter:off
	static final String[] IGNORE_LIST = {
			ElkTestUtils.TEST_INPUT_LOCATION + "/query/class/Disjunctions.owl",// Disjuctions not supported
			ElkTestUtils.TEST_INPUT_LOCATION + "/query/class/OneOf.owl",// Disjuctions not supported
		};
	// @formatter:on

	static {
		Arrays.sort(IGNORE_LIST);
	}

	@Override
	protected boolean ignore(final QueryTestInput<ElkClassExpression> input) {
		return super.ignore(input) || TestUtils.ignore(input,
				ElkTestUtils.TEST_INPUT_LOCATION, IGNORE_LIST);
	}

	public ElkIncrementalClassExpressionSuperClassesQueryTest(
			final TestManifest<QueryTestInput<ElkClassExpression>> manifest) {
		super(manifest,
				new ElkIncrementalReasoningTestDelegate<RelatedEntitiesTestOutput<ElkClass>>(
						manifest) {

					@Override
					public RelatedEntitiesTestOutput<ElkClass> getExpectedOutput()
							throws Exception {
						final Set<? extends Node<ElkClass>> subNodes = getStandardReasoner()
								.getSuperClassesQuietly(
										manifest.getInput().getQuery(), true);
						return new ElkRelatedEntitiesTestOutput<ElkClass>(
								subNodes, ElkClassKeyProvider.INSTANCE);
					}

					@Override
					public RelatedEntitiesTestOutput<ElkClass> getActualOutput()
							throws Exception {
						final Set<? extends Node<ElkClass>> subNodes = getIncrementalReasoner()
								.getSuperClassesQuietly(
										manifest.getInput().getQuery(), true);
						return new ElkRelatedEntitiesTestOutput<ElkClass>(
								subNodes, ElkClassKeyProvider.INSTANCE);
					}

				});
	}

}
