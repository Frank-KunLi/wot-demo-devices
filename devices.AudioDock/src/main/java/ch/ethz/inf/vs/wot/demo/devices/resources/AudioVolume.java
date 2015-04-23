package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.*;

public class AudioVolume extends CoapResource {
	
	private static int volume = 18;
	
	public static int getVolume() {
		return volume;
	}

	public AudioVolume() {
		super("vol");
		getAttributes().setTitle("Volume");
		getAttributes().addInterfaceDescription("core#a");
		getAttributes().setObservable();

		setObservable(true);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, ""+volume, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
			exchange.respond(BAD_REQUEST, "text/plain only");
			return;
		}
		
		String pl = exchange.getRequestText();
		int vol = Integer.parseInt(pl);
		if (pl.matches("/^[0-9]+$/") && (0 <= vol) && (vol <= 100)) {
			volume = vol;
		} else {
			exchange.respond(BAD_REQUEST, "0--100");
			return;
		}

		// complete the request
		exchange.respond(CHANGED);
		
		changed();
	}
}
