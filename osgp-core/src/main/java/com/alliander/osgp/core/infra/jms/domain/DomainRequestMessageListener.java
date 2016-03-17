/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.core.infra.jms.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alliander.osgp.core.application.services.DeviceRequestMessageService;
import com.alliander.osgp.domain.core.entities.DomainInfo;
import com.alliander.osgp.domain.core.entities.ScheduledTask;
import com.alliander.osgp.domain.core.repositories.ScheduledTaskRepository;
import com.alliander.osgp.shared.exceptionhandling.FunctionalException;
import com.alliander.osgp.shared.infra.jms.Constants;
import com.alliander.osgp.shared.infra.jms.DeviceMessageMetadata;
import com.alliander.osgp.shared.infra.jms.ProtocolRequestMessage;

public class DomainRequestMessageListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainRequestMessageListener.class);

    private final DomainInfo domainInfo;
    private final DeviceRequestMessageService deviceRequestMessageService;
    private final ScheduledTaskRepository scheduledTaskRepository;

    public DomainRequestMessageListener(final DomainInfo domainInfo,
            final DeviceRequestMessageService osgpRequestMessageService,
            final ScheduledTaskRepository scheduledTaskRepository) {
        this.domainInfo = domainInfo;
        this.deviceRequestMessageService = osgpRequestMessageService;
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    @Override
    public void onMessage(final Message message) {
        try {
            LOGGER.info("Received domain request message of type: {} for domain: {} and domainVersion: {}", message
                    .getJMSType().toString(), this.domainInfo.getDomain(), this.domainInfo.getDomainVersion());

            if (message.propertyExists(Constants.SCHEDULE_TIME)) {
                final ScheduledTask scheduledTask = this.createScheduledTask(message);
                this.scheduledTaskRepository.save(scheduledTask);
                LOGGER.info("Scheduled task for device [{}] at [{}] created.", scheduledTask.getDeviceIdentification(),
                        scheduledTask.getscheduledTime());
            } else {
                final ProtocolRequestMessage protocolRequestMessage = this.createProtocolRequestMessage(message);
                this.deviceRequestMessageService.processMessage(protocolRequestMessage);
                LOGGER.info("Domain request for device [{}] processed.",
                        protocolRequestMessage.getDeviceIdentification());
            }
        } catch (final JMSException | FunctionalException e) {
            LOGGER.error("Exception: {}, StackTrace: {}", e.getMessage(), e.getStackTrace(), e);
        }
    }

    public ScheduledTask createScheduledTask(final Message message) throws JMSException {

        final Serializable messageData = ((ObjectMessage) message).getObject();
        final Timestamp scheduleTimeStamp = new Timestamp(message.getLongProperty(Constants.SCHEDULE_TIME));

        final DeviceMessageMetadata deviceMessageMetadata = new DeviceMessageMetadata(
                message.getStringProperty(Constants.DEVICE_IDENTIFICATION),
                message.getStringProperty(Constants.ORGANISATION_IDENTIFICATION), message.getJMSCorrelationID(),
                message.getJMSType(), message.getJMSPriority());

        return new ScheduledTask(deviceMessageMetadata, this.domainInfo.getDomain(),
                this.domainInfo.getDomainVersion(), messageData, scheduleTimeStamp);
    }

    public ProtocolRequestMessage createProtocolRequestMessage(final Message message) throws JMSException {

        final DeviceMessageMetadata deviceMessageMetadata = new DeviceMessageMetadata(
                message.getStringProperty(Constants.DEVICE_IDENTIFICATION),
                message.getStringProperty(Constants.ORGANISATION_IDENTIFICATION), message.getJMSCorrelationID(),
                message.getJMSType(), message.getJMSPriority());

        final String ipAddress = message.getStringProperty(Constants.IP_ADDRESS);
        final Serializable messageData = ((ObjectMessage) message).getObject();

        // @formatter:off
        return new ProtocolRequestMessage.Builder()
        .deviceMessageMetadata(deviceMessageMetadata)
        .domain(this.domainInfo.getDomain())
        .domainVersion(this.domainInfo.getDomainVersion())
        .ipAddress(ipAddress)
        .request(messageData)
        .build();
        // @formatter:on
    }
}
