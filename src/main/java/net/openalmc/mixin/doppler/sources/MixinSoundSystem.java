package net.openalmc.mixin.doppler.sources;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.phys.Vec3;
import net.openalmc.mixin.doppler.MixinContextAccessor;
import net.openalmc.mixin.doppler.MixinSourceAccessor;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import com.mojang.blaze3d.audio.Library;
import java.util.Iterator;

@Mixin(SoundEngine.class)
public abstract class MixinSoundSystem {
    @Shadow
    @Final
    private Library library;

    private Vec3 lastSourcePos = new Vec3(0, 0, 0);

    private long lastTime;
    private long currentTime;

    @Inject(
            method = "tickInGameSound()V",
            at = @At("HEAD")
    )
    private void onTickStart(CallbackInfo ci) {
        currentTime = System.nanoTime();
    }

    @Inject(
            method = "tickInGameSound()V",
            at = @At("RETURN")
    )
    private void onTickEnd(CallbackInfo ci) {
        lastTime = System.nanoTime();
    }

    @Inject(
            method = "tickInGameSound()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/TickableSoundInstance;tick()V", ordinal = 0)
    )
    private void onBeforeSourceTick(CallbackInfo ci, @Local TickableSoundInstance tickableSoundInstance) {
        lastSourcePos = new Vec3(tickableSoundInstance.getX(), tickableSoundInstance.getY(), tickableSoundInstance.getZ());
    }

    @Inject(
            method = "tickInGameSound()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", ordinal = 0)
    )
    private void onSourceSetProperties(CallbackInfo ci, @Local TickableSoundInstance tickableSoundInstance, @Local ChannelAccess.ChannelHandle sourceManager) {
        MixinContextAccessor accessor = (MixinContextAccessor) library;
        long contextId = accessor.getContextPointer();
        if (contextId > 0) {
            long tickDuration = currentTime - lastTime;
            if (tickDuration > 0) {
                double velocityX = (tickableSoundInstance.getX() - lastSourcePos.x());
                double velocityY = (tickableSoundInstance.getY() - lastSourcePos.y());
                double velocityZ = (tickableSoundInstance.getZ() - lastSourcePos.z());
                if (velocityX != 0 || velocityY != 0 || velocityZ != 0) {
                    double tickDurationSeconds = (currentTime - lastTime) / 1_000_000_000.0;
                    float[] velocity = new float[]{
                            (float) (velocityX / tickDurationSeconds),
                            (float) (velocityY / tickDurationSeconds),
                            (float) (velocityZ / tickDurationSeconds)
                    };

                    sourceManager.execute(source -> {
                        MixinSourceAccessor sourceAccessor = (MixinSourceAccessor) source;
                        AL10.alSourcefv(sourceAccessor.getPointer(), AL10.AL_VELOCITY, velocity);
                    });
                }
            }
        }
    }
}
