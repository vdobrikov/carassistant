package com.carassistant.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Component
public class CarAssistantEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(CarAssistantEventListener.class);

    @EventListener
    public void onCarAssistantEvent(CarAssistantEvent event) {
        LOG.info("Event: {}", event);
    }
}
