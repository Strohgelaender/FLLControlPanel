package telnet;

import static telnet.FoobarTelnetClient.nowPlaying;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;

class MusicThread extends Thread {


	private static final int waitingTime = 1;
	private final String interpret;
	private final String song;
	private final FoobarTelnetClient client;

	MusicThread(final String interpret, final String song, final FoobarTelnetClient client) {
		this.interpret = customEscape(interpret);
		this.song = customEscape(song);
		this.client = client;
	}

	private String customEscape(String string) {
		return StringEscapeUtils.escapeHtml4(
				string.replace("ñ", "&ntilde;")
						.replace("ä", "&auml;")
						.replace("Ä", "&Auml;")
						.replace("ö", "&ouml;")
						.replace("Ö", "&Ouml;")
						.replace("ü", "&uuml;")
						.replace("Ü", "&Uuml;")
		);
	}

	@Override
	public void run() {
		System.out.println("run entered: " + song);
		try {
			Thread.sleep(waitingTime);
		} catch (InterruptedException e) {
			System.out.println("Interrupted: " + song);
			return;
		}
		System.out.println("doing Output: " + song);
		if (client.isConnected() && client.isOutputActive()) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(nowPlaying));
				String output = String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
						"<nowPlaying>\n" +
						"\t<song>%s</song>\n" +
						"\t<interpret>%s</interpret>\n" +
						"\t<time>%s</time>\n" +
						"</nowPlaying>", song, interpret, System.currentTimeMillis());
				writer.write(output);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
