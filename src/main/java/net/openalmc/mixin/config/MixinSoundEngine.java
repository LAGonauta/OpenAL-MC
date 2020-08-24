package net.openalmc.mixin.config;

import net.minecraft.client.sound.SoundEngine;
import net.openalmc.config.Config;
import net.openalmc.config.ConfigModel;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
    @Redirect(
            method = "openDevice",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcOpenDevice(Ljava/nio/ByteBuffer;)J", remap = false)
    )
    private static long openNamedDevice(ByteBuffer buffer) {
        if (buffer == null) {
            ConfigModel data = Config.getData();
            if (!data.DeviceName.equals("")) {
                return ALC10.alcOpenDevice(data.DeviceName);
            }
        }

        return ALC10.alcOpenDevice(buffer);
    }

    @Redirect(
            method = "init",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcCreateContext(JLjava/nio/IntBuffer;)J", remap = false)
    )
    private long setContextAttributes(long deviceId, IntBuffer attrList) {

        ConfigModel data = Config.getData();

        int[] list = new int[]{ ALC10.ALC_FREQUENCY, data.Frequency, EXTEfx.ALC_MAX_AUXILIARY_SENDS, data.MaxSends, 0 };

        return ALC10.alcCreateContext(deviceId, list);
    }
}
