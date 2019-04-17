package ru.hse.msg;

import io.grpc.stub.StreamObserver;

public class MessageSenderService extends MessageSenderGrpc.MessageSenderImplBase {
    @Override
    public void send(MsgProto.Message request, StreamObserver<MsgProto.MessageResponse> responseObserver) {
        
    }
}
