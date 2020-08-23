package net.openalmc.mixin;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.openal.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.sound.SoundEngine;
import net.openalmc.OpenALMCMod;

import static org.lwjgl.system.MemoryUtil.memUTF8Safe;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngineCompatibility {
    @ModifyArg(
            method = "init",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 1),
            index = 2
    )
    private int modifyMaxSources(int maxSources) {
        return 1024;
    }

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lorg/lwjgl/openal/AL;createCapabilities(Lorg/lwjgl/openal/ALCCapabilities;)Lorg/lwjgl/openal/ALCapabilities;", remap = false)
    )
    private void setLinearDistanceModel(CallbackInfo ci) {
        AL11.alDistanceModel(AL11.AL_LINEAR_DISTANCE);
    }

    // Do not check for extensions
    @Inject(
            method = "init",
            at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/sound/AlUtil;checkErrors(Ljava/lang/String;)Z"),
            cancellable = true
    )
    private void returnFromMethod(CallbackInfo ci) {
        OpenALMCMod.LOGGER.info("Removing extension check");
        ci.cancel();
    }

    @Redirect(
            method = "openDevice",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcOpenDevice(Ljava/nio/ByteBuffer;)J", remap = false)
    )
    private static long openNamedDevice(ByteBuffer buffer) {
        if (buffer == null) {
            long deviceNamesPointer = 0;
            if (ALC10.alcIsExtensionPresent(0, "ALC_ENUMERATION_EXT")) {
                if (ALC10.alcIsExtensionPresent(0, "ALC_ENUMERATE_ALL_EXT")) {
                    deviceNamesPointer = ALC10.nalcGetString(0, ALC11.ALC_ALL_DEVICES_SPECIFIER);
                } else {
                    deviceNamesPointer = ALC10.nalcGetString(0, ALC10.ALC_DEVICE_SPECIFIER);
                }
            }

            if (deviceNamesPointer > 0) {
                ArrayList<String> devices = new ArrayList<>();
                String deviceName = "";
                do {
                    deviceName = memUTF8Safe(deviceNamesPointer);
                    if (!deviceName.equals("")) {
                        devices.add(deviceName);
                        deviceNamesPointer += deviceName.length() + 1;
                    }
                } while (!deviceName.equals(""));

                if (devices.size() > 0) {
                    return ALC10.alcOpenDevice(devices.get(0)); // TODO: custom from menu
                }
            }
        }

        return ALC10.alcOpenDevice(buffer);
    }

    @Redirect(
            method = "init",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcCreateContext(JLjava/nio/IntBuffer;)J", remap = false)
    )
    private long setContextAttributes(long deviceId, IntBuffer attrList) {

        int[] list = new int[]{ ALC10.ALC_FREQUENCY, 48000, EXTEfx.ALC_MAX_AUXILIARY_SENDS, 2, 0 };

        return ALC10.alcCreateContext(deviceId, list);
    }
}
