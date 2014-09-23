/**
 * 
 */
package org.semanticweb.elk.proofs.inferences.mapping;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.elk.owl.implementation.ElkObjectFactoryImpl;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.interfaces.ElkClassExpression;
import org.semanticweb.elk.owl.interfaces.ElkObjectFactory;
import org.semanticweb.elk.owl.interfaces.ElkObjectProperty;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyChain;
import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedBinaryPropertyChain;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClass;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedDataHasValue;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedIndividual;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectComplementOf;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectIntersectionOf;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectProperty;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectSomeValuesFrom;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectUnionOf;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedClassExpressionVisitor;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedPropertyChainVisitor;

/**
 * @author Pavel Klinov
 *
 * pavel.klinov@uni-ulm.de
 */
public class Deindexer implements IndexedClassExpressionVisitor<ElkClassExpression> {

	private final static ElkObjectFactory factory_ = new ElkObjectFactoryImpl();
	
	public static ElkClassExpression deindex(IndexedClassExpression ice) {
		return ice.accept(new Deindexer());
	}
	
	public static ElkObjectProperty deindex(IndexedObjectProperty ip) {
		return ip.getElkObjectProperty();
	}
	
	public static ElkObjectPropertyChain deindex(IndexedBinaryPropertyChain ipc) {
		final List<ElkObjectPropertyExpression> properties = new LinkedList<ElkObjectPropertyExpression>();
		
		while (ipc != null) {
			properties.add(deindex(ipc.getLeftProperty()));
			
			ipc = ipc.getRightProperty().accept(new IndexedPropertyChainVisitor<IndexedBinaryPropertyChain>() {

				@Override
				public IndexedBinaryPropertyChain visit(IndexedObjectProperty element) {
					properties.add(element.getElkObjectProperty());
					return null;
				}

				@Override
				public IndexedBinaryPropertyChain visit(IndexedBinaryPropertyChain element) {
					return element;
				}
			});
		}
		
		return factory_.getObjectPropertyChain(properties);
	}
	
	private List<? extends ElkClassExpression> deindex(
			Set<IndexedClassExpression> expressions) {
		List<ElkClassExpression> deindexed = new ArrayList<ElkClassExpression>(expressions.size());
		
		for (IndexedClassExpression ice : expressions) {
			deindexed.add(deindex(ice));
		}
		
		return deindexed;
	}
	
	@Override
	public ElkClass visit(IndexedClass element) {
		return element.getElkClass();
	}

	@Override
	public ElkClassExpression visit(IndexedIndividual element) {
		return factory_.getObjectOneOf(element.getElkNamedIndividual());
	}

	@Override
	public ElkClassExpression visit(IndexedObjectComplementOf element) {
		return factory_.getObjectComplementOf(element.getNegated().accept(this));
	}

	@Override
	public ElkClassExpression visit(IndexedObjectIntersectionOf element) {
		return factory_.getObjectIntersectionOf(element.getFirstConjunct().accept(this), element.getSecondConjunct().accept(this));
	}

	@Override
	public ElkClassExpression visit(IndexedObjectSomeValuesFrom element) {
		return factory_.getObjectSomeValuesFrom(deindex(element.getRelation()), element.getFiller().accept(this));
	}

	@Override
	public ElkClassExpression visit(IndexedObjectUnionOf element) {
		return factory_.getObjectUnionOf(deindex(element.getDisjuncts()));
	}

	@Override
	public ElkClassExpression visit(IndexedDataHasValue element) {
		return factory_.getDataHasValue(element.getRelation(), element.getFiller());
	}

}