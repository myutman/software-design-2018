package ru.hse.msg;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Sender {

  ConnectionFactory factory = new ConnectionFactory();
  Connection connection;
  Channel channel;

  public Sender() throws IOException, TimeoutException {
    factory.setHost("localhost");
    connection = factory.newConnection();
    channel = connection.createChannel();
  }

  public void joinChannel(String channelName) throws IOException {
    channel.queueDeclare(channelName, false, false, false, null);
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        MsgProto.Message message = MsgProto.Message.parseFrom(body);
        
      }
    };
    channel.basicConsume(channelName, true, consumer);
  }

  public void writeMessage(String message, String userName, String channelName) throws IOException {
    channel.basicPublish("", channelName, null, MsgProto.Message.newBuilder()
        .setMessageText(message)
        .setUserName(userName)
        .build().toByteArray());
  }

  public void close() throws IOException, TimeoutException {
    channel.close();
    connection.close();
  }
}
