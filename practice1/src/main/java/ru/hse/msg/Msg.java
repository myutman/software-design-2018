package ru.hse.msg;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Msg extends Application {

    private static VBox root = new VBox();
    private static TextField text;
    private static TextField userName;
    private static ListView<String> msgs;
    private static Button destroyUSA;
    private static Chat chat;
    private static ObservableList<String> messages = FXCollections.observableArrayList();

    public static void main(String[] args) {
        int ourPort = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        String host = args[2];
        chat = new Chat(ourPort, port, host);
        launch(args);
    }

    public static void showNewMSG(String msg, String usr){
        messages.add(usr + " | " + msg);
    }

    @Override
    public void start(Stage primaryStage) {
        root.setSpacing(10);
        destroyUSA = new Button("Send");

        text = new TextField();
        userName = new TextField();
        destroyUSA.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                chat.sendMessage(text.getText(), userName.getText());
            }
        });
        msgs = new ListView<String>(messages);
        root.getChildren().add(msgs);
        root.getChildren().add(text);
        root.getChildren().add(userName);
        root.getChildren().add(destroyUSA);
        primaryStage.setTitle("Go fuck yourself!! )))");
        Scene scene = new Scene(root, 450, 200);
        primaryStage.setScene(scene);
        showNewMSG("1231", "32131231231");
        primaryStage.show();
    }
}
