package net.openalmc.mixin.doppler.listener;

import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEngineExecutor;
import net.minecraft.world.phys.Vec3;
import net.openalmc.mixin.doppler.MixinContextAccessor;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class MixinSoundSystem {
    @Shadow
    @Final
    private Library library;

    @Shadow
    @Final
    private SoundEngineExecutor executor;

    private Vec3 velocity = new Vec3(0, 0, 0);

    // For some reason Minecraft adds gravity to the velocity, we must remove it
    private final double gravity = 0.0784;

    // For soem reason the velocity is lower than what it really is
    // This multiplier makes it more realistic
    private final double multiplier = 20;

    @Inject(method = "updateSource(Lnet/minecraft/client/Camera;)V",
            at = @At("HEAD")
    )
    private void setListenerVelocity(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null && client.isRunning()) {
            MixinContextAccessor accessor = (MixinContextAccessor) library;
            long contextId = accessor.getContextPointer();
            if (contextId > 0) {
                Vec3 value = client.player.getDeltaMovement();

                velocity = new Vec3(
                        openalmc_gotToTarget(velocity.x, value.x * multiplier),
                        openalmc_gotToTarget(velocity.y, (value.y + gravity) * multiplier),
                        openalmc_gotToTarget(velocity.z, value.z * multiplier)
                );

                this.executor.execute(() -> {
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
