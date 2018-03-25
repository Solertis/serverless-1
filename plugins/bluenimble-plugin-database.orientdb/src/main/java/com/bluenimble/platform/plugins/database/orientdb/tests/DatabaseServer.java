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
package com.bluenimble.platform.plugins.database.orientdb.tests;

import com.bluenimble.platform.Lang;
import com.bluenimble.platform.Traceable;
import com.bluenimble.platform.api.tracing.Tracer;
import com.bluenimble.platform.db.Database;
import com.bluenimble.platform.plugins.database.orientdb.impls.OrientDatabase;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;

public class DatabaseServer {

	private OPartitionedDatabasePool pool;
	
	public DatabaseServer () {
		pool = new OPartitionedDatabasePool (
			"remote:cherry.database.uswest.bluenimble.tech:2424/dbv2", 
			"admin", 
			"dbv2",
			2, 
			2
		);
			
	}
	
	public Database get () {
		return new OrientDatabase (pool.acquire (), new Tracer () {
			private static final long serialVersionUID = 4922972723643535449L;

			@Override
			public void onInstall (Traceable traceable) {
			}
			@Override
			public void onShutdown (Traceable traceable) {
			}

			@Override
			public void log (Level level, Object o, Throwable th) {
				System.out.println (level + " > " + o + " | " + Lang.toString (th));
			}

			@Override
			public void log (Level level, Object o, Object... args) {
				System.out.println (level + " > " + o);
			}
			@Override
			public boolean isEnabled (Level level) {
				return true;
			}
			
		});
	}
	
}
