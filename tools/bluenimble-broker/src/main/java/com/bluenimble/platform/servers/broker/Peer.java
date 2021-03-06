package com.bluenimble.platform.servers.broker;

import java.io.Serializable;
import java.util.Set;

import com.bluenimble.platform.json.JsonObject;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;

public interface Peer extends Serializable {
	
	String Key = "Broker.Peer.Key";

	interface Spec {
		String Id 			= "id";
		String Type 		= "type";
		String Durable 		= "durable";
		String MonoChannel 	= "monoChannel";
		String Channels 	= "channels";
	}
	
	// default types
	enum Type {
		unknown,
		joker,
		node
	}
	
	String		id				();
	void		id				(String id);
	
	String		type			();
	void		type			(String type);
	
	boolean 	isDurable 		();
	void		setDurable		(boolean durable);
	
	boolean 	isMonoChannel 	();
	void		setMonoChannel	(boolean monoChannel);
	
	Set<String> channels 		();
	void		addChannel		(String channel);
	boolean		hasAccess		(String channel);
	
	boolean		is 				(Set<String> peerTypes);
	boolean		isNode 			();
	
	Set<String> joined 			();
	
	void		init 			(SocketIOServer server, SocketIONamespace namespace, SocketIOClient client);
	
	void		trigger 		(String event, Object... message);
	
	void		join 			(String channel);
	void		leave 			(String channel);
	
	void 		broadcast 		(String channel, Object data);
	
	boolean 	canJoin			(String channel);
	
	boolean 	canPublish		(String channel);
	
	void		terminate 		(int delay);
	
	JsonObject	info			();

}
