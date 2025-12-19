package net.openalmc;

import net.openalmc.config.Config;
import org.apache.logging.log4j.*;

import net.fabricmc.api.ClientModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

class PrefixedLogger extends ExtendedLoggerWrapper {
    private final String prefix;

    private PrefixedLogger(ExtendedLogger logger, String name, String prefix) {
        super(logger, name, logger.getMessageFactory());
        this.prefix = "[" + prefix + "] ";
    }

    // Static factory method to wrap a standard logger
    public static Logger create(Logger logger, String prefix) {
        return new PrefixedLogger((ExtendedLogger) logger, logger.getName(), prefix);
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        // This is the "choke point" where all log calls eventually pass through
        // We wrap the original message with our prefix
        String prefixedMsg = prefix + message.getFormattedMessage();

        // Use the MessageFactory to create a new message with the prefix
        Message newMessage = getMessageFactory().newMessage(prefixedMsg);

        super.logMessage(fqcn, level, marker, newMessage, t);
    }
}

public class OpenALMCMod implements ClientModInitializer {
	public static final Logger LOGGER = PrefixedLogger.create(
            LogManager.getLogger("OpenALMCMod"),
            "OpenALMC"
    );

	@Override
	public void onInitializeClient() {

		LOGGER.info("Initializing OpenALMCMod");

		Config.loadData();
	}
}
