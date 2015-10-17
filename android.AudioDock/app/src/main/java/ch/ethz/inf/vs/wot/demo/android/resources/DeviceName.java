package ch.ethz.inf.vs.wot.demo.android.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class DeviceName extends CoapResource {
	
	private String name = "AudioDock";

	public DeviceName() {
		super("n");
		getAttributes().setTitle("Name");
		getAttributes().addInterfaceDescription("core#p");
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, name, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		name = exchange.getRequestText();

		// complete the request
		exchange.respond(CHANGED);
	}
}
