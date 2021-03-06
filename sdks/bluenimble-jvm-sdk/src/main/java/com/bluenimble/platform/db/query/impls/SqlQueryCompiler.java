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
package com.bluenimble.platform.db.query.impls;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.bluenimble.platform.Lang;
import com.bluenimble.platform.db.DatabaseException;
import com.bluenimble.platform.db.query.CompiledQuery;
import com.bluenimble.platform.db.query.Condition;
import com.bluenimble.platform.db.query.Filter;
import com.bluenimble.platform.db.query.GroupBy;
import com.bluenimble.platform.db.query.OrderBy;
import com.bluenimble.platform.db.query.OrderByField;
import com.bluenimble.platform.db.query.Query;
import com.bluenimble.platform.db.query.Query.Operator;
import com.bluenimble.platform.db.query.Select;

public class SqlQueryCompiler extends EventedQueryCompiler {

	private static final long serialVersionUID = -721087118950354168L;
	
	private static final String					ParamPrefix		= "p";
	
	protected interface Sql {
		String OrderBy 	= "order by";
		String GroupBy 	= "group by";
		String From 	= "from";
	}
	
	private static final Map<Operator, String> 	OperatorsMap 	= new HashMap<Operator, String> ();
	static {
		OperatorsMap.put (Operator.eq, 		"=");
		OperatorsMap.put (Operator.neq, 	"<>");
		OperatorsMap.put (Operator.gt, 		">");
		OperatorsMap.put (Operator.lt, 		"<");
		OperatorsMap.put (Operator.gte, 	">=");
		OperatorsMap.put (Operator.lte, 	"<=");
		OperatorsMap.put (Operator.like, 	"like");
		OperatorsMap.put (Operator.nlike, 	"not like");
		OperatorsMap.put (Operator.btw, 	"between");
		OperatorsMap.put (Operator.nbtw, 	"not between");
		OperatorsMap.put (Operator.in, 		"in");
		OperatorsMap.put (Operator.nin, 	"not in");
		OperatorsMap.put (Operator.nil, 	"is null");
		OperatorsMap.put (Operator.nnil, 	"is not null");
	}
	
	protected StringBuilder 		buff = new StringBuilder ();
	protected Map<String, Object>	bindings;
	
	private int						counter;
		
	protected Query.Construct		dml;
	private Query 					query;

	public SqlQueryCompiler (Query.Construct dml) {
		this (dml, -1);
	}
	
	public SqlQueryCompiler (Query.Construct dml, int counter) {
		this.dml 		= dml;
		this.counter 	= counter;
	}
	
	@Override
	protected void onQuery (Timing timing, Query query) throws DatabaseException {
		this.query 		= query;
	}

	@Override
	protected void onSelect (Timing timing, Select select)
			throws DatabaseException {
		if (Timing.start.equals (timing)) {
			buff.append (dml.name ());
		} else {
			buff.append (Lang.SPACE).append (Sql.From).append (Lang.SPACE);
			entity ();
		} 
	}

	@Override
	protected void onSelectField (String field, int count, int index)
			throws DatabaseException {
		if (!Query.Construct.select.equals (dml)) {
			return;
		}
		buff.append (Lang.SPACE); field (field);
		if (count == (index + 1)) {
			return;
		}
		buff.append (Lang.COMMA);
	}

	@Override
	protected void onFilter (Timing timing, Filter filter, boolean isWhere)
			throws DatabaseException {
		
		if (filter == null || filter.isEmpty ()) {
			return;
		}
		
		switch (timing) {
			case start:
				if (isWhere) {
					buff.append (Lang.SPACE).append (Query.Construct.where.name ()).append (Lang.SPACE); 
				} else {
					buff.append (Lang.SPACE).append (filter.parentConjunction ().name ()).append (Lang.SPACE).append (Lang.PARENTH_OPEN); 
				}
				break;
	
			case end:
				if (!isWhere) {
					buff.append (Lang.PARENTH_CLOSE); 
				}
				break;
	
			default:
				break;
		}
	}

