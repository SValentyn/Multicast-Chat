package logic;

import interfaces.Messenger;
import interfaces.UITask;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Implements the main chat functionality.
 */
public class MessengerImpl implements Messenger {

    private InetAddress address;
    private int port;
    private String name;
    private UITask UI;
    private MulticastSocket group;
    private boolean canceled = false;

    public MessengerImpl(String address, String port, String name, UITask UI) {
        try {
            this.address = InetAddress.getByName(address);
            this.port = Integer.parseInt(port);
            this.name = name;
            this.UI = UI;
            group = new MulticastSocket(this.port);
            group.setTimeToLive(5);
            group.joinGroup(this.address);
        } catch (IOException e) {
            showErrorDialog("The host is invalid or unavailable\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        new Receiver().start();
    }

    @Override
    public void stop() {
        cancel();
        try {
            group.leaveGroup(address);
        } catch (IOException e) {
            showErrorDialog(("Join error\n" + e.getMessage()));
        } finally {
            group.close();
        }
    }

    @Override
    public void send() {
        new Sender().start();
    }

    private class Receiver extends Thread {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (!isCanceled()) {
                    group.receive(packet);
                    UI.setText(getCurrentTime() + new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                if (isCanceled()) {
                    showInfoDialog("Connection completed correctly");
                } else {
                    showErrorDialog("Error receiving data\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private class Sender extends Thread {
        @Override
        public void run() {
            try {
                String message = " <" + name + ">: " + UI.getMessage();
                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                group.send(packet);
            } catch (IOException e) {
                showErrorDialog("Error sending data\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private synchronized String getCurrentTime() {
        LocalDateTime time = LocalDateTime.now();  // current date and time
        return ((time.getHour() < 10) ? "0" + time.getHour() : time.getHour()) + ":"
                + ((time.getMinute() < 10) ? "0" + time.getMinute() : time.getMinute()) + ":"
                + ((time.getSecond() < 10) ? "0" + time.getSecond() : time.getSecond());
    }

    private synchronized boolean isCanceled() {
        return canceled;
    }

    private synchronized void cancel() {
        canceled = true;
    }

    private static void showInfoDialog(String info) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info Dialog");
            alert.setHeaderText("INFO: " + info);
            alert.setContentText(null);
            alert.setResizable(false);
            Stage mainStage = (Stage) alert.getDialogPane().getScene().getWindow();
            mainStage.getIcons().add(new Image(MessengerImpl.class.getResource("/decor/icon.png").toString()));
            alert.show();
        });
    }

    private static void showErrorDialog(String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("ERROR: " + error);
            alert.setContentText(null);
            alert.setResizable(false);
            Stage mainStage = (Stage) alert.getDialogPane().getScene().getWindow();
            mainStage.getIcons().add(new Image(MessengerImpl.class.getResource("/decor/icon.png").toString()));
            alert.show();
        });
    }
}
