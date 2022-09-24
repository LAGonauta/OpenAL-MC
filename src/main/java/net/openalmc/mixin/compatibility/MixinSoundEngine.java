package net.openalmc.mixin.compatibility;

import org.lwjgl.openal.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.sound.SoundEngine;
import net.openalmc.OpenALMCMod;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
    @ModifyArg(
            method = "init",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 1),
            index = 2
    )
    private int modifyMaxSources(int maxSources) {
        return 1024;
    }

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;", remap = false)
    )
    private void setLinearDistanceModel(CallbackInfo ci) {
        AL11.alDistanceModel(AL11.AL_LINEAR_DISTANCE);
    }

    // Do not check for extensions
    @Inject(
            method = "init",
            at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/sound/AlUtil;checkErrors(Ljava/lang/String;)Z"),
            cancellable = true
    )
    private void returnFromMethod(CallbackInfo ci) {
        OpenALMCMod.LOGGER.info("Removing extension check");
        OpenALMCMod.LOGGER.info(
                "OpenAL initialized on device {}",
                ((SoundEngine)(Object)this).getCurrentDeviceName()
        );
        ci.cancel();
    }
}
