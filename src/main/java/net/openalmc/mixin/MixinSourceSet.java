package net.openalmc.mixin;

import net.openalmc.OpenALMCMod;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.Source;
import net.minecraft.client.sound.SoundEngine.RunMode;
import net.openalmc.SourceFix;
import net.openalmc.implementation.LocalSourceSet;

import java.time.Duration;
import java.time.LocalDateTime;

@Mixin(SoundEngine.SourceSetImpl.class)
public class MixinSourceSet {
    private LocalDateTime lastLogged = LocalDateTime.now();

    @Redirect(
            method = "createSource()Lnet/minecraft/client/sound/Source;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/Source;create()Lnet/minecraft/client/sound/Source;")
    )
    private Source createSingleSource() {
        Object thisObject = (Object) this;
        if (thisObject instanceof LocalSourceSet) {
            RunMode runMode = ((LocalSourceSet) thisObject).runMode;
            return SourceFix.create(runMode);
        }
        return SourceFix.create(RunMode.STATIC);
    }

    @Redirect(
            method = "createSource()Lnet/minecraft/client/sound/Source;",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false)
    )
    private void slowLogger(Logger logger, String message, Object obj) {
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(lastLogged, now).compareTo(Duration.ofSeconds(1)) > 0) {
            OpenALMCMod.LOGGER.warn(message, obj);
            lastLogged = LocalDateTime.now();
        }
    }

}