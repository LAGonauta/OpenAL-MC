package net.openalmc.implementation;

import net.openalmc.OpenALMCMod;
import org.lwjgl.system.APIUtil;

public class FixApiVersion {
    public static APIUtil.APIVersion apiParseVersion(String version) {

        OpenALMCMod.LOGGER.info("Rewriting OpenAL API version");

        int i = 0;
        for (int end = version.length(); i < end; ++i) {
            if (Character.isDigit(version.charAt(i))) {
                break;
            }
        }

        version = version.substring(i);

        return APIUtil.apiParseVersion(version);
    }
}
