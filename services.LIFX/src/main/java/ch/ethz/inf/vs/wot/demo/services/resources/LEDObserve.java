package ch.ethz.inf.vs.wot.demo.services.resources;

import java.awt.Color;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.services.LIFX;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class LEDObserve extends CoapResource {
	
	private static String uri = "";
	private static CoapClient client = null;
	private static CoapObserveRelation handle = null;

	public LEDObserve() {
		super("obs");
		getAttributes().setTitle("Follow color");
		getAttributes().addResourceType("led:obs");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, uri, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String in = exchange.getRequestText();
		
		if (in.startsWith("coap://")) {

			exchange.respond(CHANGED);
			
			if (handle!=null) handle.proactiveCancel();
			
			client = this.createClient(in);
			handle = client.observeAndWait(new CoapHandler() {
				
				@Override
				public void onLoad(CoapResponse response) {
					try {
						LIFX.bulb.setColor( Color.decode(response.getResponseText()) );
					} catch (NumberFormatException e) {
						handle.proactiveCancel();
					}
				}
				
				@Override
				public void onError() {
					handle.reactiveCancel();
				}
			});
			
		
			changed();
			return;
		} else {
			exchange.respond(BAD_REQUEST, "coap URI");
		}
	}
}
