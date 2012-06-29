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
package org.semanticweb.elk.reasoner.datatypes.valuespaces.restricted;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;
import org.semanticweb.elk.reasoner.datatypes.enums.Datatype;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.ValueSpace;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.values.LiteralValue;

/**
 * Representation of any value that satisfies specified regular expression
 * 
 * @author Pospishnyi Olexandr
 */
public class PatternValueSpace implements ValueSpace {

	public Automaton automaton;
	public Datatype datatype;

	public PatternValueSpace(String regexp, Datatype datatype) {
		try {
			this.datatype = datatype;
			this.automaton = new RegExp(regexp).toAutomaton();
		} catch (Throwable th) {
		}
	}

	public Datatype getDatatype() {
		return datatype;
	}

	public ValueSpaceType getType() {
		return ValueSpaceType.PATTERN;
	}

	public boolean isEmptyInterval() {
		return automaton == null || automaton.isEmpty();
	}
	
	/**
	 * PatternValueSpace could contain
	 * - another PatternValueSpace if one is a subset of another
	 * - LengthRestrictedValueSpace that will satisfy this pattern 
	 * - LiteralValue that matches pattern
	 *
	 * @param valueSpace
	 * @return true if this value space contains {@code valueSpace}
	 * 
	 * Note: BasicOperations.subsetOf() appears to be not thread safe.
	 * Cloning initial automatons to avoid ConcurrentModificationException.
	 * Todo: synchronize this block is performance well be an issues
	 */
	public boolean contains(ValueSpace valueSpace) {
		boolean typechek = valueSpace.getDatatype().isCompatibleWith(this.datatype);
		if (typechek != true) {
			return false;
		}
		switch (valueSpace.getType()) {
			case LITERAL_VALUE:
				LiteralValue lvs = (LiteralValue) valueSpace;
				return automaton.run(lvs.value);
			case PATTERN:
				PatternValueSpace pvs = (PatternValueSpace) valueSpace;
				return BasicOperations.subsetOf(pvs.automaton.clone(), this.automaton.clone());
			case LENGTH_RESTRICTED:
				LengthRestrictedValueSpace lrvs = (LengthRestrictedValueSpace) valueSpace;
				return BasicOperations.subsetOf(lrvs.asAutomaton().clone(), this.automaton.clone());
			default:
				return false;
		}
	}
}
