package com.bluenimble.platform.servers.broker.server.impls;

import java.util.Set;

import com.bluenimble.platform.json.JsonObject;
import com.bluenimble.platform.servers.broker.Message;
import com.bluenimble.platform.servers.broker.Peer;
import com.bluenimble.platform.servers.broker.PeerAck;
import com.bluenimble.platform.servers.broker.Response;
import com.bluenimble.platform.servers.broker.listeners.EventListener;
import com.bluenimble.platform.servers.broker.listeners.EventListener.Default;
import com.bluenimble.platform.servers.broker.utils.PeerUtils;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;

public class DelegateListener implements DataListener<Object> {
	
	private static final PeerAck NoAck = new PeerAck () {
		private static final long serialVersionUID = -6356045593057157195L;
		@Override
		public boolean requested () {
			return false;
		}
		@Override
		public void notify (Object... data) {
		}
	};

	protected EventListener<Object>		listener;
	protected Set<String>				accessibleBy;
	
	public DelegateListener (EventListener<Object> listener, Set<String> accessibleBy) {
		this.listener 		= listener;
		this.accessibleBy 	= accessibleBy;
	}
	
	@Override
	public void onData (SocketIOClient client, Object data, AckRequest ackRequest) throws Exception {
		Peer peer = PeerUtils.peer (client);
		
		if (!peer.is (accessibleBy)) {
			peer.trigger (Default.error.name (), new JsonObject ().set (Message.Status, Response.Error).set (Message.Reason, "Unauthorized action"));
			return;
		}
		
		listener.process (peer, data, (ackRequest == null || !ackRequest.isAckRequested ()) ? NoAck : new PeerAck () {
			private static final long serialVersionUID = -2426621346865190051L;
			@Override
			public boolean requested () {
				return ackRequest != null && ackRequest.isAckRequested ();
			}
			@Override
			public void notify (Object... data) {
				if (ackRequest != null && ackRequest.isAckRequested ()) {
					ackRequest.sendAckData (data);
				}
			}
		});
	}
	
}
