package net.openalmc.mixin.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.sound.SoundEngine;
import net.openalmc.OpenALMCMod;
import net.openalmc.config.Config;
import net.openalmc.config.ConfigModel;
import net.openalmc.mixin.invokers.MixinAlUtilInvoker;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
    @Shadow private long contextPointer;

    @Shadow private long devicePointer;

    @Redirect(
            method = "openDevice",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcOpenDevice(Ljava/lang/CharSequence;)J", remap = false)
    )
    private static long openNamedDevice(CharSequence deviceName) {
        if (deviceName == null) {
            var data = Config.getData();
            if (!"".equals(data.DeviceName)) {
                return ALC10.alcOpenDevice(data.DeviceName);
            }
        }

        return ALC10.alcOpenDevice(deviceName);
    }

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", remap = false, target = "Lorg/lwjgl/openal/ALC10;alcMakeContextCurrent(J)Z", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void setAttr(String deviceSpecifier, boolean directionalAudio, CallbackInfo ci, ALCCapabilities aLCCapabilities) {
        if (this.contextPointer != 0) {
            ALC10.alcDestroyContext(this.contextPointer);
            this.contextPointer = 0;
        }

        var data = Config.getData();

        var maxSends = data.MaxSends;
        if (FabricLoader.getInstance().isModLoaded("sound_physics_remastered")) {
            OpenALMCMod.LOGGER.info("Sound Physics Remastered is loaded. Setting Max Sends to 4");
            maxSends = 4;
        }

        var list = new int[]{ ALC10.ALC_FREQUENCY, data.Frequency, EXTEfx.ALC_MAX_AUXILIARY_SENDS, maxSends, 0, 0 };

        this.contextPointer = ALC10.alcCreateContext(this.devicePointer, list);

        if (MixinAlUtilInvoker.invokeCheckAlcErrors(this.devicePointer, "creating context on device ")) {
            OpenALMCMod.LOGGER.error("Some error creating context, continuing anyway. Context handle: {}", this.contextPointer);
        }
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
