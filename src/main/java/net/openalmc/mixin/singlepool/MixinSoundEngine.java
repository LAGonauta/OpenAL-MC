package net.openalmc.mixin.singlepool;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {
    @Shadow
    private SoundEngine.SourceSet streamingSources;

    @Shadow
    private SoundEngine.SourceSet staticSources;

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;", remap = false)
    )
    private void sameSets(CallbackInfo ci) {
        //this.staticSources = this.streamingSources;
    }

    /**
     * @author LAGonauta
     * @reason Only one sourcep pool
     */
    //@Overwrite
    public String getDebugString() {
        return String.format("Sounds: %d/%d", this.streamingSources.getSourceCount(), this.streamingSources.getMaxSourceCount());
    }


    /**
     * @author LAGonauta
     * @reason We use only one source pool now
     */
    //@Overwrite
    public Source createSource(SoundEngine.RunMode mode) {
        return this.streamingSources.createSource();
    }
}
