package net.openalmc.mixin.doppler.listener;

import net.minecraft.client.render.Camera;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Mixin(SoundSystem.class)
public abstract class MixinSoundSystem {
    @Shadow
    @Final
    private SoundEngine soundEngine;

    @Shadow
    @Final
    private SoundExecutor taskQueue;

    private Vec3d lastSourcePos = new Vec3d(0, 0, 0);
    private Vec3d lastListenerPos = new Vec3d(0, 0, 0);
    private long lastListenerTime = 0;
    private final int QUEUE_SIZE = 10;
    private Queue<Vec3d> listenerVelocityQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);


    @Inject(method = "updateListenerPosition(Lnet/minecraft/client/render/Camera;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/Camera;getPos()Lnet/minecraft/util/math/Vec3d;"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void setListenerVelocity(Camera camera, CallbackInfo ci, Vec3d vec3d) {
        MixinContextAccessor accessor = (MixinContextAccessor) soundEngine;
        long contextId = accessor.getContextPointer();
        if (contextId > 0) {
            this.taskQueue.execute(() -> {
                long currentTime = System.nanoTime();
                double tickDurationSeconds = (currentTime - lastListenerTime) / 1_000_000_000.0;
                Vec3d velocity = vec3d.subtract(lastListenerPos);
                if (listenerVelocityQueue.size() >= QUEUE_SIZE) {
                    listenerVelocityQueue.poll();
                }
                listenerVelocityQueue.add(new Vec3d(velocity.x / tickDurationSeconds, velocity.y / tickDurationSeconds,velocity.z / tickDurationSeconds));

                Optional<Vec3d> value = listenerVelocityQueue
                        .stream()
                        .reduce((left, right) -> new Vec3d(left.x + right.x, left.y + right.y, left.z + right.z));

                if (value.isPresent()) {
                    value = Optional.of(new Vec3d(value.get().x / listenerVelocityQueue.size(), value.get().y / listenerVelocityQueue.size(), value.get().z / listenerVelocityQueue.size()));
                } else {
                    value = Optional.of(velocity);
                }

                AL10.alListenerfv(AL10.AL_VELOCITY, new float[]{
                        (float) value.get().x,
                        (float) value.get().y,
                        (float) value.get().z}
                );

                lastListenerPos = vec3d;
                lastListenerTime = currentTime;
            });
        }
    }
}
