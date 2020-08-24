package net.openalmc.config;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.text.TranslatableText;

public class ConfigScreen implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create();
            builder.setParentScreen(parent);
            builder.setTitle(new TranslatableText("openalmc.config.title"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            builder.setSavingRunnable(Config::saveData);

            ConfigModel data = Config.getData();

            ConfigCategory settings = builder.getOrCreateCategory(new TranslatableText("openalmc.config.category"));
            {
                DropdownMenuBuilder<String> deviceDropdownMenu = entryBuilder.startDropdownMenu(
                        new TranslatableText("openalmc.config.devices"),
                        DropdownMenuBuilder.TopCellElementBuilder.of(data.DeviceName.equals("") ? Config.Devices.get(0) : data.DeviceName, (val) -> val),
                        DropdownMenuBuilder.CellCreatorBuilder.of()
                )
                        .requireRestart()
                        .setDefaultValue(Config.Devices.get(0))
                        .setSelections(Config.Devices)
                        .setSaveConsumer(device -> data.DeviceName = device);

                settings.addEntry(deviceDropdownMenu.build());
            }

            settings.addEntry(entryBuilder.startIntField(new TranslatableText("openalmc.config.frequency"), data.Frequency)
                    .setDefaultValue(48000)
                    .requireRestart()
                    .setMin(8000)
                    .setMax(192000)
                    .setSaveConsumer(newValue -> data.Frequency = newValue).build()
            );

            settings.addEntry(entryBuilder.startIntField(new TranslatableText("openalmc.config.maxsends"), data.MaxSends)
                    .setDefaultValue(2)
                    .requireRestart()
                    .setMin(2)
                    .setMax(16) // TODO: depends on implementation
                    .setSaveConsumer(newValue -> data.MaxSends = newValue).build()
            );

            return builder.build();
        };
    }
}
