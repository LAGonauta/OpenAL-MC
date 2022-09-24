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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;
import java.util.function.IntFunction;

@Mixin(value = AL.class, remap = false)
public abstract class MixinAl {
    @Inject(
            method = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;Ljava/util/function/IntFunction;)Lorg/lwjgl/openal/ALCapabilities;",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/APIUtil;apiFilterExtensions(Ljava/util/Set;Lorg/lwjgl/system/Configuration;)V"),
            remap = false,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void onAlExt(ALCCapabilities alcCaps, IntFunction<PointerBuffer> bufferFactory, CallbackInfoReturnable<ALCapabilities> cir,
                              long alGetProcAddress, FunctionProvider functionProvider, long GetString, long GetError,
                              long IsExtensionPresent, String versionString, APIUtil.APIVersion apiVersion, int majorVersion,
                              int minorVersion, int AL_VERSIONS[][], Set<String> supportedExtensions, String extensionsString) {
        if (supportedExtensions != null) {
            var list = new String[] {
                    "OpenAL_SOFT_bformat_ex",
                    "AL_EXT_ALAW",
                    "AL_EXT_BFORMAT",
                    "AL_EXT_DOUBLE",
                    "ALC_EXT_EFX",
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
            for (var item : list) {
                try (var stack = MemoryStack.stackPush()) {
                    var result = JNI.invokePZ(MemoryUtil.memAddress(stack.ASCII(item, true)), IsExtensionPresent);
                    OpenALMCMod.LOGGER.debug("Context support for {}: {}", item, result);
                    if (result) {
                        supportedExtensions.add(item);
                    }
                }
                supportedExtensions.add(item);
            }
        }
    }
}