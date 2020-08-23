package net.openalmc.mixin.singlepool;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(SoundEngine.SourceSetImpl.class)
public interface MixinSourceSetAccessor {
    @Accessor("sources")
    Set<Source> getSourceSet();
}
