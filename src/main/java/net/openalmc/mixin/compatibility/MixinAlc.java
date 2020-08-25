package net.openalmc.mixin.compatibility;

import net.openalmc.compatibility.OpenALCaps;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ALC.class, remap = false)
public abstract class MixinAlc {
    @Inject(method = "getICD", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onGetICD(CallbackInfoReturnable<ALCCapabilities> cir) {
        if (OpenALCaps.alcCaps != null) {
            cir.setReturnValue(OpenALCaps.alcCaps);
        }
    }
}
