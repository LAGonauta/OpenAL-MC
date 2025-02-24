package net.openalmc.mixin.compatibility;

import net.openalmc.OpenALMCMod;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;
import java.util.function.IntFunction;

import static org.lwjgl.system.APIUtil.apiFilterExtensions;

@Mixin(value = ALCapabilities.class, remap = false)
public abstract class MixinAl {
    @Inject(
            method = "<init>",
            at = @At(value = "INVOKE", remap = false, target = "Ljava/util/function/IntFunction;apply(I)Ljava/lang/Object;"),
            remap = false
    )
    private void onAlExt(FunctionProvider provider, Set<String> supportedExtensions, IntFunction<PointerBuffer> bufferFactory, CallbackInfo ci) {
        if (supportedExtensions != null) {
            var list = new String[] {
                    "OpenAL_SOFT_bformat_ex",
                    "AL_EXT_ALAW",
                    "AL_EXT_BFORMAT",
                    "AL_EXT_DOUBLE",
                    "AL_EXT_EXPONENT_DISTANCE",
                    "AL_EXT_FLOAT32",
                    "AL_EXT_IMA4",
                    "AL_EXT_LINEAR_DISTANCE",
                    "AL_EXT_MCFORMATS",
                    "AL_EXT_MULAW",
                    "AL_EXT_MULAW_BFORMAT",
                    "AL_EXT_MULAW_MCFORMATS",
                    "AL_EXT_OFFSET",
                    "AL_EXT_source_distance_model",
                    "AL_EXT_SOURCE_RADIUS",
                    "AL_EXT_static_buffer",
                    "AL_EXT_STEREO_ANGLES",
                    "AL_EXT_vorbis",
                    "AL_LOKI_IMA_ADPCM",
                    "AL_LOKI_quadriphonic",
                    "AL_LOKI_WAVE_format",
                    "AL_SOFT_block_alignment",
                    "AL_SOFT_buffer_samples",
                    "AL_SOFT_buffer_sub_data",
                    "AL_SOFT_deferred_updates",
                    "AL_SOFT_direct_channels",
                    "AL_SOFT_direct_channels_remix",
                    "AL_SOFT_gain_clamp_ex",
                    "AL_SOFT_loop_points",
                    "AL_SOFT_MSADPCM",
                    "AL_SOFT_source_latency",
                    "AL_SOFT_source_length",
                    "AL_SOFT_source_resampler",
                    "AL_SOFT_source_spatialize"
            };
            var IsExtensionPresent = provider.getFunctionAddress("alIsExtensionPresent");
            for (var item : list) {
                if (!supportedExtensions.contains(item)) {
                    try (var stack = MemoryStack.stackPush()) {
                        var result = JNI.invokePZ(MemoryUtil.memAddress(stack.ASCII(item, true)), IsExtensionPresent);
                        OpenALMCMod.LOGGER.debug("Context support for {}: {}", item, result);
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