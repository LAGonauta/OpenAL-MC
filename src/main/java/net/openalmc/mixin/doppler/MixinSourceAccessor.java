package net.openalmc.mixin.doppler;

import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Channel.class)
public interface MixinSourceAccessor {
    @Accessor("source")
    int getPointer();
}
