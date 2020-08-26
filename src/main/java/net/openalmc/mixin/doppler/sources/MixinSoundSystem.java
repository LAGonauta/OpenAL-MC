package net.openalmc.mixin.doppler.sources;

import net.minecraft.client.sound.*;
import net.minecraft.util.math.Vec3d;
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

import java.util.Iterator;

@Mixin(SoundSystem.class)
public abstract class MixinSoundSystem {
    @Shadow
    @Final
    private SoundEngine soundEngine;

    private Vec3d lastSourcePos = new Vec3d(0, 0, 0);

    private long lastTime;
    private long currentTime;

    @Inject(
            method = "tick()V",
            at = @At("HEAD")
    )
    private void onTickStart(CallbackInfo ci) {
        currentTime = System.nanoTime();
    }

    @Inject(
            method = "tick()V",
            at = @At("RETURN")
    )
    private void onTickEnd(CallbackInfo ci) {
        lastTime = System.nanoTime();
    }

    @Inject(
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/TickableSoundInstance;tick()V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onBeforeSourceTick(CallbackInfo ci, Iterator var1, TickableSoundInstance tickableSoundInstance) {
        lastSourcePos = new Vec3d(tickableSoundInstance.getX(), tickableSoundInstance.getY(), tickableSoundInstance.getZ());
    }

    @Inject(
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/Channel$SourceManager;run(Ljava/util/function/Consumer;)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onSourceSetProperties(CallbackInfo ci, Iterator var1, TickableSoundInstance tickableSoundInstance, float f, float g, Vec3d vec3d, Channel.SourceManager sourceManager) {
        MixinContextAccessor accessor = (MixinContextAccessor) soundEngine;
        long contextId = accessor.getContextPointer();
        if (contextId > 0) {
            long tickDuration = currentTime - lastTime;
            if (tickDuration > 0) {
                double velocityX = (tickableSoundInstance.getX() - lastSourcePos.getX());
                double velocityY = (tickableSoundInstance.getY() - lastSourcePos.getY());
                double velocityZ = (tickableSoundInstance.getZ() - lastSourcePos.getZ());
                if (velocityX != 0 || velocityY != 0 || velocityZ != 0) {
                    double tickDurationSeconds = (currentTime - lastTime) / 1_000_000_000.0;
                    float[] velocity = new float[]{
                            (float) (velocityX / tickDurationSeconds),
                            (float) (velocityY / tickDurationSeconds),
                            (float) (velocityZ / tickDurationSeconds)
                    };

                    sourceManager.run(source -> {
                        MixinSourceAccessor sourceAccessor = (MixinSourceAccessor) source;
                        AL10.alSourcefv(sourceAccessor.getPointer(), AL10.AL_VELOCITY, velocity);
                    });
                }
            }
        }
    }
}
