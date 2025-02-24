package net.openalmc.mixin.config;

import net.minecraft.client.sound.SoundEngine;
import net.openalmc.config.Config;
import net.openalmc.config.ConfigModel;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
    @Inject(
            method = "init",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;", remap = false)
    )
    private void setDopplerFactor(CallbackInfo ci) {
        ConfigModel data = Config.getData();

        AL10.alDopplerFactor(data.DopplerFactor);
    }
}
