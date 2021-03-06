/**
 * Copyright 2017 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.osgpfoundation.osgp.adapter.domain.da.infra.jms.ws.messageprocessors;

import com.alliander.osgp.domain.core.valueobjects.DeviceFunction;
import com.alliander.osgp.shared.infra.jms.Constants;
import org.osgpfoundation.osgp.adapter.domain.da.application.services.MonitoringService;
import org.osgpfoundation.osgp.adapter.domain.da.infra.jms.ws.AbstractWebServiceRequestMessageProcessor;
import org.osgpfoundation.osgp.domain.da.valueobjects.GetPQValuesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Class for processing da get pq values request messages
 */
@Component("domainDistributionAutomationGetPQValuesRequestMessageProcessor")
public class GetPQValuesRequestMessageProcessor extends AbstractWebServiceRequestMessageProcessor {
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetPQValuesRequestMessageProcessor.class);

    @Autowired
    @Qualifier("domainDistributionAutomationMonitoringService")
    private MonitoringService monitoringService;

    public GetPQValuesRequestMessageProcessor() {
        super(DeviceFunction.GET_POWER_QUALITY_VALUES);
    }

    @Override
    public void processMessage(final ObjectMessage message) {
        LOGGER.info("Processing DA Get PQ Values request message");

        String correlationUid = null;
        String messageType = null;
        String organisationIdentification = null;
        String deviceIdentification = null;
        GetPQValuesRequest getPQValuesRequest = null;

        try {
            correlationUid = message.getJMSCorrelationID();
            messageType = message.getJMSType();
            organisationIdentification = message.getStringProperty(Constants.ORGANISATION_IDENTIFICATION);
            deviceIdentification = message.getStringProperty(Constants.DEVICE_IDENTIFICATION);

            if (message.getObject() instanceof GetPQValuesRequest) {
                getPQValuesRequest = (GetPQValuesRequest) message.getObject();
            }

        } catch (final JMSException e) {
            LOGGER.error("UNRECOVERABLE ERROR, unable to read ObjectMessage instance, giving up.", e);
            LOGGER.debug("correlationUid: {}", correlationUid);
            LOGGER.debug("messageType: {}", messageType);
            LOGGER.debug("organisationIdentification: {}", organisationIdentification);
            LOGGER.debug("deviceIdentification: {}", deviceIdentification);
            return;
        }

        try {
            LOGGER.info("Calling application service function: {}", messageType);

            this.monitoringService.getPQValues(organisationIdentification, deviceIdentification, correlationUid,
                    messageType, getPQValuesRequest);

        } catch (final Exception e) {
            this.handleError(e, correlationUid, organisationIdentification, deviceIdentification, messageType);
        }
    }
}
