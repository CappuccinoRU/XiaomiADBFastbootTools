package app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class XiaomiADBFastbootTools extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void createFile(String file, boolean exec) {
        File temp = new File(System.getProperty("user.home") + "/temp");
        temp.mkdir();
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(file));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (file.lastIndexOf("/") != -1)
            file = file.substring(file.lastIndexOf("/") + 1);
        File newfile = new File(System.getProperty("user.home") + "/temp/" + file);
        if (!newfile.exists()) {
            try {
                newfile.createNewFile();
                FileOutputStream fos = new FileOutputStream(newfile);
                fos.write(bytes);
                fos.flush();
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        newfile.setExecutable(exec, false);
    }

    public void setupFiles() {
        String os = System.getProperty("os.name").toLowerCase();
        createFile("res/dummy.img", false);
        if (os.contains("win")) {
            createFile("res/windows/adb.exe", true);
            createFile("res/windows/fastboot.exe", true);
            createFile("res/windows/AdbWinApi.dll", false);
            createFile("res/windows/AdbWinUsbApi.dll", false);
        }
        if (os.contains("mac")) {
            createFile("res/macos/adb", true);
            createFile("res/macos/fastboot", true);
        }
        if (os.contains("linux")) {
            createFile("res/linux/adb", true);
            createFile("res/linux/fastboot", true);
        }
        Thread t = new Thread(() -> {
            new Command().exec("adb start-server");
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void start(Stage stage) throws Exception {
        setupFiles();

        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Xiaomi ADB/Fastboot Tools");
        stage.getIcons().add(new Image(this.getClass().getClassLoader().getResource("res/icon.png").toString()));
        stage.show();
        stage.setResizable(false);

        if (!new File(System.getProperty("user.home") + "/temp/adb").exists() && !new File(System.getProperty("user.home") + "/temp/adb.exe").exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fatal Error");
            alert.setHeaderText("ERROR: Couldn't initialise ADB!");
            alert.showAndWait();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        new Command().exec("adb kill-server");
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File(System.getProperty("user.home") + "/temp"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
