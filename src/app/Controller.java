package app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import observers.PopulationObservable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private TextField serverField;
    @FXML
    private TextField portField;
    @FXML
    private TextField nicknameField;
    @FXML
    private TextField channelField;
    @FXML
    private Button connectButton;
    @FXML
    private ToggleButton pauseButton;
    @FXML
    private ListView<String> trackerListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        assert serverField != null : "fx:id=\"serverField\" was not injected.";
        assert portField != null : "fx:id=\"portField\" was not injected.";
        assert nicknameField != null : "fx:id=\"nicknameField\" was not injected.";
        assert channelField != null : "fx:id=\"channelField\" was not injected.";
        assert connectButton != null : "fx:id=\"connectButton\" was not injected.";
        assert pauseButton != null : "fx:id=\"pauseButton\" was not injected.";
        assert trackerListView != null : "fx:id=\"trackerListView\" was not injected.";

        try {
            loadIRCProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LinkedList<String> changes = new LinkedList<>();
        PopulationObservable worldTracker = new PopulationObservable();
        worldTracker.addObserver((o, world, change) -> {
            if (change > 0) {
                changes.addFirst("World " + world.getWorldId() + " :: +" + change);
            } else {
                changes.addFirst("World " + world.getWorldId() + " :: " + change);
            }
            if (changes.size() > 16) {
                changes.pollLast();
            }
            Platform.runLater(() -> {
                App.sendMessageIRC(channelField.getText().trim(), changes.peekFirst());
                trackerListView.setItems(FXCollections.observableArrayList(changes));
            });
        });
        worldTracker.start();

        connectButton.setOnAction(event -> {
            if (connectButton.getText().equals("Connect")) {
                System.out.println("Connecting...");
                connectButton.setText("Disconnect");
                try {
                    saveIRCProperties();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                App.connectIRC(
                        serverField.getText().trim(),
                        Integer.parseInt(portField.getText().trim()),
                        nicknameField.getText().trim()
                );
                worldTracker.resumeTracking();
                serverField.setEditable(false);
                portField.setEditable(false);
                nicknameField.setEditable(false);
                channelField.setEditable(false);
            } else {
                App.disconnectIRC();
                serverField.setEditable(true);
                portField.setEditable(true);
                nicknameField.setEditable(true);
                channelField.setEditable(true);
                connectButton.setText("Connect");
            }
        });

        pauseButton.setOnAction(event -> {
            if (pauseButton.isSelected()) {
                pauseButton.setText("Resume");
                worldTracker.pauseTracking();
            } else {
                pauseButton.setText("Pause");
                worldTracker.resumeTracking();
            }
        });
    }

    private void loadIRCProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(getPropertiesFile()));
        serverField.setText(properties.getProperty("server", ""));
        portField.setText(properties.getProperty("port", ""));
        nicknameField.setText(properties.getProperty("nickname", ""));
        channelField.setText(properties.getProperty("channel", ""));
    }

    private void saveIRCProperties() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("server", serverField.getText());
        properties.setProperty("port", portField.getText());
        properties.setProperty("nickname", nicknameField.getText());
        properties.setProperty("channel", channelField.getText());
        properties.store(new FileOutputStream(getPropertiesFile()), null);

    }

    private File getPropertiesFile() {
        File properties = new File(System.getProperty("user.dir") + File.separator + "irc.properties");
        if (!properties.exists()) {
            try {
                if (properties.createNewFile()) {
                    return getPropertiesFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                properties = null;
            }
        }
        return properties;
    }
}
