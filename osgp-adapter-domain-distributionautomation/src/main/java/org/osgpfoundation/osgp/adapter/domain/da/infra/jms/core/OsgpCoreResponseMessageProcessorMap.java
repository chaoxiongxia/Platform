/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgpfoundation.osgp.adapter.domain.da.infra.jms.core;

import com.alliander.osgp.domain.core.valueobjects.DeviceFunction;
import com.alliander.osgp.shared.infra.jms.BaseMessageProcessorMap;
import com.alliander.osgp.shared.infra.jms.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

@Component("domainDistributionAutomationOsgpCoreResponseMessageProcessorMap")
public class OsgpCoreResponseMessageProcessorMap extends BaseMessageProcessorMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgpCoreResponseMessageProcessorMap.class);

    public OsgpCoreResponseMessageProcessorMap() {
        super("OsgpCoreResponseMessageProcessorMap");
    }

    @Override
    public MessageProcessor getMessageProcessor(final ObjectMessage message) throws JMSException {

        if (message.getJMSType() == null) {
            LOGGER.error("Unknown message type: {}", message.getJMSType());
            throw new JMSException("Unknown message type");
        }

        final DeviceFunction messageType = DeviceFunction.valueOf(message.getJMSType());

        if (messageType.name() == null) {
            LOGGER.error("No message processor found for message type: {}", message.getJMSType());
            throw new JMSException("Unknown message processor");
        }

        final MessageProcessor mp = this.messageProcessors.get(messageType.ordinal());
        if (mp == null) {
            LOGGER.error("No message processor found for message type: {}, key {}", message.getJMSType(),
                    messageType.ordinal());
            throw new JMSException("Unknown message processor");
        }
        return mp;
    }
}
