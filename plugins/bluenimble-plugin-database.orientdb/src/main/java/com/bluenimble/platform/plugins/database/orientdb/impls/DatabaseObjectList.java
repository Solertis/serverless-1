/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bluenimble.platform.plugins.database.orientdb.impls;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.bluenimble.platform.db.Database;
import com.bluenimble.platform.iterators.EmptyIterator;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class DatabaseObjectList<T> implements List<T> {

	private List<ODocument> documents;
	private Database 	database;
	
	public DatabaseObjectList (Database database, List<ODocument> documents) {
		this.database = database;
		this.documents = documents;
	}
	
	public DatabaseObjectList (Database database) {
		this (database, null);
	}
	
	@Override
	public int size () {
		if (documents == null) {
			return 0;
		}
		return documents.size ();
	}

	@Override
	public boolean isEmpty () {
		return size () <= 0;
	}

	@Override
	public boolean contains (Object o) {
		throw new UnsupportedOperationException ("contains not supported");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator () {
		if (documents == null) {
			return new EmptyIterator<T> ();
		}
		
		Iterator<ODocument> iDocs = documents.iterator ();
		if (iDocs == null) {
			return new EmptyIterator<T> ();
		}
		
		return new Iterator<T>() {
			@Override
			public boolean hasNext () {
				return iDocs.hasNext ();
			}

			@Override
			public T next () {
				return (T)new DatabaseObjectImpl (database, iDocs.next ());
			}
		};
	}

	@Override
	public Object[] toArray () {
		throw new UnsupportedOperationException ("toArray not supported");
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray (T[] a) {
		throw new UnsupportedOperationException ("toArray not supported");
	}

	@Override
	public boolean add (T e) {
		throw new UnsupportedOperationException ("add not supported");
	}

	@Override
	public boolean remove (Object o) {
		throw new UnsupportedOperationException ("remove not supported");
	}

	@Override
	public boolean containsAll (Collection<?> c) {
		throw new UnsupportedOperationException ("containsAll not supported");
	}

	@Override
	public boolean addAll (Collection<? extends T> c) {
		throw new UnsupportedOperationException ("addAll not supported");
	}

	@Override
	public boolean addAll (int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException ("addAll not supported");
	}

	@Override
	public boolean removeAll (Collection<?> c) {
		throw new UnsupportedOperationException ("removeAll not supported");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException ("retainAll not supported");
	}

	@Override
	public void clear () {
		if (documents == null) {
			return;
		}
		documents.clear ();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get (int index) {
		if (documents == null) {
			return null;
		}
		return (T)new DatabaseObjectImpl (database, documents.get (index));
	}

	@Override
	public T set (int index, T element) {
		throw new UnsupportedOperationException ("set not supported");
	}

	@Override
	public void add (int index, T element) {
		throw new UnsupportedOperationException ("add not supported");
	}

	@Override
	public T remove (int index) {
		if (documents == null) {
			return null;
		}
		documents.remove (index);
		return null;
	}

	@Override
	public int indexOf (Object o) {
		throw new UnsupportedOperationException ("indexOf not supported");
	}

	@Override
	public int lastIndexOf (Object o) {
		throw new UnsupportedOperationException ("lastIndexOf not supported");
	}

	@Override
	public ListIterator<T> listIterator () {
		throw new UnsupportedOperationException ("listIterator not supported");
	}

	@Override
	public ListIterator<T> listIterator (int index) {
		throw new UnsupportedOperationException ("listIterator not supported");
	}

	@Override
	public List<T> subList (int fromIndex, int toIndex) {
		throw new UnsupportedOperationException ("subList not supported");
	}

}
