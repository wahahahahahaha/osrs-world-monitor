package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import observers.IRCObservable;

public class App extends Application {

    private static IRCObservable irc = null;

    public static void main(String[] args) {
        launch(args);
    }

    public static void connectIRC(String server, int port, String nickname) {
        irc = new IRCObservable(server, port, nickname);
        irc.start();
    }

    public static void disconnectIRC() {
        irc.disconnect();
    }

    public static void sendMessageIRC(String channel, String message) {
        irc.sendMessage(channel, message);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        primaryStage.setTitle("OSRS World Population Monitor");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("globe.gif")));
        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }

}
