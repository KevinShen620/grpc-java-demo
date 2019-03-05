/**
 * 
 */
package kevsn.grpc.service;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * @author Kevin
 *
 */
public class EchoServer {

	private static final Logger logger = Logger.getLogger("echoServer");

	private Server server;

	public void startServer() {
		server = ServerBuilder.forPort(5000)
				.addService(new EchoServiceServerImpl()).build();
		try {
			server.start();
			server.awaitTermination();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		server.shutdown();
	}

	public static void main(String[] args) {
		logger.info("start server");
		EchoServer server = new EchoServer();
		server.startServer();
	}
}
