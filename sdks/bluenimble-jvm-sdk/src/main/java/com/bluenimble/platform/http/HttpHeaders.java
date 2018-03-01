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
package com.bluenimble.platform.http;

public interface HttpHeaders {
	
	String ACCEPT 				= "ACCEPT";
	String ACCEPT_ENCODING 		= "Accept-Encoding";
	String ACCEPT_CHARSET 		= "Accept-Charset";
	String AUTHORIZATION 		= "Authorization";
	String CONTENT_ENCODING 	= "Content-Encoding";
	String CACHE_CONTROL 		= "Cache-Control";
	String CONTENT_DISPOSITION 	= "Content-Disposition";
	String CONTENT_TYPE 		= "Content-Type";
	String CONTENT_LENGTH 		= "Content-Length";
	String CONNECTION			= "Connection";
	String IF_MODIFIED_SINCE 	= "If-Modified-Since";
	String LAST_MODIFIED 		= "Last-Modified";
	String PRAGMA 				= "Pragma";
	String PUBLIC 				= "public";
	String MAX_AGE 				= "max-age";
	String USER_AGENT 			= "User-Agent";
	String SET_COOKIE 			= "Set-Cookie";
	String COOKIE 				= "Cookie";
	String SERVER 				= "Server";
	
}
