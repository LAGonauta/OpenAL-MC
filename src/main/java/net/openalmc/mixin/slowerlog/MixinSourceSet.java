package net.openalmc.mixin.slowerlog;

import net.openalmc.OpenALMCMod;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.sound.SoundEngine;

import java.time.Duration;
import java.time.LocalDateTime;

@Mixin(SoundEngine.SourceSetImpl.class)
public class MixinSourceSet {
    private LocalDateTime lastLogged = LocalDateTime.now();

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