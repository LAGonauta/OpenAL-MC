package net.openalmc.mixin.compatibility;

import net.openalmc.OpenALMCMod;
import net.openalmc.compatibility.OpenALCaps;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

import static org.lwjgl.system.JNI.invokePPZ;

@Mixin(value = ALC.class, remap = false)
public abstract class MixinAlc {
//    @Inject(method = "getICD", at = @At("HEAD"), cancellable = true, remap = false)
//    private static void onGetICD(CallbackInfoReturnable<ALCCapabilities> cir) {
//        if (OpenALCaps.alcCaps != null) {
//            cir.setReturnValue(OpenALCaps.alcCaps);
//        }
//    }

    @ModifyArg(
            method = "createCapabilities(JLjava/util/function/IntFunction;)Lorg/lwjgl/openal/ALCCapabilities;",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/APIUtil;apiFilterExtensions(Ljava/util/Set;Lorg/lwjgl/system/Configuration;)V"),
            remap = false
    )
    private static Set<String> onExt(final Set<String> extensions) {
        if (extensions != null) {
            var list = new String[] {"ALC_ENUMERATE_ALL_EXT", "ALC_ENUMERATION_EXT", "ALC_EXT_CAPTURE", "ALC_EXT_EFX"};
            for (var item : list) {
                OpenALMCMod.LOGGER.info("Assuming driver has {} support", item);
                extensions.add(item);
            }
        }
        return extensions;
    }
}