	@Override
	protected void onCondition (Condition condition, Filter filter, int index)
			throws DatabaseException {
		
		if (index > 0) {
			buff.append (Lang.SPACE).append (filter.conjunction ().name ()).append (Lang.SPACE);
		}

		field (condition.field ());
		buff.append (Lang.SPACE).append (operatorFor (condition.operator ())).append (Lang.SPACE);
		if (Operator.nil.equals (condition.operator ()) || Operator.nnil.equals (condition.operator ())) {
			return;
		}
		
		if (condition.value () == null) {
			return;
		}
		
		Object value = condition.value ();
		
		if (Operator.in.equals (condition.operator ()) || Operator.nin.equals (condition.operator ())) {
			if (List.class.isAssignableFrom (value.getClass ())) {
				@SuppressWarnings("unchecked")
				List<Object> values = (List<Object>)value;
				if (values.isEmpty ()) {
					buff.append (Lang.ARRAY_OPEN).append (Lang.ARRAY_CLOSE);
					return;
				}
				buff.append (Lang.ARRAY_OPEN);
				for (int i = 0; i < values.size (); i++) {
					Object o = values.get (i);
					// process
					String p = bind (o);
					if (p != null) {
						buff.append (Lang.COLON).append (p);
					} else {
						valueOf (o);
					}
					if (i + 1 != values.size ()) {
						buff.append (Lang.COMMA);
					}
				}
				buff.append (Lang.ARRAY_CLOSE);
			} else if (Query.class.isAssignableFrom (value.getClass ())) {
				// process sub query
				CompiledQuery qc = new SqlQueryCompiler (Query.Construct.select, counter).compile ((Query)value);
				if (qc.bindings () != null) {
					bindings.putAll (qc.bindings ());
				}
				buff.append (Lang.PARENTH_OPEN).append (qc.query ()).append (Lang.PARENTH_CLOSE);
			}
			return;
		} 
		
		String parameter = bind (value);
		if (parameter != null) {
			buff.append (Lang.COLON).append (parameter);
		} else {
			valueOf (value);
		}
		
	}

	protected String operatorFor (Operator operator) {
		return OperatorsMap.get (operator);
	}

	@Override
	protected void onOrderBy (Timing timing, OrderBy orderBy)
			throws DatabaseException {
		if (orderBy == null || orderBy.isEmpty ()) {
			return;
		}
		
		if (Timing.end.equals (timing)) {
			return;
		}
		buff.append (Lang.SPACE).append (Sql.OrderBy);
	}

	@Override
	protected void onOrderByField (OrderByField orderBy, int count, int index)
			throws DatabaseException {
		buff.append (Lang.SPACE); field (orderBy.field ());
		buff.append (Lang.SPACE).append (orderBy.direction ().name ());
		if (count == (index + 1)) {
			return;
		}
		buff.append (Lang.COMMA);
	}

	@Override
	protected void onGroupBy (Timing timing, GroupBy groupBy)
			throws DatabaseException {
		if (groupBy == null || groupBy.isEmpty ()) {
			return;
		}
		if (Timing.end.equals (timing)) {
			return;
		}
		buff.append (Lang.SPACE).append (Sql.GroupBy);
	}

	@Override
	protected void onGroupByField (String field, int count, int index)
			throws DatabaseException {
		buff.append (Lang.SPACE); field (field);
		if (count == (index + 1)) {
			return;
		}
		buff.append (Lang.COMMA);
	}
	
	@Override
	protected CompiledQuery done () throws DatabaseException {
		return new CompiledQuery () {
			@Override
			public String query () {
				String q = buff.toString ();
				buff.setLength (0);
				return q;
			}
			
			@Override
			public Map<String, Object> bindings () {
				if (query.bindings () == null) {
					return bindings;
				}
				return query.bindings ();
			}
		};
	}
	
	protected String bind (Object value) {
		if (query.bindings () != null) {
			return null;
		}
		
		counter++;
		String parameter = ParamPrefix + (counter);

		if (bindings == null) {
			bindings = new HashMap<String, Object> ();
		}
		bindings.put (parameter, value);
		
		return parameter;
	}

	protected void valueOf (Object value) {
		String sValue = String.valueOf (value);
		if (sValue.startsWith (Lang.COLON)) {
			buff.append (sValue);
			return;
		}
		buff.append (Lang.APOS).append (Lang.replace (sValue, Lang.APOS, Lang.BACKSLASH + Lang.APOS)).append (Lang.APOS);
	}
	
	protected void entity () {
		buff.append (query.entity ());
	}
	
	protected void field (String field) {
		buff.append (field);
	}

}
