/**
 * 
 */
package kevsn.grpc.service;

import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;
import kevsn.grpc.protobuf.EchoRequest;
import kevsn.grpc.protobuf.EchoResponse;
import kevsn.grpc.protobuf.EchoServiceGrpc.EchoServiceImplBase;

/**
 * @author Kevin
 *
 */
public class EchoServiceServerImpl extends EchoServiceImplBase {

	private Logger logger = Logger.getLogger("echo server");

	@Override
	public void echoSimple(EchoRequest request,
			StreamObserver<EchoResponse> responseObserver) {
		String msg = request.getMsg();
		logger.info("receive message:" + msg);
		EchoResponse resp = newResp(request);
		responseObserver.onNext(resp);
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<EchoRequest> echoReqStream(
			StreamObserver<EchoResponse> responseObserver) {
		return new StreamObserver<EchoRequest>() {

			@Override
			public void onNext(EchoRequest value) {
				String reqMsg = value.getMsg();
				logger.info("received:" + reqMsg);
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onCompleted() {
				// responseObserver.onNext(resp);
				responseObserver.onCompleted();
			}
		};
	}

	@Override
	public StreamObserver<EchoRequest> echoBothStream(
			StreamObserver<EchoResponse> responseObserver) {
		return new StreamObserver<EchoRequest>() {

			@Override
			public void onNext(EchoRequest value) {
				// 接受一个消息，返回一个消息
				logger.info("received:" + value.getMsg());
				EchoResponse resp = newResp(value);
				responseObserver.onNext(resp);
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onCompleted() {
				// responseObserver.onCompleted();
				// do nothing
				// 客户端已经停止发送，则服务端也停止
				 responseObserver.onCompleted();
			}
		};
	}

	private EchoResponse newResp(EchoRequest req) {
		return EchoResponse.newBuilder().setMsg("response_" + req.getMsg())
				.setIndex(req.getIntdex()).build();
	}
}
