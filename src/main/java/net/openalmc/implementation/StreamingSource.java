package net.openalmc.implementation;

import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Source;
import net.openalmc.OpenALMCMod;
import net.openalmc.mixin.MixinAlUtilInvoker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

import javax.sound.sampled.AudioFormat;

import com.google.common.collect.Sets;

import org.lwjgl.openal.AL10;

public class StreamingSource extends Source {
    private int pointer;
    private Set<Integer> buffered = Sets.newHashSet();
    private boolean aborted = false;

    public StreamingSource(int pointer) {
        super(pointer);
        this.pointer = pointer;
    }

    @Override
    public void setStream(AudioStream stream) {
        this.stream = stream;
        AudioFormat audioFormat = stream.getFormat();
        this.bufferSize = getBufferSize(audioFormat, 1);

        int numBuffers = 4;
        if (this.stream != null) {
            try {
                ArrayList<Integer> buffers = new ArrayList<Integer>();
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

    @Override
    public void stop() {
        super.stop();
        aborted = true;
    }

    @Override
    public void close() {
        super.close();
        for(int buffer : buffered) {
            if (AL10.alIsBuffer(buffer)) {
                AL10.alDeleteBuffers(buffer);
            }
            MixinAlUtilInvoker.invokeCheckErrors("Deleting stream buffer");
        }
    }

    @Override
    public boolean isStopped() {
        return super.isStopped() || aborted;
    }

    @Override
    public void tick() {
        if (this.stream != null) {
            int processedBuffers = AL10.alGetSourcei(this.pointer, AL10.AL_BUFFERS_PROCESSED);
            if (processedBuffers > 0) {
                int[] bufferIds = new int[processedBuffers];
                AL10.alSourceUnqueueBuffers(this.pointer, bufferIds);
                MixinAlUtilInvoker.invokeCheckErrors("Unqueue buffers");

                try {
                    ArrayList<Integer> buffers = new ArrayList<Integer>();
                    for (int i = 0; i < bufferIds.length; ++i) {
                        int b = bufferData(bufferIds[i]);
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