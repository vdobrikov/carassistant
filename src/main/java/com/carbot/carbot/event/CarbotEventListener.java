package com.carbot.carbot.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */

@Component
public class CarbotEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(CarbotEventListener.class);

    @EventListener
    public void onCarbotEvent(CarbotEvent event) {
        LOG.info("Event: {}", event);
    }
}
