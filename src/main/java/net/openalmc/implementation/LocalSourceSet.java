package net.openalmc.implementation;

import net.minecraft.client.sound.SoundEngine.RunMode;
import net.minecraft.client.sound.SoundEngine.SourceSetImpl;

public class LocalSourceSet extends SourceSetImpl {
    public RunMode runMode;

    public LocalSourceSet(int maxSourceCount, RunMode runMode) {
        super(maxSourceCount);
        this.runMode = runMode;
    }
}