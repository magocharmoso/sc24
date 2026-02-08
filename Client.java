import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	public static void main(String[] args) {
		String host = "localhost";
		int port = 5000;

		if (args.length >= 1) {
			host = args[0];
		}
		if (args.length >= 2) {
			port = Integer.parseInt(args[1]);
		}

		try (Socket socket = new Socket(host, port)) {
			System.out.println("Connected to server " + host + ":" + port);

			BufferedReader socketReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			PrintWriter socketWriter = new PrintWriter(
					new OutputStreamWriter(socket.getOutputStream()), true);

			Thread readerThread = new Thread(() -> {
				String line;
				try {
					while ((line = socketReader.readLine()) != null) {
						System.out.println("Server: " + line);
						if ("exit".equalsIgnoreCase(line.trim())) {
							break;
						}
					}
				} catch (IOException e) {
					System.out.println("Connection closed.");
				}
			});
			readerThread.setDaemon(true);
			readerThread.start();

			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = stdin.readLine()) != null) {
				socketWriter.println(line);
				if ("exit".equalsIgnoreCase(line.trim())) {
					break;
				}
			}
		} catch (IOException e) {
			System.err.println("Client error: " + e.getMessage());
		}
	}
}
