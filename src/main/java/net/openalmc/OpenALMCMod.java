package net.openalmc;

import net.openalmc.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;

public class OpenALMCMod implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("OpenALMCMod");

	@Override
	public void onInitializeClient() {

		LOGGER.info("Initializing OpenALMCMod");

		Config.loadData();
	}
}
