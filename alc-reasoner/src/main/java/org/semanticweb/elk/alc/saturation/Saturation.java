package org.semanticweb.elk.alc.saturation;

/*
 * #%L
 * ALC Reasoner
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.elk.alc.indexing.hierarchy.IndexedClass;
import org.semanticweb.elk.alc.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.alc.indexing.hierarchy.IndexedObjectProperty;
import org.semanticweb.elk.alc.indexing.hierarchy.OntologyIndex;
import org.semanticweb.elk.alc.saturation.conclusions.implementation.BacktrackedBackwardLinkImpl;
import org.semanticweb.elk.alc.saturation.conclusions.implementation.BackwardLinkImpl;
import org.semanticweb.elk.alc.saturation.conclusions.implementation.ClashImpl;
import org.semanticweb.elk.alc.saturation.conclusions.implementation.ComposedSubsumerImpl;
import org.semanticweb.elk.alc.saturation.conclusions.implementation.ContextInitializationImpl;
import org.semanticweb.elk.alc.saturation.conclusions.implementation.NegatedSubsumerImpl;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.BackwardLink;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.Conclusion;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.ExternalDeterministicConclusion;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.ExternalPossibleConclusion;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.LocalConclusion;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.LocalDeterministicConclusion;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.LocalPossibleConclusion;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.PropagatedConclusion;
import org.semanticweb.elk.alc.saturation.conclusions.interfaces.RetractedConclusion;
import org.semanticweb.elk.alc.saturation.conclusions.visitors.ConclusionVisitor;
import org.semanticweb.elk.alc.saturation.conclusions.visitors.LocalConclusionVisitor;
import org.semanticweb.elk.util.collections.HashSetMultimap;
import org.semanticweb.elk.util.collections.Multimap;
import org.semanticweb.elk.util.collections.Operations;
import org.semanticweb.elk.util.collections.Operations.FunctorEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Saturation {

	// logger for events
	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(Saturation.class);

	private static final ExternalDeterministicConclusion CONTEXT_INIT_ = new ContextInitializationImpl();

	/**
	 * if {@code true} will use an optimized subsumption test
	 */
	private final static boolean OPTIMIZED_SUBSUMPTION_TEST_ = true;

	/**
	 * if {@code true}, the integrity of saturation will be periodically tested
	 */
	private final static boolean CHECK_SATURATION_ = false;

	/**
	 * if {@code true}, propagations won't be generated until the corresponding
	 * backward links have been created.
	 * 
	 * WARNING:
	 * This is generally unsafe when the ontology requires backtracking because
	 * propagations can get deleted during backtracking and never come back
	 * (unless they are explicitly restored similarly to propagated subsumers).
	 * Seems like too much pain for too little gain at this point.
	 */
	public final static boolean DEFERRED_PROPAGATION_GENERATION = false;
	
	public final static boolean BUFFER_BACKWARD_LINKS = true;
	/**
	 * if {@code true}, negative propagations are generated for all sub-roles of
	 * the role so that they can be easily look-up when processing negative
	 * propagations and forward links.
	 * 
	 * If true, there could be multiple backtracked backward links generated for
	 * the same link, so some may be processed when the link has already been
	 * deleted, so that check must be disabled.
	 */
	public final static boolean GENERATE_NEGATIVE_PROPAGATIONS_FOR_SUBROLES = false;
	
	/**
	 * if {@code true}, some statistics will be printed
	 */
	private static final boolean PRINT_STATS_ = false;

	private final SaturationState saturationState_;

	private final Queue<LocalDeterministicConclusion> localDeterministicConclusions_;

	private final ConclusionProducer conclusionProducer_;

	private final ConclusionVisitor<Context, Void> ruleApplicationVisitor_;

	private final LocalConclusionVisitor<Context, Boolean> backtrackingVisitor_;

	private final LocalConclusionVisitor<Context, Boolean> revertingVisitor_;

	/**
	 * The {@link Context} that is currently processed by the saturation
	 */
	private Context activeContext_;

	/*
	 * temporary maps to buffer produced and retracted backward links to avoid
	 * the backward links being added and removed
	 */
	private final Multimap<IndexedObjectProperty, Root> producedBackwardLinks_;
	private final Multimap<IndexedObjectProperty, Root> retractedBackwardLinks_;

	private final OntologyIndex ontologyIndex_;
	
	// some statistics counters
	private final SaturationStatistics statistics_ = new SaturationStatistics();

	public Saturation(SaturationState saturationState, OntologyIndex index) {
		this.ontologyIndex_ = index;
		this.saturationState_ = saturationState;
		this.localDeterministicConclusions_ = new ArrayDeque<LocalDeterministicConclusion>(
				1024);
		this.producedBackwardLinks_ = new HashSetMultimap<IndexedObjectProperty, Root>(
				64);
		this.retractedBackwardLinks_ = new HashSetMultimap<IndexedObjectProperty, Root>(
				64);
		this.conclusionProducer_ = new ConclusionProducer() {
			@Override
			public void produce(Root root, ExternalPossibleConclusion conclusion) {
				LOGGER_.trace("{}: produced {}", root, conclusion);
				saturationState_.produce(root, conclusion);
			}

			@Override
			public void produce(Root root,
					ExternalDeterministicConclusion conclusion) {
				LOGGER_.trace("{}: produced {}", root, conclusion);
				if (BUFFER_BACKWARD_LINKS && conclusion instanceof BackwardLink) {
					BackwardLink link = (BackwardLink) conclusion;
					IndexedObjectProperty relation = link.getRelation();
					if (link instanceof RetractedConclusion) {
						if (!producedBackwardLinks_.remove(relation, root))
							retractedBackwardLinks_.add(relation, root);
					} else {
						producedBackwardLinks_.add(relation, root);
					}
					return;
				}
				saturationState_.produce(root, conclusion);
			}

			@Override
			public void produce(LocalDeterministicConclusion conclusion) {
				LOGGER_.trace("produced {}", conclusion);
				localDeterministicConclusions_.add(conclusion);
			}

			@Override
			public void produce(LocalPossibleConclusion conclusion) {
				LOGGER_.trace("produced {}", conclusion);
				saturationState_.produce(activeContext_, conclusion);
			}
		};
		this.ruleApplicationVisitor_ = new RuleApplicationVisitor(
				conclusionProducer_);
		this.backtrackingVisitor_ = new BacktrackingVisitor(conclusionProducer_);
		this.revertingVisitor_ = new RevertingVisitor(conclusionProducer_);
	}

	/**
	 * Submits the given {@link IndexedClassExpression} for checking
	 * satisfiability
	 * 
	 * @param expression
	 */
	public void submit(IndexedClassExpression expression) {
		Root root = new Root(expression);
		saturationState_.produce(root, CONTEXT_INIT_);
	}

	public boolean checkSubsumer(Context context,
			IndexedClassExpression possibleSubsumer) {
		LOGGER_.trace("{}: checking possible subsumer {}", context,
				possibleSubsumer);
		if (context.isInconsistent())
			return true;
		if (!possibleSubsumer.occursNegatively()
				&& !(possibleSubsumer instanceof IndexedClass))
			LOGGER_.error("{}: checking subsumption with {} not supported",
					context, possibleSubsumer);
		process();
		if (!context.getSubsumers().contains(possibleSubsumer))
			return false;
		/*
		 * use one of the two methods: simple tests just creates a context with
		 * the root obtained by the root of the original context and adding the
		 * negated possible subsumer; optimized test, instead, reuses the
		 * existing contexts and adds a negative subsumer for the possible
		 * subsumer
		 */
		return OPTIMIZED_SUBSUMPTION_TEST_ ? checkSubsumerOptimized(context,
				possibleSubsumer) : checkSubsumerSimple(context,
				possibleSubsumer);
	}

	private boolean checkSubsumerSimple(Context context,
			IndexedClassExpression possibleSubsumer) {
		LOGGER_.trace("{}: checking possible subsumer {}", context,
				possibleSubsumer);
		Root conjectureRoot = Root.addNegativeMember(context.getRoot(),
				possibleSubsumer);
		saturationState_.produce(conjectureRoot, CONTEXT_INIT_);
		process();
		return (saturationState_.getContext(conjectureRoot).isInconsistent());
	}

	private boolean checkSubsumerOptimized(Context context,
			IndexedClassExpression possibleSubsumer) {
		// set the active context to be used for newly produced local
		// conclusions
		activeContext_ = context;
		// we are going to re-saturate this context
		saturationState_.setNotSaturated(context);
		// backtrack everything
		for (;;) {
			LocalConclusion toBacktrack = context.popHistory();
			if (toBacktrack == null) {
				LOGGER_.trace("{}: nothing to backtrack", context);
				break;
			}
			
			LOGGER_.trace("{}: backtracking {}", context, toBacktrack);
			
			toBacktrack.accept(revertingVisitor_, context);
			
			if (!context.removeConclusion(toBacktrack)) {
				LOGGER_.error("{}: cannot backtrack {}", context, toBacktrack);
			}
			
			statistics_.removedConclusions++;
		}
		restoreValidConclusions(context);
		if (context.getSubsumers().contains(possibleSubsumer)) {
			// it was derived deterministically
			LOGGER_.trace("{}: deterministic subsumer {}", context,
					possibleSubsumer);
			return true;
		}
		/*
		 * else we will add negation of the possible subsumer as the first
		 * "nondeterministic" conclusion; if the possible subsumer will still be
		 * derived, this conclusion must be backtracked, and so, the possible
		 * subsumer will be derived deterministically
		 */
		// LocalConclusion conjecture = new ConjectureNonSubsumerImpl(
		// possibleSubsumer);
		// FIXME: or some reason, the above results in more inferences, although
		// definite conclusions should result in fewer non-deterministic steps
		LocalConclusion conjecture = new NegatedSubsumerImpl(possibleSubsumer);
		if (context.addConclusion(conjecture)) {
			statistics_.addedConclusions++;
			context.pushToHistory(conjecture);
			// start applying the rules
			conjecture.accept(ruleApplicationVisitor_, context);
			processDeterministic(context);
			process();
		} else {
			LOGGER_.error("{}: conjecture cannot be added {}", context,
					conjecture);
		}
		if (context.getSubsumers().contains(possibleSubsumer)) {
			// if (context.getComposedSubsumers().contains(possibleSubsumer))
			// LOGGER_.error("{}: subsumer {} is still not definite!",
			// context, possibleSubsumer);
			return true;
		}
		return false;
	}

	Set<IndexedClass> getSubsumersOptimized(Context context) {
		// make sure everything is processed
		process();
		Set<IndexedClass> subsumerCandidates = new HashSet<IndexedClass>(32);
		// initializing with current possible subsumers
		for (IndexedClassExpression possibleSubsumer : context
				.getComposedSubsumers()) {
			if (possibleSubsumer instanceof IndexedClass)
				subsumerCandidates.add((IndexedClass) possibleSubsumer);
		}
		
		for (;;) {
			context.setHistoryNotExplored();
			// we are going to re-saturate this context
			activeContext_ = context;
			// visitor for exploring branches
			LocalConclusionVisitor<Context, Boolean> branchExplorer = new SubsumptionExploringVisitor(
					conclusionProducer_, subsumerCandidates);
			boolean proceedNext = true;
			while (proceedNext) {
				LocalConclusion toBacktrack = context.popHistory();
				if (toBacktrack == null) {
					if (proceedNext && /*context.setNotSaturated()*/context.setHistoryExplored())
						// this means that the last removed conclusion in
						// history was deterministic or history was empty
						LOGGER_.trace("{}: history fully explored", context);
					break;
				}
				
				LOGGER_.trace("{}: backtracking {}", context, toBacktrack);
				
				proceedNext = toBacktrack.accept(branchExplorer, context);
				
				if (!context.removeConclusion(toBacktrack)) {
					LOGGER_.error("{}: cannot backtrack {}", context,
							toBacktrack);
				}
				
				statistics_.removedConclusions++;
			}
			restoreValidConclusions(context);
			// re-saturate for the new choices
			processDeterministic(context);
			process();
			if (/*context.setSaturated()*/context.setHistoryNotExplored()) {
				// this means
				// that all (relevant) non-deterministic choices have
				// been explored, so we are done
				context.setHistoryExplored();
				break;
			}
			
			LOGGER_.trace("{}: next branch explored, filtering candidates", context);
			
			// filter out subsumer candidates
			Set<IndexedClassExpression> newPossibleSubsumers = context
					.getComposedSubsumers();
			Iterator<IndexedClass> subsumerCandidatesIterator = subsumerCandidates
					.iterator();
			while (subsumerCandidatesIterator.hasNext()) {
				IndexedClass candidate = subsumerCandidatesIterator.next();
				if (!newPossibleSubsumers.contains(candidate)) {
					LOGGER_.trace(
							"{}: {} is missing in a new branch, deleting",
							context, candidate);
					subsumerCandidatesIterator.remove();
				}
			}
			/*if (context.getPossibleComposedSubsumers().isEmpty()
					&& context.getPossibleDecomposedSubsumers().isEmpty()) {
				// no choices left anymore, we are at the last branch
				break;
			}*/
		}
		// finally adding all deterministically derived subsumers
		Set<IndexedClassExpression> nonDeterministicSubsumers = context
				.getComposedSubsumers();
		for (IndexedClassExpression subsumer : context.getSubsumers()) {
			if (subsumer instanceof IndexedClass
					&& !nonDeterministicSubsumers.contains(subsumer)) {
				subsumerCandidates.add((IndexedClass) subsumer);
			}
		}
		return subsumerCandidates;
	}

	
	/**
	 * Processes all previously submitted {@link IndexedClassExpression}s for
	 * checking satisfiability
	 */
	public void process() {
		for (;;) {
			activeContext_ = saturationState_.pollActiveContext();
			if (activeContext_ == null) {
				activeContext_ = saturationState_.pollPossibleContext();
				if (activeContext_ == null)
					break;
				process(activeContext_);
				continue;
			}
			processDeterministic(activeContext_);
		}
		// setting all contexts as saturated
		for (;;) {
			Context context = saturationState_.takeAndSetSaturated();
			if (context == null)
				break;
		}
		if (CHECK_SATURATION_)
			saturationState_.checkSaturation();
	}

	public SaturationStatistics getStatistics() {
		return statistics_;
	}

	private void produceBufferedBackwardLinks(Context context) {
		LOGGER_.trace("{}: producing buffered backward links (direct and backtracked)", context);
		
		Root sourceRoot = context.getRoot();
		for (IndexedObjectProperty relation : retractedBackwardLinks_.keySet()) {
			for (Root root : retractedBackwardLinks_.get(relation)) {
				saturationState_.produce(root, new BacktrackedBackwardLinkImpl(sourceRoot, relation));
				context.removePropagatedConclusions(root);
			}
		}
		for (IndexedObjectProperty relation : producedBackwardLinks_.keySet()) {
			for (Root root : producedBackwardLinks_.get(relation)) {
				saturationState_.produce(root, new BackwardLinkImpl(sourceRoot, relation));
			}
		}
		retractedBackwardLinks_.clear();
		producedBackwardLinks_.clear();
	}

	private void processDeterministic(Context context) {
		for (;;) {
			Conclusion conclusion = localDeterministicConclusions_.poll();
			if (conclusion == null) {
				conclusion = context.takeToDo();
				if (conclusion == null) {
					break;
				}
			}
			process(context, conclusion);
		}
		
		if (BUFFER_BACKWARD_LINKS) {
			produceBufferedBackwardLinks(context);
		}
	}

	private void process(Context context) {
		for (;;) {
			Conclusion conclusion = localDeterministicConclusions_.poll();
			if (conclusion == null) {
				conclusion = context.takeToDo();
				if (conclusion == null) {
					conclusion = context.takeToGuess();
					if (conclusion == null)
						break;
				}
			}
			process(context, conclusion);
		}
		
		if (BUFFER_BACKWARD_LINKS) {
			produceBufferedBackwardLinks(context);
		}
	}

	private boolean isNotRelevant(PropagatedConclusion conclusion, Context context) {
		IndexedObjectProperty relation = conclusion.getRelation();
		Root sourceRoot = conclusion.getSourceRoot();
		
		if (!context.getForwardLinks().get(relation).contains(sourceRoot.getPositiveMember())) {
			return true;
		}
		// TODO: can't we instead just check for the presence of the backward link in the source context?
		Set<IndexedClassExpression> negativeMembers = sourceRoot.getNegativeMembers();
		
		if (!negativeMembers.isEmpty()) {
			// the stored negative propagations may have one of the super-roles of the given conclusion
			Collection<IndexedClassExpression> negativeMembersInPropagations = getFillersInNegativePropagations(context, relation);
			// TODO this can be pretty slow
			return !negativeMembersInPropagations.containsAll(negativeMembers);
		}
		
		return false;
	}
	
	private void process(Context context, Conclusion conclusion) {
		LOGGER_.trace("{}: processing {}", context, conclusion);
		if (conclusion instanceof RetractedConclusion) {
			/*if (context.removeConclusion(conclusion)) {
				statistics_.removedConclusions++;	
			}*/
			
			if (!context.removeConclusion(conclusion))
				LOGGER_.error("{}: retracted conclusion not found: {}!", context, conclusion);
				
			statistics_.removedConclusions++;
			
			return;
		}
		if (conclusion instanceof PropagatedConclusion) {
			// check if the conclusion is still relevant
			if (isNotRelevant((PropagatedConclusion) conclusion, context)) {
				LOGGER_.trace("{}: conclusion not relevant {}", context,
						conclusion);
				return;
			}
		}
		if (!context.addConclusion(conclusion))
			return;

		statistics_.addedConclusions++;

		if (PRINT_STATS_) {
			if (conclusion == ClashImpl.getInstance()
					&& context.isInconsistent()) {
				statistics_.inconsistentRoots++;
				if ((statistics_.inconsistentRoots / 1000) * 1000 == statistics_.inconsistentRoots)
					LOGGER_.info("{} inconsistent roots",
							statistics_.inconsistentRoots);
			}
		}

		if (conclusion instanceof LocalConclusion
				&& !context.isInconsistent()
				&& (!context.isDeterministic() || /*context.isSaturated()*/!context.isHistoryExplored() || conclusion instanceof LocalPossibleConclusion)) {
			context.pushToHistory((LocalConclusion) conclusion);
		}

		conclusion.accept(ruleApplicationVisitor_, context);

		if (context.hasClash()) {
			localDeterministicConclusions_.clear();
			boolean proceedNext = true;
			while (proceedNext) {
				LocalConclusion toBacktrack = context.popHistory();
				if (toBacktrack == null) {
					LOGGER_.trace("{}: nothing to backtrack", context.getRoot());
					if (proceedNext && /*context.setNotSaturated()*/context.setHistoryExplored())
						// this means that the last removed conclusion in
						// history was deterministic or history was empty
						LOGGER_.trace("{}: history fully explored", context);
					break;
				}

				proceedNext = toBacktrack.accept(backtrackingVisitor_, context);

				if (!context.removeConclusion(toBacktrack))
					LOGGER_.error("{}: cannot backtrack {}", context,
							toBacktrack);
				LOGGER_.trace("{}: backtracking {}", context, toBacktrack);

				statistics_.removedConclusions++;
			}

			restoreValidConclusions(context);
		}

	}

	/**
	 * Restores (propagated) conclusions that are still valid. This is needed
	 * after every backtracking step.
	 * 
	 * @param context
	 */
	void restoreValidConclusions(Context context) {
		if (!context.getInconsistentSuccessors().isEmpty()) {
			conclusionProducer_.produce(ClashImpl.getInstance());
		}
		for (Root root : context.getPropagatedComposedSubsumers().keySet()) {
			for (IndexedClassExpression subsumer : context
					.getPropagatedComposedSubsumers().get(root))
				conclusionProducer_.produce(new ComposedSubsumerImpl(subsumer));
		}
	}

	public Collection<IndexedClass> getAtomicSubsumers(
			IndexedClassExpression rootClass) {
		Context rootContext = saturationState_.getContext(rootClass);

		if (rootContext.getSaturatedContext() != null) {
			// everything has been computed and is up-to-date
			return rootContext.getSaturatedContext().getAtomicSubsumers();
		}
		
		Set<IndexedClass> subsumers = null;
		
		if (rootContext.isDeterministic()) {
			// a shortcut when the context is deterministic
			rootContext.setSaturatedContext(new SaturatedContext(Operations.map(rootContext.getSubsumers(), new FunctorEx<IndexedClassExpression, IndexedClass>(){

				@Override
				public IndexedClass apply(IndexedClassExpression element) {
					if (element instanceof IndexedClass) {
						return (IndexedClass) element;
					}
					
					return null;
				}

				@Override
				public IndexedClassExpression deapply(Object element) {
					if (element instanceof IndexedClass) {
						return (IndexedClass) element;
					}
					
					return null;
				}
				
			})));
			
			return rootContext.getSaturatedContext().getAtomicSubsumers();
		}
		
		if (rootContext.isInconsistent()) {
			LOGGER_.trace("{} is unsatisfiable", rootClass);
			
			subsumers = Collections.singleton(ontologyIndex_.getIndexedOwlNothing());
		}
		else {
			LOGGER_.trace("Started computing subsumers for {}", rootClass);
		
			subsumers = getSubsumersOptimized(rootContext);
		}
		
		rootContext.setSaturatedContext(new SaturatedContext(subsumers));
		
		return subsumers;

	}

	// returns the collection of fillers of all negative propagations for
	// super-roles of the given relation
	static Collection<IndexedClassExpression> getFillersInNegativePropagations(final Context context, IndexedObjectProperty relation) {
		if (GENERATE_NEGATIVE_PROPAGATIONS_FOR_SUBROLES) {
			return context.getNegativePropagations().get(relation);
		}

		final Set<IndexedObjectProperty> superProperties = relation.getSaturatedProperty().getSuperProperties();

		return new AbstractCollection<IndexedClassExpression>() {

			@Override
			public Iterator<IndexedClassExpression> iterator() {
				return new Iterator<IndexedClassExpression>() {

					private final Iterator<IndexedObjectProperty> superPropertiesIterator_ = superProperties.iterator();
					private IndexedObjectProperty currentProperty_ = null;
					private Iterator<IndexedClassExpression> currentNegativeMembers_ = null;
					private IndexedClassExpression current_ = null;

					@Override
					public boolean hasNext() {
						for (;;) {
							if (current_ != null) {
								return true;
							}

							if (currentNegativeMembers_ != null && currentNegativeMembers_.hasNext()) {
								current_ = currentNegativeMembers_.next();
								return true;
							}
							// move on the properties iterator
							if (superPropertiesIterator_.hasNext()) {
								currentProperty_ = superPropertiesIterator_.next();
								currentNegativeMembers_ = context.getNegativePropagations().get(currentProperty_).iterator();
							} else {
								return false;
							}
						}
					}

					@Override
					public IndexedClassExpression next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}

						IndexedClassExpression toReturn = current_;

						current_ = null;

						return toReturn;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

			@Override
			public int size() {
				// lower approximation
				return Math.min(superProperties.size(), context.getNegativePropagations().keySet().size());
			}

			@Override
			public boolean contains(Object o) {
				if (context.getNegativePropagations().isEmpty()) {
					return false;
				}

				for (IndexedObjectProperty property : superProperties) {
					if (context.getNegativePropagations().get(property).contains(o)) {
						return true;
					}
				}

				return false;
			}

		};
	}		
}
