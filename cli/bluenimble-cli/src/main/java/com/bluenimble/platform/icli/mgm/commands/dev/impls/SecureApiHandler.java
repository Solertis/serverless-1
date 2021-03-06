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
package com.bluenimble.platform.icli.mgm.commands.dev.impls;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bluenimble.platform.Json;
import com.bluenimble.platform.Lang;
import com.bluenimble.platform.api.Api;
import com.bluenimble.platform.cli.Tool;
import com.bluenimble.platform.cli.ToolContext;
import com.bluenimble.platform.cli.command.CommandExecutionException;
import com.bluenimble.platform.cli.command.CommandHandler;
import com.bluenimble.platform.cli.command.CommandResult;
import com.bluenimble.platform.cli.command.impls.DefaultCommandResult;
import com.bluenimble.platform.icli.mgm.BlueNimble;
import com.bluenimble.platform.icli.mgm.CliSpec;
import com.bluenimble.platform.icli.mgm.utils.CodeGenUtils;
import com.bluenimble.platform.icli.mgm.utils.CodeGenUtils.Tokens;
import com.bluenimble.platform.icli.mgm.utils.SpecUtils;
import com.bluenimble.platform.json.JsonObject;

public class SecureApiHandler implements CommandHandler {

	private static final long serialVersionUID = 7185236990672693349L;
	
	private static final Set<String> Schemes = new HashSet<String> ();
	static {
		Schemes.add ("token");
		Schemes.add ("cookie");
		Schemes.add ("basic");
		Schemes.add ("key");
		Schemes.add ("signature");
	}
	
	private static final Set<String> Methods = new HashSet<String> ();
	static {
		Methods.add ("up");
		Methods.add ("fb");
		Methods.add ("gp");
		Methods.add ("gh");
		Methods.add ("li");
	}
	
