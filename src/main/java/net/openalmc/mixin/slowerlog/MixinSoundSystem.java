package net.openalmc.mixin.slowerlog;

import net.minecraft.client.sound.SoundSystem;
import net.openalmc.OpenALMCMod;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.time.Duration;
import java.time.LocalDateTime;

@Mixin(SoundSystem.class)
public abstract class MixinSoundSystem {
    private LocalDateTime lastLogged = LocalDateTime.now();

    @Redirect(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V", remap = false)
    )
    private void slowLogger(Logger logger, String message) {
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(lastLogged, now).compareTo(Duration.ofSeconds(1)) > 0) {
            OpenALMCMod.LOGGER.warn(message);
            lastLogged = LocalDateTime.now();
        }
    }
}
