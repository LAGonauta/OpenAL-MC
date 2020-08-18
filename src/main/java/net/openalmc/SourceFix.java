package net.openalmc;

import org.lwjgl.openal.AL10;

import net.minecraft.client.sound.Source;
import net.minecraft.client.sound.SoundEngine.RunMode;
import net.openalmc.implementation.StaticSource;
import net.openalmc.implementation.StreamingSource;
import net.openalmc.mixin.MixinAlUtilInvoker;

public class SourceFix {

    public static Source create(RunMode mode) {
        int sourceId = AL10.alGenSources();
        if (MixinAlUtilInvoker.invokeCheckErrors("Allocate new source")) {
            return null;
        }
        return mode == RunMode.STREAMING ? new StreamingSource(sourceId) : new StaticSource(sourceId);
    }
}