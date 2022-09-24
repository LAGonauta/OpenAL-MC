package net.openalmc.mixin.compatibility;

import net.openalmc.OpenALMCMod;
import net.openalmc.compatibility.OpenALCaps;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AL.class, remap = false)
public abstract class MixinAl {
//    @Inject(
//            method = "createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;",
//            at = @At("HEAD"),
//            remap = false
//    )
//    private static void setAlcCaps(ALCCapabilities alcCaps, CallbackInfoReturnable<ALCapabilities> ci) {
//        OpenALCaps.alcCaps = alcCaps;
//    }
//
//    @Inject(
//            method = "createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;",
//            at = @At("RETURN"),
//            remap = false
//    )
//    private static void setAlCaps(CallbackInfoReturnable<ALCapabilities> cir) {
//        OpenALCaps.alCaps = cir.getReturnValue();
//    }
//
//    @Inject(
//            method = "getICD",
//            at = @At("HEAD"),
//            cancellable = true,
//            remap = false
//    )
//    private static void onGetICD(CallbackInfoReturnable<ALCapabilities> cir) {
//        if (OpenALCaps.alCaps != null) {
//            cir.setReturnValue(OpenALCaps.alCaps);
//        }
//    }
}