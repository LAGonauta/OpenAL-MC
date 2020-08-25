package net.openalmc.mixin.batchcontext;

import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SoundEngine.class)
public interface MixinContextAccessor {
    @Accessor("contextPointer")
    long getContextPointer();
}
