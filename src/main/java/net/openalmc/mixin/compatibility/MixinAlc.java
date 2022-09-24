package net.openalmc.mixin.compatibility;

import net.openalmc.OpenALMCMod;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.FunctionProviderLocal;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;
import java.util.function.IntFunction;

@Mixin(value = ALC.class, remap = false)
public abstract class MixinAlc {
    @Inject(
            method = "createCapabilities(JLjava/util/function/IntFunction;)Lorg/lwjgl/openal/ALCCapabilities;",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/APIUtil;apiFilterExtensions(Ljava/util/Set;Lorg/lwjgl/system/Configuration;)V"),
            remap = false,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void onAlcExt(long device, IntFunction<PointerBuffer> bufferFactory, CallbackInfoReturnable<ALCCapabilities> cir,
                              FunctionProviderLocal functionProvider, long GetIntegerv, long GetString, long IsExtensionPresent,
                              int majorVersion, int minorVersion, int ALC_VERSIONS[][], Set<String> supportedExtensions) {

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
            for (var item : list) {
                try (var stack = MemoryStack.stackPush()) {
                    var result = JNI.invokePPZ(device, MemoryUtil.memAddress(stack.ASCII(item, true)), IsExtensionPresent);
                    OpenALMCMod.LOGGER.debug("Device {} support for {}: {}", device, item, result);
                    if (result) {
                        supportedExtensions.add(item);
                    }
                }
                supportedExtensions.add(item);
            }
        }
    }
}
