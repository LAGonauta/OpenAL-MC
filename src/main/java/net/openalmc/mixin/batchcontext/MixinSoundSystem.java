package net.openalmc.mixin.batchcontext;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundSystem;
import org.lwjgl.openal.ALC10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public abstract class MixinSoundSystem {
    @Shadow
    @Final
    private SoundEngine soundEngine;

    @Inject(method = "tick(Z)V", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        MixinContextAccessor accessor = (MixinContextAccessor) soundEngine;
        long contextId = accessor.getContextPointer();
        if (contextId > 0) {
            ALC10.alcSuspendContext(contextId);
        }
    }

    @Inject(method = "tick(Z)V", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        MixinContextAccessor accessor = (MixinContextAccessor) soundEngine;
        long contextId = accessor.getContextPointer();
        if (contextId > 0) {
            ALC10.alcProcessContext(contextId);
        }
    }
}
