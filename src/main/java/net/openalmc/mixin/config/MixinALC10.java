package net.openalmc.mixin.config;

import net.fabricmc.loader.api.FabricLoader;
import net.openalmc.OpenALMCMod;
import net.openalmc.config.Config;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(ALC10.class)
public abstract class MixinALC10 {
    @ModifyVariable(method = "alcCreateContext(JLjava/nio/IntBuffer;)J", remap = false, at = @At("HEAD"), argsOnly = true)
    private static IntBuffer customCreateContext(IntBuffer attrList) {
        return IntBuffer.wrap(buildAttrList());
    }


    @ModifyVariable(method = "alcCreateContext(J[I)J", remap = false, at = @At("HEAD"), argsOnly = true)
    private static int[] modifyAttrList(int[] attrList) {
        return buildAttrList();
    }

    @Inject(method = "alcCreateContext(JLjava/nio/IntBuffer;)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateContextIntBuffer(long deviceHandle, IntBuffer attrList, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(deviceHandle);
        OpenALMCMod.LOGGER.info("Error before creating context (IntBuffer): {}", err);
    }

    @Inject(method = "alcCreateContext(J[I)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateContextIntArr(long deviceHandle, int[] attrList, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(deviceHandle);
        OpenALMCMod.LOGGER.info("Error before creating context: {}", err);
    }

    @Inject(method = "alcOpenDevice(Ljava/lang/CharSequence;)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateDeviceCharSequence(CharSequence deviceSpecifier, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(0);
        OpenALMCMod.LOGGER.info("Error before creating device: {}", err);
    }

    @Inject(method = "alcOpenDevice(Ljava/nio/ByteBuffer;)J", remap = false, at = @At("HEAD"))
    private static void resetErrorBeforeCreateDeviceByteBuffer(ByteBuffer deviceSpecifier, CallbackInfoReturnable<Long> cir) {
        var err = ALC10.alcGetError(0);
        OpenALMCMod.LOGGER.info("Error before creating device: {}", err);
    }

    @Unique
    private static int[] buildAttrList() {
        var data = Config.getData();

        var maxSends = data.MaxSends;
        if (FabricLoader.getInstance().isModLoaded("sound_physics_remastered")) {
            OpenALMCMod.LOGGER.info("Sound Physics Remastered is loaded. Setting Max Sends to 4");
            maxSends = 4;
        }

        return new int[]{ ALC10.ALC_FREQUENCY, data.Frequency, EXTEfx.ALC_MAX_AUXILIARY_SENDS, maxSends, 0, 0 };
    }
}
