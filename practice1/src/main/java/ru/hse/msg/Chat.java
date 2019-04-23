package ru.hse.msg;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class Chat extends MessageSenderGrpc.MessageSenderImplBase {
    private io.grpc.Server server;
    private MessageSenderGrpc.MessageSenderStub stub;

    public Chat(int ourPort, int port, String host) {
        server = ServerBuilder.forPort(ourPort).addService(this).build();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        stub = MessageSenderGrpc.newStub(channel);
    }

    public void sendMessage(String userName, String message) {
        stub.send(MsgProto.Message.newBuilder()
                .setMessageText(message)
                .setUserName(userName)
                .build(), new StreamObserver<MsgProto.MessageResponse>() {
            @Override
            public void onNext(MsgProto.MessageResponse value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

        @Override
        public void send(MsgProto.Message request, StreamObserver<MsgProto.MessageResponse> responseObserver) {
            Msg.showNewMSG(request.getMessageText(), request.getUserName());
            responseObserver.onNext(MsgProto.MessageResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }

}
