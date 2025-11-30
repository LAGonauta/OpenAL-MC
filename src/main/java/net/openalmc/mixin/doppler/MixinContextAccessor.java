package net.openalmc.mixin.doppler;

import com.mojang.blaze3d.audio.Library;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Library.class)
public interface MixinContextAccessor {
    @Accessor("contextPointer")
    long getContextPointer();
}
