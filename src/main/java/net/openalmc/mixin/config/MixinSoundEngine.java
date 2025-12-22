package net.openalmc.mixin.config;

import com.mojang.blaze3d.audio.Library;
import net.fabricmc.loader.api.FabricLoader;
import net.openalmc.OpenALMCMod;
import net.openalmc.config.Config;
import net.openalmc.config.ConfigModel;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Library.class)
public abstract class MixinSoundEngine {
    @Shadow
    private long currentDevice;

    @Shadow
    private long context;

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;", remap = false)
    )
    private void setDopplerFactor(CallbackInfo ci) {
        ConfigModel data = Config.getData();

        AL10.alDopplerFactor(data.DopplerFactor);
    }

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/OpenAlUtil;checkALCError(JLjava/lang/String;)Z", ordinal = 1)
    )
    private void recreateContext(CallbackInfo ci) {
        if (this.context > 0) {
            ALC10.alcDestroyContext(this.context);
            this.context = 0;
        }
        this.context = ALC10.alcCreateContext(currentDevice, buildAttrList());
    }

    @Unique
    private static int[] buildAttrList() {
        var data = Config.getData();

        var maxSends = data.MaxSends;
        if (FabricLoader.getInstance().isModLoaded("sound_physics_remastered")) {
            OpenALMCMod.LOGGER.info("Sound Physics Remastered is loaded. Setting Max Sends to 4");
            maxSends = 4;
        }

        return new int[]{ALC10.ALC_FREQUENCY, data.Frequency, EXTEfx.ALC_MAX_AUXILIARY_SENDS, maxSends, 0};
    }
}
