package org.semanticweb.elk.reasoner.incremental;

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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.semanticweb.elk.loading.AxiomChangeListener;
import org.semanticweb.elk.loading.ChangesLoader;
import org.semanticweb.elk.loading.ElkLoadingException;
import org.semanticweb.elk.loading.Loader;
import org.semanticweb.elk.loading.SimpleElkAxiomChange;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.printers.OwlFunctionalStylePrinter;
import org.semanticweb.elk.owl.visitors.ElkAxiomProcessor;

/**
 * Loads sets of added and removed axioms in the reasoner. Can do so in the
 * forward way, i.e., added axioms are added and removed are removed, or the
 * reverse way to undo the changes
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class IncrementalChangeLoader implements ChangesLoader {

	public enum DIRECTION {
		FORWARD, BACKWARD
	};

	protected static final Logger LOGGER_ = Logger
			.getLogger(IncrementalChangeLoader.class);

	/**
	 * The change to be used by this {@link ChangesLoader}
	 */
	private final IncrementalChange<ElkAxiom> change_;

	/**
	 * change listener if any is needed
	 */
	private AxiomChangeListener listener_;

	private final DIRECTION dir_;

	public IncrementalChangeLoader(IncrementalChange<ElkAxiom> change,
			DIRECTION dir) {
		this.change_ = change;
		this.dir_ = dir;
	}

	@Override
	public Loader getLoader(final ElkAxiomProcessor axiomInserter,
			final ElkAxiomProcessor axiomDeleter) {

		return new Loader() {

			private final Iterator<ElkAxiom> additions_ = change_
					.getAdditions().iterator();
			private final Iterator<ElkAxiom> deletions_ = change_
					.getDeletions().iterator();

			@Override
			public void load() throws ElkLoadingException {

				while (deletions_.hasNext()) {
					if (Thread.currentThread().isInterrupted())
						break;
					ElkAxiom axiom = deletions_.next();

					switch (dir_) {
					case FORWARD:
						delete(axiom);
						break;
					case BACKWARD:
						add(axiom);
					}
				}
				while (additions_.hasNext()) {
					if (Thread.currentThread().isInterrupted())
						break;
					ElkAxiom axiom = additions_.next();

					switch (dir_) {
					case FORWARD:
						add(axiom);
						break;
					case BACKWARD:
						delete(axiom);
					}
				}
			}

			private void add(ElkAxiom axiom) {
				axiomInserter.visit(axiom);

				if (LOGGER_.isTraceEnabled())
					LOGGER_.trace("adding: "
							+ OwlFunctionalStylePrinter.toString(axiom));
				if (listener_ != null)
					listener_.notify(new SimpleElkAxiomChange(axiom, 1));
			}

			private void delete(ElkAxiom axiom) {
				axiomDeleter.visit(axiom);

				if (LOGGER_.isTraceEnabled())
					LOGGER_.trace("deleting: "
							+ OwlFunctionalStylePrinter.toString(axiom));
				if (listener_ != null)
					listener_.notify(new SimpleElkAxiomChange(axiom, 1));
			}

			@Override
			public void dispose() {
				// nothing to do
			}

		};
	}

	@Override
	public void registerChangeListener(AxiomChangeListener listener) {
		this.listener_ = listener;
	}

}
