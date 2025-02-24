package net.openalmc.config;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memUTF8Safe;

public class Config {
    private static List<String> devices;

    private static ConfigModel data = new ConfigModel();

    private static File getConfigFile() {
        return new File("./config/openalmc/data.obj");
    }

    public static ConfigModel getData() {
        return data;
    }

    public static void loadData() {
        try {
            final var file = getConfigFile();
            if (file.exists()) {
                FileInputStream fileInputStream
                        = new FileInputStream(file);
                ObjectInputStream objectInputStream
                        = new ObjectInputStream(fileInputStream);
                data = (ConfigModel) objectInputStream.readObject();
                objectInputStream.close();
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        final var file = getConfigFile();
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            final var fileOutputStream
                    = new FileOutputStream(file, false);
            final var objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
