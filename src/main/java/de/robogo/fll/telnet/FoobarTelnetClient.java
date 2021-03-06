package de.robogo.fll.telnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

public class FoobarTelnetClient implements Runnable {

	public static final File nowPlaying = new File("C:\\Users\\Lucas Welscher\\Desktop\\FLL\\nowPlaying.xml");
	private static final String IP_OF_MUSIC_PC = "localhost"; //TODO
	private static final int PORT_OF_MUSIC_PC = 3333; //TODO
	private final TelnetClient client;
	private Thread reader;
	private boolean active = true;
	private MusicThread musicThread;

	private final Thread shutdownHook = new Thread(() -> disconnect(false));

	public FoobarTelnetClient() {
		client = new TelnetClient();
		//TODO was bedeutet das?
		TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
		EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
		SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

		try {
			client.addOptionHandler(ttopt);
			client.addOptionHandler(echoopt);
			client.addOptionHandler(gaopt);
		} catch (Exception e) {
			System.err.println("Error registering option handlers: " + e.getMessage());
		}
	}

	public static void main(String[] args) throws InterruptedException {
		new FoobarTelnetClient().connect();
		Thread.sleep(5000000L);
	}

	public void connect() {
		//TODO warum ist das in einem Loop?
		//reconnect irendwie machen -  das hier als nichmal eigener Thread?
		try {
			client.connect(IP_OF_MUSIC_PC, PORT_OF_MUSIC_PC);

			reader = new Thread(this);
			reader.start();

			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect(boolean removeShutdownHandler) {
		try {
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		reader.interrupt();
		if (removeShutdownHandler) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		}
	}

	@Override
	public void run() {
		InputStream instr = client.getInputStream();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(instr, StandardCharsets.UTF_8))) {
			while (!Thread.interrupted()) {
				String input = reader.readLine();
				if (input == null)
					continue;
				String[] message = input.split("\\|");
				if (message[0].equals("111")) {
					if (message.length != 6) {
						System.out.println(input);
					} else {
						//now playing
						if (active) {
							if (musicThread != null && musicThread.isAlive()) {
								musicThread.interrupt();
							}
							musicThread = new MusicThread(message[4], message[5], this);
							musicThread.start();
						} //else { //else commented out for debug output
						System.out.println(message[4] + " - " + message[5]);
						//}
					}
				} else if (message[0].equals("112") || message[0].equals("113")) {
					//112: Stop, 113: Pause
					if (musicThread != null) {
						musicThread.interrupt();
					}
				} else {
					System.out.println(input);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	public boolean isOutputActive() {
		return active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}
}
