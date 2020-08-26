package net.openalmc.mixin.doppler;

import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Source.class)
public interface MixinSourceAccessor {
    @Accessor("pointer")
    int getPointer();
}
