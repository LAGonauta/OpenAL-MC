package net.openalmc.config;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.text.TranslatableText;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memUTF8Safe;

public class ConfigScreen implements ModMenuApi {
    private int currentFrequency = 48000;
    private int currentMaxSends = 2;
    private String currentDevice = "";
    private static List<String> devices = getDevices();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create();
            builder.setParentScreen(parent);
            builder.setTitle(new TranslatableText("openalmc.config.title"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigCategory settings = builder.getOrCreateCategory(new TranslatableText("openalmc.config.category"));
            {
                DropdownMenuBuilder<String> deviceDropdownMenu = entryBuilder.startDropdownMenu(
                        new TranslatableText("openalmc.config.devices"),
                        DropdownMenuBuilder.TopCellElementBuilder.of(currentDevice == "" ? devices.get(0) : currentDevice, (val) -> val),
                        DropdownMenuBuilder.CellCreatorBuilder.of()
                )
                        .requireRestart()
                        .setDefaultValue(devices.get(0))
                        .setSelections(devices)
                        .setSaveConsumer(device -> currentDevice = device);

                settings.addEntry(deviceDropdownMenu.build());
            }

            settings.addEntry(entryBuilder.startIntField(new TranslatableText("openalmc.config.frequency"), currentFrequency)
                    .setDefaultValue(48000)
                    .requireRestart()
                    .setMin(8000)
                    .setMax(192000)
                    .setSaveConsumer(newValue -> currentFrequency = newValue)
                    .setTooltip(new TranslatableText("openalmc.config.frequency.tooltip")).build()
            );

            settings.addEntry(entryBuilder.startIntField(new TranslatableText("openalmc.config.maxsends"), currentMaxSends)
                    .setDefaultValue(2)
                    .requireRestart()
                    .setMin(2)
                    .setMax(16) // TODO: depends on implementation
                    .setSaveConsumer(newValue -> currentMaxSends = newValue)
                    .setTooltip(new TranslatableText("openalmc.config.maxsends.tooltip")).build()
            );

            return builder.build();
        };
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
