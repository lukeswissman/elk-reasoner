/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation.tracing.inferences;

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedPropertyChain;

/**
 * @author Pavel Klinov
 *
 * pavel.klinov@uni-ulm.de
 */
public class ReflexiveInference extends AbstractInference {
	//TODO need a kind of Conclusion for this
	private final IndexedPropertyChain reflexiveChain_;
	
	/**
	 * 
	 */
	public ReflexiveInference(IndexedPropertyChain chain) {
		reflexiveChain_ = chain;
	}

	public IndexedPropertyChain getChain() {
		return reflexiveChain_;
	}
	
	@Override
	public String toString() {
		return "Reflexive inference: owl:Thing => " + reflexiveChain_ + " some owl:Thing";
	}
	
	@Override
	public <R> R accept(InferenceVisitor<R> visitor) {
		return visitor.visit(this);
	}
}
