package net.openalmc.compatibility;

import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

public abstract class OpenALCaps {
    // Not thread safe, but ok for now
    public static ALCCapabilities alcCaps = null;
    public static ALCapabilities alCaps = null;
}
