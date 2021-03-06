/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgpfoundation.osgp.adapter.ws.da.infra.jms.messageprocessors;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alliander.osgp.domain.core.valueobjects.DeviceFunction;
import com.alliander.osgp.shared.infra.jms.BaseMessageProcessorMap;
import com.alliander.osgp.shared.infra.jms.MessageProcessor;

@Component
public class DomainResponseMessageProcessorMap extends BaseMessageProcessorMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainResponseMessageProcessorMap.class);

    protected DomainResponseMessageProcessorMap() {
        super("domainResponseMessageProcessorMap");
    }

    @Override
    public MessageProcessor getMessageProcessor(final ObjectMessage message) throws JMSException {

        if (message.getJMSType() == null) {
            LOGGER.error("Unknown message type: {}", message.getJMSType());
            throw new JMSException("Unknown message type");
        }

        final DeviceFunction messageType = DeviceFunction.valueOf(message.getJMSType());

        if (messageType.name() == null) {
            LOGGER.error("No device function found for message type: {}", message.getJMSType());
            throw new JMSException("Unknown device function");
        }

        final MessageProcessor messageProcessor = this.messageProcessors.get(messageType.ordinal());
        if (messageType.name() == null) {
            LOGGER.error("No message processor found for message type: {}", message.getJMSType());
            throw new JMSException("Unknown message processor");
        }

        return messageProcessor;
    }
}
