import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) {
		int port = 5000;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server listening on port " + port + "...");
			try (Socket socket = serverSocket.accept()) {
				System.out.println("Client connected: " + socket.getRemoteSocketAddress());

				BufferedReader socketReader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				PrintWriter socketWriter = new PrintWriter(
						new OutputStreamWriter(socket.getOutputStream()), true);

				Thread readerThread = new Thread(() -> {
					String line;
					try {
						while ((line = socketReader.readLine()) != null) {
							System.out.println("Client: " + line);
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
			}
		} catch (IOException e) {
			System.err.println("Server error: " + e.getMessage());
		}
	}
}
