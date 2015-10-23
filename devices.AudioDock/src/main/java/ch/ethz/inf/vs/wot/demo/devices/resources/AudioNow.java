package ch.ethz.inf.vs.wot.demo.devices.resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Arrays;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;

public class AudioNow extends CoapResource {
	
	private static String[] songs = {"bohemian_rhapsody.mp3", "gangnam_style.mp3", "fur_elise.mp3"};
	private static String song = songs[0];
	
	public static String getSong() {
		return song;
	}

	public AudioNow() {
		super("now");
		getAttributes().setTitle("Now playing");
		getAttributes().addInterfaceDescription("core#p");
		getAttributes().setObservable();

		setObservable(true);
		
		AudioPlaying.player.setMP3(song);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		exchange.respond(CONTENT, song, TEXT_PLAIN);
	}
	
	@Override
	public void handlePUT(CoapExchange exchange) {

//		if (!exchange.getRequestOptions().isContentFormat(TEXT_PLAIN)) {
//			exchange.respond(BAD_REQUEST, "text/plain only");
//			return;
//		}
		
		if (!Arrays.asList(songs).contains(exchange.getRequestText())) {
			exchange.respond(BAD_REQUEST, Arrays.toString(songs));
		} else {
			song = exchange.getRequestText();
			exchange.respond(CHANGED);

			AudioPlaying.player.stop();
			AudioPlaying.player.setSong(song);
			//AudioPlaying.player.play();
			changed();
		}
	}
}
