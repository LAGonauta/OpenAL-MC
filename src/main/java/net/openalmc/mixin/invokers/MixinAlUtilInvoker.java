package net.openalmc.mixin.invokers;

import com.mojang.blaze3d.audio.OpenAlUtil;
import javax.sound.sampled.AudioFormat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OpenAlUtil.class)
public interface MixinAlUtilInvoker {
    @Invoker("checkErrors")
    static boolean invokeCheckErrors(String sectionName) {
        throw new RuntimeException("Mixin invoker body somehow got called, this should never happen");
    }

    @Invoker("checkAlcErrors")
    static boolean invokeCheckAlcErrors(long devicePointer, String sectionName) {
        throw new RuntimeException("Mixin invoker body somehow got called, this should never happen");
    }

    @Invoker("getFormatId")
    static int invokeGetFormatId(AudioFormat devicePointer) {
        throw new RuntimeException("Mixin invoker body somehow got called, this should never happen");
    }
}
