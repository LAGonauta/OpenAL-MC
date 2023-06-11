package net.openalmc.mixin.compatibility;

import net.openalmc.OpenALMCMod;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.IntFunction;

@Mixin(value = ALCCapabilities.class, remap = false)
public abstract class MixinAlc {
    @Inject(
            method = "<init>",
            at = @At(value= "INVOKE", remap = false, target = "Ljava/util/function/IntFunction;apply(I)Ljava/lang/Object;"),
            remap = false
    )
    private void onAlcExt(FunctionProviderLocal provider, long device, Set<String> supportedExtensions,
                                 IntFunction<PointerBuffer> bufferFactory, CallbackInfo ci) {
        if (supportedExtensions != null) {
            var list = new String[] {
                    "ALC_ENUMERATE_ALL_EXT",
                    "ALC_ENUMERATION_EXT",
                    "ALC_EXT_CAPTURE",
                    "ALC_EXT_EFX",
                    "ALC_EXT_DEDICATED",
                    "ALC_EXT_DEFAULT_FILTER_ORDER",
                    "ALC_EXT_disconnect",
                    "ALC_EXT_thread_local_context",
                    "ALC_LOKI_audio_channel",
                    "ALC_SOFT_device_clock",
                    "ALC_SOFT_HRTF",
                    "ALC_SOFT_loopback",
                    "ALC_SOFT_output_limiter",
                    "ALC_SOFT_pause_device"
            };
            var IsExtensionPresent = provider.getFunctionAddress("alcIsExtensionPresent");
            for (var item : list) {
                if (!supportedExtensions.contains(item)) {
                    try (var stack = MemoryStack.stackPush()) {
                        var result = JNI.invokePPZ(device, MemoryUtil.memAddress(stack.ASCII(item, true)), IsExtensionPresent);
                        OpenALMCMod.LOGGER.info("Device {} support for {}: {}", device, item, result);
                        if (result) {
                            supportedExtensions.add(item);
                        }
                    }
                }
            }

            APIUtil.apiFilterExtensions(supportedExtensions, Configuration.OPENAL_EXTENSION_FILTER);
        }
    }
}
