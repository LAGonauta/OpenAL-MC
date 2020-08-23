package net.openalmc.mixin;

import net.minecraft.client.sound.Source;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Source.class)
public class MixinSourceCompatibility {
    @Redirect(
            method = "Lnet/minecraft/client/sound/Source;setAttenuation(F)V",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL10;alSourcei(III)V", ordinal = 0, remap = false)
    )
    private void disableSetSourceDistanceModel(int source, int param, int value) {
        // NOOP
    }

    @Redirect(
            method = "Lnet/minecraft/client/sound/Source;disableAttenuation()V",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL10;alSourcei(III)V", remap = false)
    )
    private void disableAttenuation(int source, int param, int value) {
        AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, value);
    }
}
