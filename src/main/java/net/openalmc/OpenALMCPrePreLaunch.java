package net.openalmc;

import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.jarboot.JarBooter;
import org.lwjgl.openal.SOFTSourceSpatialize;

import java.net.MalformedURLException;
import java.net.URL;

public class OpenALMCPrePreLaunch implements PrePreLaunch {
    @Override
    public void onPrePreLaunch() {
        String resource = SOFTSourceSpatialize.class.getResource("").toString();

        String toBeRemoved = "!/org/lwjgl/openal/";
        if (resource.endsWith(toBeRemoved)) {
            resource = resource.substring(0, resource.length() - toBeRemoved.length());
        }

        toBeRemoved = "jar:";
        if (resource.startsWith(toBeRemoved)) {
            resource = resource.substring(toBeRemoved.length());
        }

        try {
            JarBooter.addUrl(new URL(resource));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
