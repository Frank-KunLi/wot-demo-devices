package ch.ethz.inf.vs.wot.demo.w3c.resources;

import org.eclipse.californium.core.server.resources.CoapExchange;

import ch.ethz.inf.vs.wot.demo.w3c.utils.PropertyResource;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CONTENT;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class DeviceManufacturer extends PropertyResource {

	public DeviceManufacturer() {
		super("Property", "Manufacturer", "mfc", "xsd:string", false);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, "Osram", TEXT_PLAIN);
	}
}
