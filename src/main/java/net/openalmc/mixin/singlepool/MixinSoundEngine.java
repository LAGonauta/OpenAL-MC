package net.openalmc.mixin.singlepool;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.Source;
import net.openalmc.OpenALMCMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
    @Shadow protected abstract int getMonoSourceCount();

    @Shadow private SoundEngine.SourceSet staticSources;

    @Shadow private SoundEngine.SourceSet streamingSources;

    @Redirect(
            method = "init",
            at = @At(value = "NEW", target = "Lnet/minecraft/client/sound/SoundEngine$SourceSetImpl;")
    )
    private SoundEngine.SourceSetImpl setSourceNumber(int sourceNumber) {
        return new SoundEngine.SourceSetImpl(getMonoSourceCount());
    }

    /**
     * @author LAGonauta
     * @reason Only one source pool
     */
    @Overwrite
    public String getDebugString() {
        return String.format("Sounds: %d/%d", this.streamingSources.getSourceCount() + this.staticSources.getSourceCount(), this.streamingSources.getMaxSourceCount());
    }

    /**
     * @author LAGonauta
     * @reason We use only one source pool now
     */
    @Overwrite
    public Source createSource(SoundEngine.RunMode mode) {
        if (mode == SoundEngine.RunMode.STREAMING) {
            MixinSourceSetAccessor accessor1 = (MixinSourceSetAccessor)this.streamingSources;
            if (this.streamingSources.getSourceCount() + this.staticSources.getSourceCount() >= this.streamingSources.getMaxSourceCount()) {
                OpenALMCMod.LOGGER.warn("Dropping static source to make way for streaming source.");
                MixinSourceSetAccessor accessor = (MixinSourceSetAccessor)this.streamingSources;
                this.streamingSources.release(accessor.getSourceSet().iterator().next());
            }
            return this.staticSources.createSource();
        }
        return this.streamingSources.createSource();
    }
}
