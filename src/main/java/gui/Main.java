package gui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {

	public static void main(String[] args) {
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://test.et.hands-on-technology.de/index.php?id=19&cHash=7727dc2f837127d73c8df339fd6976f0&tx_hotet_hotetadmin%5Baction%5D=showRobogame&tx_hotet_hotetadmin%5Bcontroller%5D=Score"))
				.build();

		client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::headers)
				//.thenApply(HttpResponse::body)
				.thenAccept(System.out::println)
				.join();

		System.out.println("Finished!");
	}
}
