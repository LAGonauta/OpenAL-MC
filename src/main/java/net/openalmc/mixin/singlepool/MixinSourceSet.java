package net.openalmc.mixin.singlepool;

import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoundEngine.SourceSetImpl.class)
public class MixinSourceSet {
}
