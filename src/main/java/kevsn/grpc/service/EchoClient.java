/**
 * 
 */
package kevsn.grpc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import kevsn.grpc.protobuf.EchoRequest;
import kevsn.grpc.protobuf.EchoResponse;
import kevsn.grpc.protobuf.EchoServiceGrpc;
import kevsn.grpc.protobuf.EchoServiceGrpc.EchoServiceBlockingStub;
import kevsn.grpc.protobuf.EchoServiceGrpc.EchoServiceFutureStub;
import kevsn.grpc.protobuf.EchoServiceGrpc.EchoServiceStub;

/**
 * @author Kevin
 *
 */
public class EchoClient {

	private static final Logger logger = Logger.getLogger("echoClient");

	public void testSimpleMessage() {
		ManagedChannel channel = getChannel();
		EchoServiceBlockingStub stub = EchoServiceGrpc.newBlockingStub(channel);
		EchoResponse resp = stub.echoSimple(newRequest(0));
		logger.info("response:" + resp);
	}

	private ManagedChannel getChannel() {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5000)
				.usePlaintext()
				.build();
		return channel;
	}

	public void testBothStreamMessage() throws InterruptedException {
		ManagedChannel channel = getChannel();
		EchoServiceStub stub = EchoServiceGrpc.newStub(channel);
		CountDownLatch l = new CountDownLatch(1);
		StreamObserver<EchoRequest> reqStream = stub
				.echoBothStream(new ClientResponseObserver<EchoRequest, EchoResponse>() {

					private StreamObserver<EchoRequest> reqstream;

					@Override
					public void onNext(EchoResponse value) {
						logger.info("received:" + value.getMsg());
						int reIndex = value.getIndex();
						if (reIndex == 9) {
							// 如果已经是第十条消息了，则停止发送
							this.reqstream.onCompleted();
						} else {
							int index = reIndex + 1;
							EchoRequest req = newRequest(index);
							this.reqstream.onNext(req);
						}
					}

					@Override
					public void onError(Throwable t) {
						t.printStackTrace();
					}

					@Override
					public void onCompleted() {
						// 服务端消息已经返回完毕
						l.countDown();
					}

					@Override
					public void beforeStart(
							ClientCallStreamObserver<EchoRequest> requestStream) {
						this.reqstream = requestStream;
					}
				});
		// 发送第一条消息，第一条消息发送完毕后，会遵循一个请求一个响应的模式进行下去
		reqStream.onNext(newRequest(0));
		l.await();
	}

	public void testReqStreamMessage() throws InterruptedException {
		ManagedChannel channel = getChannel();
		EchoServiceStub stub = EchoServiceGrpc.newStub(channel);
		List<EchoRequest> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			EchoRequest req = newRequest(i);
			list.add(req);
		}
		CountDownLatch latch = new CountDownLatch(1);
		StreamObserver<EchoRequest> reqstream = stub
				.echoReqStream(new StreamObserver<EchoResponse>() {

					@Override
					public void onNext(EchoResponse value) {
						logger.info("received respose:" + value.getMsg());
					}

					@Override
					public void onError(Throwable t) {
						t.printStackTrace();
					}

					@Override
					public void onCompleted() {
						latch.countDown();
					}
				});

		list.stream().forEach(req -> reqstream.onNext(req));
		reqstream.onCompleted();
		latch.await();
	}

	private EchoRequest newRequest(int i) {
		return EchoRequest.newBuilder().setMsg("request_" + i).setIntdex(i)
				.build();
	}

	public static void main(String[] args) throws InterruptedException {
		EchoClient client = new EchoClient();
		// client.testSimpleMessage();
		// client.testReqStreamMessage();
		client.testBothStreamMessage();
	}
}
