package net.openalmc.mixin.doppler.listener;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.util.math.Vec3d;
import net.openalmc.mixin.doppler.MixinContextAccessor;
import org.lwjgl.openal.AL10;
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

    @Shadow
    @Final
    private SoundExecutor taskQueue;

    private Vec3d velocity = new Vec3d(0, 0, 0);

    // For some reason Minecraft adds gravity to the velocity, we must remove it
    private final double gravity = 0.0784;

    // For soem reason the velocity is lower than what it really is
    // This multiplier makes it more realistic
    private final double multiplier = 20;

    @Inject(method = "updateListenerPosition(Lnet/minecraft/client/render/Camera;)V",
            at = @At("HEAD")
    )
    private void setListenerVelocity(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null && client.isRunning()) {
            MixinContextAccessor accessor = (MixinContextAccessor) soundEngine;
            long contextId = accessor.getContextPointer();
            if (contextId > 0) {
                Vec3d value = client.player.getVelocity();

                velocity = new Vec3d(
                        openalmc_gotToTarget(velocity.x, value.x * multiplier),
                        openalmc_gotToTarget(velocity.y, (value.y + gravity) * multiplier),
                        openalmc_gotToTarget(velocity.z, value.z * multiplier)
                );

                this.taskQueue.execute(() -> {
                    AL10.alListenerfv(AL10.AL_VELOCITY, new float[]{
                                    (float) velocity.x,
                                    (float) velocity.y,
                                    (float) velocity.z
                            }
                    );
                });
            }
        }
    }

    // Filter the velocity before applying
    private double openalmc_gotToTarget(double current, double target) {
        return (current * 3 + target) / 4;
    }
}
