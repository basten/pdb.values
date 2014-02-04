/*******************************************************************************
* Copyright (c) 2009, 2012 Centrum Wiskunde en Informatica (CWI)
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Arnold Lankamp - interfaces and implementation
*    Anya Helene Bagge - labels
*******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.persistent;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.util.TransientMap;
import org.eclipse.imp.pdb.facts.util.TrieMap;

// TODO Add checking.
/**
 * Implementation of IMapWriter.
 * 
 * @author Arnold Lankamp
 */
/*package*/ class TemporaryMapWriter1 implements IMapWriter{
	protected Type keyType;
	protected Type valueType;
	protected Type mapType;
	
	protected final TransientMap<IValue,IValue> mapContent;
	
	protected IMap constructedMap;
	protected final boolean inferred;
	
	/*package*/ TemporaryMapWriter1() {
		super();
				
		this.mapType = null;
		this.keyType = TypeFactory.getInstance().voidType();
		this.valueType =  TypeFactory.getInstance().voidType();
		this.inferred = true;
		
		mapContent = TrieMap.transientOf();
		
		constructedMap = null;
	}
	
	/*package*/ TemporaryMapWriter1(Type mapType) {
		super();
		
		/*
		 * TODO: msteindorfer: review this (legacy?) code snippet. I don't know
		 * exactly about its semantics.
		 */
		if(mapType.isFixedWidth() && mapType.getArity() >= 2) {
			mapType = TypeFactory.getInstance().mapTypeFromTuple(mapType);
		}
		
		this.mapType = mapType;
		this.keyType = mapType.getKeyType();
		this.valueType = mapType.getValueType();
		this.inferred = false;
		
		mapContent = TrieMap.transientOf();
		
		constructedMap = null;
	}
	
	@Override
	public void put(IValue key, IValue value){
		checkMutation();
		updateTypes(key,value);
		
		mapContent.__put(key, value);
	}
	
	private void updateTypes(IValue key, IValue value) {
		if (inferred) {
			keyType = keyType.lub(key.getType());
			valueType = valueType.lub(value.getType());
		}
	}

	@Override
	public void putAll(IMap map){
		checkMutation();
		
		Iterator<Entry<IValue, IValue>> entryIterator = map.entryIterator();
		while(entryIterator.hasNext()){
			Entry<IValue, IValue> entry = entryIterator.next();
			IValue key = entry.getKey();
			IValue value = entry.getValue();
			
			mapContent.__put(key, value);			
		}
	}
	
	@Override
	public void putAll(java.util.Map<IValue, IValue> map){
		checkMutation();
		
		Iterator<Entry<IValue, IValue>> entryIterator = map.entrySet().iterator();
		while(entryIterator.hasNext()){
			Entry<IValue, IValue> entry = entryIterator.next();
			IValue key = entry.getKey();
			IValue value = entry.getValue();
			
			mapContent.__put(key, value);			
		}
	}
	
	@Override
	public void insert(IValue... values){
		checkMutation();
		
		for(int i = values.length - 1; i >= 0; i--){
			IValue value = values[i];
			
			if(!(value instanceof ITuple)) throw new IllegalArgumentException("Argument must be of ITuple type.");
			
			ITuple tuple = (ITuple) value;
			
			if(tuple.arity() != 2) throw new IllegalArgumentException("Tuple must have an arity of 2.");
			
			IValue key = tuple.get(0);
			IValue value2 = tuple.get(1);
			updateTypes(key,value2);
			mapContent.__put(key, value2);
		}
	}
	
	@Override
	public void insertAll(Iterable<? extends IValue> collection){
		checkMutation();
		
		Iterator<? extends IValue> collectionIterator = collection.iterator();
		while(collectionIterator.hasNext()){
			IValue value = collectionIterator.next();
			
			if(!(value instanceof ITuple)) throw new IllegalArgumentException("Argument must be of ITuple type.");
			
			ITuple tuple = (ITuple) value;
			
			if(tuple.arity() != 2) throw new IllegalArgumentException("Tuple must have an arity of 2.");
			
			IValue key = tuple.get(0);
			IValue value2 = tuple.get(1);
			updateTypes(key,value2);
			mapContent.__put(key, value2);
		}
	}
	
	protected void checkMutation() {
		if (constructedMap != null)
			throw new UnsupportedOperationException(
					"Mutation of a finalized map is not supported.");
	}
	
	@Override
	public IMap done(){
		if (constructedMap == null) {
			if (mapType == null) {
				mapType = TypeFactory.getInstance().mapType(keyType, valueType);
			}

			if (mapType.hasFieldNames()) {
				constructedMap = new PDBPersistentHashMap(keyType, mapType.getKeyLabel(), valueType, mapType.getValueLabel(), mapContent.freeze());
			} else {
				constructedMap = new PDBPersistentHashMap(keyType, valueType, mapContent.freeze());
			}
		}

		return constructedMap;
	}
}