	@Override
	public CommandResult execute (Tool tool, String... args) throws CommandExecutionException {
		
		if (args == null || args.length < 1) {
			throw new CommandExecutionException ("Command syntax:\nsecure api [api namespace] [+ seperated list of shemes, default to 'cookie+token'] [+ seperated list of methods, default to 'up']\nTry something like secure api token+cookie up+fb");
		}
		
		String api = args [0];

		String apiPath = Json.getString (Json.getObject (BlueNimble.Config, CliSpec.Config.Apis), api);
		if (Lang.isNullOrEmpty (apiPath)) {
			throw new CommandExecutionException ("api path not found for '" + api + "'");
		}
		File apiFolder = new File (BlueNimble.Workspace, apiPath);
		if (!apiFolder.exists () || !apiFolder.isDirectory ()) {
			throw new CommandExecutionException ("invalid api folder '" + apiPath + "'");
		}
		
		// schemes
		String [] schemes = getSchemes  (args);
		
		for (String s : schemes) {
			if (!Schemes.contains (s)) {
				throw new CommandExecutionException ("unsupported authentication scheme '" + s + "'");
			}
		}
		
		boolean oauth = false;

		// methods
		String [] methods = getMethods (args);
		if (methods != null) {
			for (String m : methods) {
				if (!Methods.contains (m)) {
					throw new CommandExecutionException ("unsupported authentication method '" + m + "'");
				} else if (!"up".equals (m)) {
					oauth = true;
				}
			}
		}
		
		// load tplApiSpec
		JsonObject oTplApi = null;
		try {
			oTplApi = Json.load (new File (BlueNimble.Home, "templates/security/api.json"));
		} catch (Exception ex) {
			throw new CommandExecutionException ("can't read api template security spec file templates/security/api.json");
		}
		
		JsonObject tplSchemes = (JsonObject)Json.find (oTplApi, "security", "schemes");
		
		// read api spec
		JsonObject oApi = SpecUtils.read (apiFolder);
		
		// add security schemes
		JsonObject oSecurity = Json.getObject (oApi, Api.Spec.Security.class.getSimpleName ().toLowerCase ());
		if (oSecurity == null) {
			oSecurity = new JsonObject ();
			oApi.set (Api.Spec.Security.class.getSimpleName ().toLowerCase (), oSecurity);
		}
		JsonObject oSchemes = Json.getObject (oSecurity, Api.Spec.Security.Schemes);
		if (oSchemes == null) {
			oSchemes = new JsonObject ();
			oSecurity.set (Api.Spec.Security.Schemes, oSchemes);
		}
		for (String s : schemes) {
			if (!oSchemes.containsKey (s)) {
				oSchemes.set (s, tplSchemes.get (s));
				tool.printer ().info ("security scheme '" + s + "' added to api '" + api + "'");
			}
		}
		
		SpecUtils.write (apiFolder, oApi);
		
		// tool.printer ().content ("Api '" + api + "' updated spec", oApi.toString (2));
		
		apiFolder = SpecUtils.specFolder (apiFolder);
		
		// add services
		if (Lang.existsIn ("up", methods)) {
			try {
				addService (tool, api, apiFolder, "signup");
			} catch (Exception ex) {
				throw new CommandExecutionException ("An error occured when generating code for Signup service. Cause: " + ex.getMessage (), ex);
			}
			// copy email template
			File emailsTplsFolder = new File (apiFolder, "resources/templates/emails");
			if (!emailsTplsFolder.exists ()) {
				emailsTplsFolder.mkdirs ();
			}
			
			File apiSignupTplFile = new File (emailsTplsFolder, "signup.html");
			if (!apiSignupTplFile.exists ()) {
				File signupTplFile = new File (BlueNimble.Home, "templates/security/templates/emails/signup.html");
				 
				JsonObject tokens = new JsonObject ();
				tokens.put (Tokens.api, api);
				tokens.put (Tokens.Api, api.substring (0, 1).toUpperCase () + api.substring (1));
				
				CodeGenUtils.writeFile (signupTplFile, apiSignupTplFile, tokens, null);
				tool.printer ().important (
					"An activation email html file was created -> " + CodeGenUtils.underline (tool, "'templates/emails/" + apiSignupTplFile.getName ()) + "'. It's used by the Signup service" +
					"\nMake sure that the email feature is added to your space in order to send emails.\nUse command 'add feature' to add an smtp server config"
				); 
			}
			try {
				addService (tool, api, apiFolder, "activate");
			} catch (Exception ex) {
				throw new CommandExecutionException ("An error occured when generating code for Activate service. Cause: " + ex.getMessage (), ex);
			}
			try {
				addService (tool, api, apiFolder, "login");
			} catch (Exception ex) {
				throw new CommandExecutionException ("An error occured when generating code for Login service. Cause: " + ex.getMessage (), ex);
			}
			try {
				addService (tool, api, apiFolder, "changePassword");
			} catch (Exception ex) {
				throw new CommandExecutionException ("An error occured when generating code for ChangePassword service. Cause: " + ex.getMessage (), ex);
			}
			try {
				addService (tool, api, apiFolder, "resend");
			} catch (Exception ex) {
				throw new CommandExecutionException ("An error occured when generating code for Resend service. Cause: " + ex.getMessage (), ex);
			}
			try {
				addService (tool, api, apiFolder, "createToken");
			} catch (Exception ex) {
				throw new CommandExecutionException ("An error occured when generating code for CreateToken service. Cause: " + ex.getMessage (), ex);
			}
		}
		if (oauth) {
			@SuppressWarnings("unchecked")
			Map<String, Object> vars = (Map<String, Object>)tool.getContext (Tool.ROOT_CTX).get (ToolContext.VARS);

			String specLang 	= (String)vars.get (BlueNimble.DefaultVars.SpecLanguage);
			if (Lang.isNullOrEmpty (specLang)) {
				specLang = BlueNimble.SpecLangs.Json;
			}
			
			try {
				addService (tool, api, apiFolder, "oAuth");
				tool.printer ().important ("Make sure that your clientId and sercretId are set in your oauth providers.\nSee service spec file " + CodeGenUtils.underline (tool, "resources/services/security/OAuth." + specLang));
			} catch (Exception ex) {
				throw new CommandExecutionException ("An error occured when generating code for oAuth service. Cause: " + ex.getMessage (), ex);
			}
		}

		return new DefaultCommandResult (CommandResult.OK, null);
		
	}
	
