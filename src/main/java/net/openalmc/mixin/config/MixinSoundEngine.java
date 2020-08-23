package net.openalmc.mixin.config;

import net.minecraft.client.sound.SoundEngine;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.system.MemoryUtil.memUTF8Safe;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
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
                    if (deviceName != null && !deviceName.equals("")) {
                        devices.add(deviceName);
                        deviceNamesPointer += deviceName.length() + 1;
                    }
                } while (deviceName != null && !deviceName.equals(""));

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

        int[] list = new int[]{ ALC10.ALC_FREQUENCY, 48000, EXTEfx.ALC_MAX_AUXILIARY_SENDS, 2, 0 }; // TODO custom from menu

        return ALC10.alcCreateContext(deviceId, list);
    }
}
