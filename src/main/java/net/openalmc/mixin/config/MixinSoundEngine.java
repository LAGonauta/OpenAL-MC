package net.openalmc.mixin.config;

import net.minecraft.client.sound.SoundEngine;
import net.openalmc.config.Config;
import net.openalmc.config.ConfigModel;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
    @Redirect(
            method = "openDevice",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcOpenDevice(Ljava/lang/CharSequence;)J", remap = false)
    )
    private static long openNamedDevice(CharSequence deviceName) {
        if (deviceName == null) {
            ConfigModel data = Config.getData();
            if (!"".equals(data.DeviceName)) {
                return ALC10.alcOpenDevice(data.DeviceName);
            }
        }

        return ALC10.alcOpenDevice(deviceName);
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

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;", remap = false)
    )
    private void setLinearDistanceModel(CallbackInfo ci) {
        ConfigModel data = Config.getData();

        AL10.alDopplerFactor(data.DopplerFactor);
    }
}
