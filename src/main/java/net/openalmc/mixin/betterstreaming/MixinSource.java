package net.openalmc.mixin.betterstreaming;

import com.google.common.collect.Sets;
import net.minecraft.client.sound.AudioStream;
import net.openalmc.OpenALMCMod;
import net.openalmc.mixin.invokers.MixinAlUtilInvoker;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

@Mixin(Source.class)
public abstract class MixinSource {
    @Shadow
    @Final
    private int pointer;

    @Shadow
    private AudioStream stream;

    @Shadow
    private int bufferSize = 16384;

    @Shadow
    private static int getBufferSize(AudioFormat format, int time) {
        throw new AssertionError("Should not be called");
    }

    private final Set<Integer> buffered = Sets.newHashSet();
    private boolean aborted = false;

    @Inject(method = "stop", at = @At("HEAD"))
    private void stop(CallbackInfo ci) {
        aborted = true;
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void onClose(CallbackInfo ci) {
        for(int buffer : buffered) {
            if (AL10.alIsBuffer(buffer)) {
                AL10.alDeleteBuffers(buffer);
            }
            MixinAlUtilInvoker.invokeCheckErrors("Deleting stream buffer");
        }
    }

    @Inject(
            method = "isStopped",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onIsStopped(CallbackInfoReturnable<Boolean> cir) {
        if (aborted && !cir.getReturnValue()) {
            cir.setReturnValue(aborted);
        }
    }

    /**
     * @author LAGonauta
     * @reason Reuse buffers on streaming
     */
    @Overwrite
    public void setStream(AudioStream stream) {
        this.stream = stream;
        AudioFormat audioFormat = stream.getFormat();
        this.bufferSize = getBufferSize(audioFormat, 1);

        int numBuffers = 4;
        if (this.stream != null) {
            try {
                ArrayList<Integer> buffers = new ArrayList<>();
                for (int index = 0; index < numBuffers; ++index) {
                    int bufferId = bufferData(0);
                    if (bufferId > 0) {
                        buffers.add(bufferId);
                    }
                }
                if (buffers.size() > 0) {
                    int[] finalBuffers = new int[buffers.size()];
                    for (int index = 0, end = buffers.size(); index < end; ++index) {
                        finalBuffers[index] = buffers.get(index);
                    }
                    buffered.addAll(buffers);
                    AL10.alSourceQueueBuffers(this.pointer, finalBuffers);
                }
            } catch (IOException ex) {
                OpenALMCMod.LOGGER.error("Failed to read from audio stream", ex);
            }
        }
    }

    /**
     * @author LAGonauta
     * @reason Reuse buffer on streaming
     */
    @Overwrite
    public void tick() {
        if (this.stream != null) {
            int processedBuffers = AL10.alGetSourcei(this.pointer, AL10.AL_BUFFERS_PROCESSED);
            if (processedBuffers > 0) {
                int[] bufferIds = new int[processedBuffers];
                AL10.alSourceUnqueueBuffers(this.pointer, bufferIds);
                MixinAlUtilInvoker.invokeCheckErrors("Unqueue buffers");

                try {
                    ArrayList<Integer> buffers = new ArrayList<>();
                    for (int bufferId : bufferIds) {
                        int b = bufferData(bufferId);
                        if (b > 0) {
                            buffers.add(b);
                        }
                    }
                    if (buffers.size() > 0) {
                        int[] finalBuffers = new int[buffers.size()];
                        for (int index = 0, end = buffers.size(); index < end; ++index) {
                            finalBuffers[index] = buffers.get(index);
                        }
                        AL10.alSourceQueueBuffers(this.pointer, finalBuffers);
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                int state = AL10.alGetSourcei(this.pointer, AL10.AL_SOURCE_STATE);
                if (state == AL10.AL_INITIAL) {
                    int queued = AL10.alGetSourcei(this.pointer, AL10.AL_BUFFERS_QUEUED);
                    if (queued > 0) {
                        OpenALMCMod.LOGGER.info("Source overrun: " + this.pointer + ". It had " + queued + " buffers. New state: " + AL10.alGetSourcei(this.pointer, AL10.AL_SOURCE_STATE));
                        AL10.alSourcePlay(this.pointer);
                    } else {
                        AL10.alSourceStop(this.pointer);
                        aborted = true;
                    }
                }
            }
        }
    }

    private int bufferData(int bufferId) throws IOException {
        ByteBuffer byteBuffer = this.stream.getBuffer(this.bufferSize);
        if (byteBuffer != null && byteBuffer.remaining() > 0) {
            //OpenALMCMod.LOGGER.info("Buffering for " + pointer + " : " + bufferId + ".");
            if (bufferId == 0) {
                bufferId = AL10.alGenBuffers();
            }

            if (MixinAlUtilInvoker.invokeCheckErrors("Creating buffer")) {
                return 0;
            }

            AudioFormat format = this.stream.getFormat();
            int formatId = MixinAlUtilInvoker.invokeGetFormatId(format);
            AL10.alBufferData(bufferId, formatId, byteBuffer, (int) format.getSampleRate());
            MixinAlUtilInvoker.invokeCheckErrors("Assigning buffer data");

            return bufferId;
        }

        return 0;
    }
}
