import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
	public static void main(String[] args) {
		int port = 5000;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}

		List<ClientConnection> clients = new CopyOnWriteArrayList<>();
		ExecutorService pool = Executors.newCachedThreadPool();
		AtomicBoolean running = new AtomicBoolean(true);

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server listening on port " + port + "...");

			Thread consoleThread = new Thread(() -> {
				BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
				String line;
				try {
					while ((line = stdin.readLine()) != null) {
						if ("exit".equalsIgnoreCase(line.trim())) {
							running.set(false);
							try {
								serverSocket.close();
							} catch (IOException e) {
								// ignore
							}
							break;
						}
						broadcast(clients, line);
					}
				} catch (IOException e) {
					System.out.println("Console closed.");
				}
			});
			consoleThread.setDaemon(true);
			consoleThread.start();

			while (running.get()) {
				try {
					Socket socket = serverSocket.accept();
					ClientConnection client = new ClientConnection(socket);
					clients.add(client);
					System.out.println("Client connected: " + client.remoteAddress);
					pool.execute(() -> handleClient(client, clients));
				} catch (IOException e) {
					if (running.get()) {
						System.err.println("Accept error: " + e.getMessage());
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Server error: " + e.getMessage());
		} finally {
			pool.shutdownNow();
			for (ClientConnection client : clients) {
				client.close();
			}
		}
	}

	private static void handleClient(ClientConnection client, List<ClientConnection> clients) {
		String line;
		try {
			while ((line = client.reader.readLine()) != null) {
				if ("exit".equalsIgnoreCase(line.trim())) {
					break;
				}
				execLine(client, line);
			}
		} catch (IOException e) {
			System.out.println("Connection closed: " + client.remoteAddress);
		} finally {
			clients.remove(client);
			client.close();
		}
	}

	private static void broadcast(List<ClientConnection> clients, String message) {
		for (ClientConnection client : clients) {
			client.writer.println(message);
		}
	}

	private static void execLine(ClientConnection client, String line) {
		System.out.println("Client " + client.remoteAddress + ": " + line);
	}

	private static class ClientConnection {
		private final Socket socket;
		private final BufferedReader reader;
		private final PrintWriter writer;
		private final String remoteAddress;

		private ClientConnection(Socket socket) throws IOException {
			this.socket = socket;
			this.remoteAddress = socket.getRemoteSocketAddress().toString();
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
		}

		private void close() {
			try {
				socket.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
