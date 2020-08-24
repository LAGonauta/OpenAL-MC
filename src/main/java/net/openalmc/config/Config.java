package net.openalmc.config;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memUTF8Safe;

public class Config {
    public static List<String> Devices = getDevices();

    private static ConfigModel data = new ConfigModel();

    private static File getConfigFile() {
        return new File("./config/openalmc/data.obj");
    }

    public static ConfigModel getData() {
        return data;
    }

    public static void loadData() {
        try {
            File file = getConfigFile();
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

        if (!Devices.contains(data.DeviceName)) {
            data.DeviceName = "";
        }
    }

    public static void saveData() {
        File file = getConfigFile();
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
            FileOutputStream fileOutputStream
                    = new FileOutputStream(file, false);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getDevices() {
        ArrayList<String> devices = new ArrayList<>();
        long deviceNamesPointer = 0;
        if (ALC10.alcIsExtensionPresent(0, "ALC_ENUMERATION_EXT")) {
            if (ALC10.alcIsExtensionPresent(0, "ALC_ENUMERATE_ALL_EXT")) {
                deviceNamesPointer = ALC10.nalcGetString(0, ALC11.ALC_ALL_DEVICES_SPECIFIER);
            } else {
                deviceNamesPointer = ALC10.nalcGetString(0, ALC10.ALC_DEVICE_SPECIFIER);
            }
        }

        if (deviceNamesPointer > 0) {
            String deviceName = "";
            do {
                deviceName = memUTF8Safe(deviceNamesPointer);
                if (deviceName != null && !deviceName.equals("")) {
                    devices.add(deviceName);
                    deviceNamesPointer += deviceName.length() + 1;
                }
            } while (deviceName != null && !deviceName.equals(""));
        }

        return devices;
    }
}
