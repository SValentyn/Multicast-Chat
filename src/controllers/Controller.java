package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import logic.EDTInvocationHandler;
import interfaces.Messenger;
import logic.MessengerImpl;
import interfaces.UITask;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Manage graphical application window.
 */
public class Controller implements Initializable {

    @FXML
    private TextField inputMsg;

    @FXML
    private TextField addressField;

    @FXML
    private TextField portField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea textArea;

    @FXML
    private Button buttonSend;

    @FXML
    private Button buttonConnect;

    @FXML
    private Button buttonDisconnect;

    @FXML
    private Button buttonClear;

    @FXML
    private Button buttonExit;

    private Messenger messenger = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addressField.setText("224.0.0.1");
        portField.setText("10100");
        nameField.setPromptText("User");
        inputMsg.setPromptText("Enter message");
        nameField.requestFocus();
        textArea.setEditable(false);
        buttonSend.setDisable(true);
        buttonDisconnect.setDisable(true);

        inputMsg.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER") & !(buttonSend.isDisable())) {
                actionSend();
            }
        });
    }

    @FXML
    public void actionSend() {
        messenger.send();
        inputMsg.requestFocus();
    }

    @FXML
    public void actionConnect() {
        UITask ui = (UITask) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{UITask.class},
                new EDTInvocationHandler(new UITaskImpl()));
        messenger = new MessengerImpl(addressField.getText().trim(), portField.getText().trim(), nameField.getText().trim(), ui);
        messenger.start();

        inputMsg.requestFocus();
        buttonSend.setDisable(false);
        buttonConnect.setDisable(true);
        buttonDisconnect.setDisable(false);
        addressField.setDisable(true);
        portField.setDisable(true);
        nameField.setDisable(true);
    }

    @FXML
    public void actionDisconnect() {
        messenger.stop();

        buttonSend.setDisable(true);
        buttonConnect.setDisable(false);
        buttonDisconnect.setDisable(true);
        addressField.setDisable(false);
        portField.setDisable(false);
        nameField.setDisable(false);
        nameField.requestFocus();
    }

    @FXML
    public void actionClear() {
        textArea.clear();
        if (buttonConnect.isDisable()) inputMsg.requestFocus();
        if (buttonDisconnect.isDisable()) nameField.requestFocus();
    }

    @FXML
    public void actionExit() {
        if (messenger != null) messenger.stop();
        Platform.exit();
        System.exit(0);
    }

    class UITaskImpl implements UITask {
        @Override
        public String getMessage() {
            String message = inputMsg.getText();
            inputMsg.clear();
            return message;
        }

        @Override
        public void setText(String message) {
            textArea.appendText(message + "\n");
        }
    }

}
