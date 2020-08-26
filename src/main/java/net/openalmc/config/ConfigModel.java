package net.openalmc.config;

import java.io.Serializable;

public class ConfigModel implements Serializable {
    public String DeviceName = "";
    public Integer MaxSends = 2;
    public Integer Frequency = 48000;
    public float DopplerFactor = 1.0f;
}
