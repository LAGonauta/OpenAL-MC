package net.openalmc.mixin;

import net.openalmc.OpenALMCMod;
import org.lwjgl.openal.AL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AL.class, remap = false)
public abstract class MixinAl {
    @ModifyArg(
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/APIUtil;apiParseVersion(Ljava/lang/String;)Lorg/lwjgl/system/APIUtil$APIVersion;", ordinal = 0, remap = false),
            method = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;",
            remap = false
    )
    private static String fixVersionString(String versionString) {
        OpenALMCMod.LOGGER.info("Rewriting OpenAL API version");

        int i = 0;
        for (int end = versionString.length(); i < end; ++i) {
            if (Character.isDigit(versionString.charAt(i))) {
                break;
            }
        }

        versionString = versionString.substring(i);
        return versionString;
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/EXTThreadLocalContext;alcGetThreadContext()J", ordinal = 0, remap = false),
            method = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;",
            remap = false
    )
    private static long disableThreadContext() {
        OpenALMCMod.LOGGER.info("Disabling ThreadContext");
        return 0; // NULL
    }
}