	private void addService (Tool tool, String api, File root, String service) throws Exception {
		File secSpecsFolder = new File (root, "resources/services/security");
		if (!secSpecsFolder.exists ()) {
			secSpecsFolder.mkdirs ();
		}
		
		JsonObject tokens = new JsonObject ();
		tokens.put (Tokens.api, api);
		tokens.put (Tokens.Api, api.substring (0, 1).toUpperCase () + api.substring (1));
		tokens.put (Tokens.service, service);
		tokens.put (Tokens.Service, service.substring (0, 1).toUpperCase () + service.substring (1));
		
		@SuppressWarnings("unchecked")
		Map<String, Object> vars = (Map<String, Object>)tool.getContext (Tool.ROOT_CTX).get (ToolContext.VARS);

		String specLang 	= (String)vars.get (BlueNimble.DefaultVars.SpecLanguage);
		if (Lang.isNullOrEmpty (specLang)) {
			specLang = BlueNimble.SpecLangs.Json;
		}
		
		String uService = Json.getString (tokens, Tokens.Service);
		
		tool.printer ().node (0, "'" + CodeGenUtils.highlight (tool, uService) + "' Service"); 
		File specFile = new File (BlueNimble.Home, "templates/security/services/" + service + "/spec.json");
		CodeGenUtils.writeFile (specFile, new File (secSpecsFolder, uService + "." + specLang), tokens, specLang);
		tool.printer ().node (1, "  spec file 'services/" + CodeGenUtils.underline (tool, "security/" + uService + "." + specLang) + "'"); 
		
		File scriptFile = new File (BlueNimble.Home, "templates/security/services/" + service + "/function.js");
		if (scriptFile.exists ()) {
			File secFunctionsFolder = new File (root, "resources/functions/security");
			if (!secFunctionsFolder.exists ()) {
				secFunctionsFolder.mkdirs ();
			}
			
			CodeGenUtils.writeFile (scriptFile, new File (secFunctionsFolder, uService + ".js"), tokens, null);
			
			tool.printer ().node (1, "script file 'functions/" + CodeGenUtils.underline (tool, "security/" + uService + ".js") + "'"); 
		}
	}

	private String [] getSchemes (String [] args) {
		if (args.length == 1) {
			return new String [] { "token", "cookie" };
		}
		String sSchemes = args [1];
		if (sSchemes.equals (Lang.STAR)) {
			return new String [] {"token", "cookie", "signature", "basic"};
		} else {
			return Lang.split (sSchemes.toLowerCase (), Lang.PLUS, true);
		}
	}

	private String [] getMethods (String [] args) {
		if (args.length < 3) {
			return new String [] {"up"};
		}
		String sMethods = args [2];
		if (sMethods.equals (Lang.STAR)) {
			return new String [] {"up", "fb", "li", "gh", "gp"};
		} else {
			return Lang.split (sMethods.toLowerCase (), Lang.PLUS, true);
		}
	}

	@Override
	public String getName () {
		return "api";
	}

	@Override
	public String getDescription () {
		return "secure an api. Ex. secure api token+cookie up+fb. This will enable the token and cookie based authentication and will generate the signup, activate and login services for both user/password and facebook methods"
				+ "If you want to generate all supported schemes and methods, type in ' secure api your-api * * '"
				+ "\n\t available schemes:"
				+ "\n\t   'token'  requests should send a valid token (see Login.js service and api.json spec)." 
				+ "\n\t            This scheme is the most used in mobile applications and IoT devices."
				+ "\n\t            Supported protocols : Http, CoAP and Mqtt"
				+ "\n\t   'cookie' requests should send a valid cookie (see api.json spec)." 
				+ "\n\t            This scheme is used only in web applications"
				+ "\n\t            Supported protocols: Http"
				+ "\n\t   'basic'  requests should send the HTTP Authorization Basic Header" 
				+ "\n\t            This scheme is used in web applications and some mobile applications specific cases"
				+ "\n\t            Supported protocols: Http"
				+ "\n\t   'signature'  devices and application should sign requests to be able to call this api services." 
				+ "\n\t            This scheme mostly used in cloud integration and some IoT and mobile use cases"
				+ "\n\t            Supported protocols: Http, CoAP and Mqtt"
				+ "\n"
				+ "\n\t available methods:"
				+ "\n\t   'up'     (User/Password) classic User/Password authentication. The secure command will generate services to handle signup, account activation, login and change password" 
				+ "\n\t   'fb'     (Facebook)      The secure command will generate the oauth service for all oauth providers" 
				+ "\n\t   'li'     (LinkedIn)" 
				+ "\n\t   'gp'     (Google +)" 
				+ "\n\t   'gh'     (GitHub)" 
				;
	}

	@Override
	public Arg [] getArgs () {
		return new Arg [] {
				new AbstractArg () {
					@Override
					public String name () {
						return "api";
					}
					@Override
					public String desc () {
						return "api namespace";
					}
				},
				new AbstractArg () {
					@Override
					public String name () {
						return "schemes";
					}
					@Override
					public String desc () {
						return "(+) plus-separated list of schemes such as token+signature. * means all schemes";
					}
					@Override
					public boolean required () {
						return false;
					}
				},
				new AbstractArg () {
					@Override
					public String name () {
						return "methods";
					}
					@Override
					public String desc () {
						return "(+) plus-separated list of methods such as up+li. * means all methods. generating code for these methods makes sense for web apps or mobile apps with a server-oauth flow";
					}
					@Override
					public boolean required () {
						return false;
					}
				}
		};
	}


}
