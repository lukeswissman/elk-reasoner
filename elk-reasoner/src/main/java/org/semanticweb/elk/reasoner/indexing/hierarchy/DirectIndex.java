/*
 * #%L
 * ELK Reasoner
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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.interfaces.ElkSubObjectPropertyExpression;
import org.semanticweb.elk.owl.predefined.PredefinedElkClass;
import org.semanticweb.elk.owl.visitors.ElkAxiomProcessor;
import org.semanticweb.elk.reasoner.indexing.OntologyIndex;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.rules.ChainableRule;
import org.semanticweb.elk.reasoner.saturation.rules.LinkRule;
import org.semanticweb.elk.util.collections.ArrayHashSet;
import org.semanticweb.elk.util.collections.Operations;
import org.semanticweb.elk.util.collections.chains.AbstractChain;
import org.semanticweb.elk.util.collections.chains.Chain;

public class DirectIndex implements OntologyIndex {

	final IndexedClass indexedOwlThing, indexedOwlNothing;

	private final IndexedObjectCache objectCache_;
	private final IndexObjectConverter elkObjectIndexer_;
	private final ElkAxiomIndexerVisitor directAxiomInserter_,
			directAxiomDeleter_;

	private ChainableRule<Context> contextInitRules_ = null;

	private final Set<IndexedObjectProperty> reflexiveObjectProperties_;

	public DirectIndex(IndexedObjectCache objectCache) {
		objectCache_ = objectCache;
		elkObjectIndexer_ = new IndexObjectConverter(objectCache, objectCache);

		// index predefined entities
		// TODO: what to do if someone tries to delete them?
		ElkAxiomIndexerVisitor tmpIndexer = new ElkAxiomIndexerVisitor(
				objectCache_, null,
				new DirectIndexUpdater<OntologyIndex>(this), true);

		this.indexedOwlThing = tmpIndexer
				.indexClassDeclaration(PredefinedElkClass.OWL_THING);
		this.indexedOwlNothing = tmpIndexer
				.indexClassDeclaration(PredefinedElkClass.OWL_NOTHING);

		this.directAxiomInserter_ = new ElkAxiomIndexerVisitor(objectCache,
				indexedOwlNothing, new DirectIndexUpdater<OntologyIndex>(this),
				true);
		this.directAxiomDeleter_ = new ElkAxiomIndexerVisitor(objectCache,
				indexedOwlNothing, new DirectIndexUpdater<OntologyIndex>(this),
				false);

		this.reflexiveObjectProperties_ = new ArrayHashSet<IndexedObjectProperty>(
				64);
	}

	public DirectIndex() {
		this(new IndexedObjectCache());
	}

	@Override
	public IndexedObjectCache getIndexedObjectCache() {
		return this.objectCache_;
	}

	@Override
	public LinkRule<Context> getContextInitRuleHead() {
		return contextInitRules_;
	}

	@Override
	public Chain<ChainableRule<Context>> getContextInitRuleChain() {
		return new AbstractChain<ChainableRule<Context>>() {

			@Override
			public ChainableRule<Context> next() {
				return contextInitRules_;
			}

			@Override
			public void setNext(ChainableRule<Context> tail) {
				contextInitRules_ = tail;
			}
		};
	}

	@Override
	public IndexedClassExpression getIndexed(ElkClassExpression representative) {
		IndexedClassExpression result = representative
				.accept(elkObjectIndexer_);
		if (result.occurs())
			return result;
		else
			return null;
	}

	@Override
	public IndexedPropertyChain getIndexed(
			ElkSubObjectPropertyExpression elkSubObjectPropertyExpression) {
		IndexedPropertyChain result = elkSubObjectPropertyExpression
				.accept(elkObjectIndexer_);
		if (result.occurs())
			return result;
		else
			return null;
	}

	@Override
	public Collection<IndexedClassExpression> getIndexedClassExpressions() {
		return objectCache_.indexedClassExpressionLookup;
	}

	@Override
	public Collection<IndexedAxiom> getIndexedAxioms() {
		return objectCache_.indexedAxiomLookup;
	}

	@Override
	public Collection<IndexedClass> getIndexedClasses() {
		return new AbstractCollection<IndexedClass>() {
			@Override
			public Iterator<IndexedClass> iterator() {
				return Operations.filter(getIndexedClassExpressions(),
						IndexedClass.class).iterator();
			}

			@Override
			public int size() {
				return objectCache_.indexedClassCount;
			}
		};
	}

	@Override
	public Collection<IndexedIndividual> getIndexedIndividuals() {
		return new AbstractCollection<IndexedIndividual>() {

			@Override
			public Iterator<IndexedIndividual> iterator() {
				return Operations.filter(getIndexedClassExpressions(),
						IndexedIndividual.class).iterator();
			}

			@Override
			public int size() {
				return objectCache_.indexedIndividualCount;
			}

		};
	}

	@Override
	public Collection<IndexedPropertyChain> getIndexedPropertyChains() {
		return objectCache_.indexedPropertyChainLookup;
	}

	@Override
	public Collection<IndexedObjectProperty> getIndexedObjectProperties() {
		return new AbstractCollection<IndexedObjectProperty>() {

			@Override
			public Iterator<IndexedObjectProperty> iterator() {
				return Operations.filter(getIndexedPropertyChains(),
						IndexedObjectProperty.class).iterator();
			}

			@Override
			public int size() {
				return objectCache_.indexedObjectPropertyCount;
			}
		};
	}

	@Override
	public Collection<IndexedObjectProperty> getReflexiveObjectProperties() {
		return Collections.unmodifiableCollection(reflexiveObjectProperties_);
	}

	@Override
	public boolean addReflexiveProperty(IndexedObjectProperty property) {
		return reflexiveObjectProperties_.add(property);

	}

	@Override
	public boolean removeReflexiveProperty(IndexedObjectProperty property) {
		return reflexiveObjectProperties_.remove(property);
	}

	@Override
	public ElkAxiomProcessor getAxiomInserter() {
		return directAxiomInserter_;
	}

	@Override
	public ElkAxiomProcessor getAxiomDeleter() {
		return directAxiomDeleter_;
	}

	@Override
	public IndexedClass getIndexedOwlThing() {
		return indexedOwlThing;
	}

	@Override
	public IndexedClass getIndexedOwlNothing() {
		return indexedOwlNothing;
	}

}