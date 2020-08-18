package net.openalmc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.sound.Source;

@Mixin(Source.class)
public interface MixinSourceInvoker {
    @Invoker("create")
    static Source invokeCreate() {
      throw new RuntimeException("Mixin invoker body somehow got called, this should never happen");
    }
}