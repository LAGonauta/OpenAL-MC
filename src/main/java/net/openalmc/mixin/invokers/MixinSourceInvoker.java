package net.openalmc.mixin.invokers;

import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Channel.class)
public interface MixinSourceInvoker {
    @Invoker("create")
    static Channel invokeCreate() {
      throw new RuntimeException("Mixin invoker body somehow got called, this should never happen");
    }
}