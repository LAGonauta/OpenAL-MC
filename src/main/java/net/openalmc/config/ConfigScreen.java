package net.openalmc.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ConfigScreen implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            final var builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("openalmc.config.title"));
            final var entryBuilder = builder.entryBuilder();

            builder.setSavingRunnable(Config::saveData);

            final var data = Config.getData();

            final var settings = builder.getOrCreateCategory(Text.translatable("openalmc.config.category"));
            {
                final var deviceDropdownMenu = entryBuilder.startDropdownMenu(
                        Text.translatable("openalmc.config.devices"),
                        DropdownMenuBuilder.TopCellElementBuilder.of("".equals(data.DeviceName) ? Config.getDevices().get(0) : data.DeviceName, Function.identity()),
                        DropdownMenuBuilder.CellCreatorBuilder.of()
                )
                        .requireRestart()
                        .setDefaultValue(Config.getDevices().get(0))
                        .setSelections(Config.getDevices())
                        .setSaveConsumer(device -> data.DeviceName = device);

                settings.addEntry(deviceDropdownMenu.build());
            }

            settings.addEntry(entryBuilder.startIntField(Text.translatable("openalmc.config.frequency"), data.Frequency)
                    .setDefaultValue(48000)
                    .requireRestart()
                    .setMin(8000)
                    .setMax(192000)
                    .setSaveConsumer(newValue -> data.Frequency = newValue).build()
            );

            settings.addEntry(entryBuilder.startIntField(Text.translatable("openalmc.config.maxsends"), data.MaxSends)
                    .setDefaultValue(2)
                    .requireRestart()
                    .setMin(2)
                    .setMax(16) // TODO: depends on implementation
                    .setSaveConsumer(newValue -> data.MaxSends = newValue).build()
            );

            settings.addEntry(entryBuilder.startFloatField(Text.translatable("openalmc.config.dopplerfactor"), data.DopplerFactor)
                    .setDefaultValue(1.0f)
                    .requireRestart()
                    .setMin(0.1f)
                    .setMax(10.0f)
                    .setSaveConsumer(newValue -> data.DopplerFactor = newValue).build()
            );

            return builder.build();
        };
    }
}
