package net.openalmc.mixin.config;

import net.fabricmc.loader.api.FabricLoader;
import net.openalmc.OpenALMCMod;
import net.openalmc.config.Config;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

@Mixin(ALC10.class)
public abstract class MixinALC10 {
    @ModifyVariable(method = "alcCreateContext(JLjava/nio/IntBuffer;)J", remap = false, at = @At("HEAD"), argsOnly = true)
    private static IntBuffer customCreateContext(IntBuffer attrList) {
        var data = Config.getData();

        var maxSends = data.MaxSends;
        if (FabricLoader.getInstance().isModLoaded("sound_physics_remastered")) {
            OpenALMCMod.LOGGER.info("Sound Physics Remastered is loaded. Setting Max Sends to 4");
            maxSends = 4;
        }
        return IntBuffer.wrap(new int[]{ ALC10.ALC_FREQUENCY, data.Frequency, EXTEfx.ALC_MAX_AUXILIARY_SENDS, maxSends, 0, 0 });
    }

    @Inject(method = "alcCreateContext(JLjava/nio/IntBuffer;)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateContextIntBuffer(long deviceHandle, IntBuffer attrList, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(deviceHandle);
        OpenALMCMod.LOGGER.info("Error before creating context (IntBuffer): {}", err);
    }

    @ModifyVariable(method = "alcCreateContext(J[I)J", remap = false, at = @At("HEAD"), argsOnly = true)
    private static int[] modifyAttrList(int[] attrList) {
        var data = Config.getData();

        var maxSends = data.MaxSends;
        if (FabricLoader.getInstance().isModLoaded("sound_physics_remastered")) {
            OpenALMCMod.LOGGER.info("Sound Physics Remastered is loaded. Setting Max Sends to 4");
            maxSends = 4;
        }

        return new int[]{ ALC10.ALC_FREQUENCY, data.Frequency, EXTEfx.ALC_MAX_AUXILIARY_SENDS, maxSends, 0, 0 };
    }

    @Inject(method = "alcCreateContext(J[I)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateContextIntArr(long deviceHandle, int[] attrList, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(deviceHandle);
        OpenALMCMod.LOGGER.info("Error before creating context: {}", err);
    }

    @ModifyVariable(method = "alcOpenDevice(Ljava/lang/CharSequence;)J", remap = false, at = @At("HEAD"), argsOnly = true)
    private static CharSequence modifyDevice(CharSequence deviceName) {
        if (deviceName == null) {
            var data = Config.getData();
            if (!"".equals(data.DeviceName)) {
                return data.DeviceName;
            }
        }

        return deviceName;
    }

    @Inject(method = "alcOpenDevice(Ljava/lang/CharSequence;)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateDeviceCharSequence(CharSequence deviceSpecifier, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(0);
        OpenALMCMod.LOGGER.info("Error before creating device: {}", err);
    }

    @ModifyVariable(method = "alcOpenDevice(Ljava/nio/ByteBuffer;)J", remap = false, at = @At("HEAD"), argsOnly = true)
    private static ByteBuffer modifyDevice(ByteBuffer deviceName) {
        if (deviceName == null) {
            var data = Config.getData();
            if (!"".equals(data.DeviceName)) {
                return ByteBuffer.wrap(data.DeviceName.getBytes(StandardCharsets.UTF_8));
            }
        }

        return deviceName;
    }

    @Inject(method = "alcOpenDevice(Ljava/nio/ByteBuffer;)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateDeviceByteBuffer(ByteBuffer deviceSpecifier, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(0);
        OpenALMCMod.LOGGER.info("Error before creating device: {}", err);
    }
}
