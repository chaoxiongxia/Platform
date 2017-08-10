/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.domain.microgrids.application.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alliander.osgp.domain.core.services.CorrelationIdProviderService;
import com.alliander.osgp.domain.core.valueobjects.DeviceFunction;
import com.alliander.osgp.domain.microgrids.entities.RtuDevice;
import com.alliander.osgp.dto.valueobjects.microgrids.GetDataRequestDto;
import com.alliander.osgp.dto.valueobjects.microgrids.GetDataResponseDto;
import com.alliander.osgp.dto.valueobjects.microgrids.GetDataSystemIdentifierDto;
import com.alliander.osgp.dto.valueobjects.microgrids.MeasurementDto;
import com.alliander.osgp.dto.valueobjects.microgrids.MeasurementFilterDto;
import com.alliander.osgp.dto.valueobjects.microgrids.PhaseDto;
import com.alliander.osgp.dto.valueobjects.microgrids.SystemFilterDto;
import com.alliander.osgp.shared.infra.jms.RequestMessage;
import com.alliander.osgp.shared.infra.jms.ResponseMessageResultType;

@Service(value = "domainMicrogridsCommunicationRecoveryService")
@Transactional(value = "transactionManager")
public class CommunicatonRecoveryService extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatonRecoveryService.class);

    private static final int SYSTEM_ID = 1;
    private static final String SYSTEM_TYPE = "RTU";
    private static final int MEASUREMENT_ID = 1;
    private static final String MEASUREMENT_NODE = "Alm1";
    private static final double MEASUREMENT_VALUE_ALARM_ON = 1.0;

    // new code bjorn
    private static final int PHASE_ID = 1;
    private static final String NAME = "phsA";

    @Autowired
    private CorrelationIdProviderService correlationIdProviderService;
    @Autowired
    @Qualifier("domainMicrogridsAdHocManagementService")
    private AdHocManagementService adHocManagementService;

    /**
     * Send a signal that the connection with the device has been lost. This is
     * done by putting a GetDataResponse on the queue with an alarm value. When
     * this response is received by the webservice adapter, it can send a
     * notification to the client.
     *
     * @param rtu
     */
    public void signalConnectionLost(final RtuDevice rtu) {
        LOGGER.info("Sending connection lost signal for device {}.", rtu.getDeviceIdentification());

        final GetDataResponseDto dataResponse = new GetDataResponseDto(Arrays.asList(
                new GetDataSystemIdentifierDto(SYSTEM_ID, SYSTEM_TYPE, Arrays.asList(new MeasurementDto(MEASUREMENT_ID,
                        MEASUREMENT_NODE, 0, new DateTime(DateTimeZone.UTC), MEASUREMENT_VALUE_ALARM_ON,

                        Arrays.asList(new PhaseDto(PHASE_ID, NAME, 0, new DateTime(DateTimeZone.UTC),
                                MEASUREMENT_VALUE_ALARM_ON))

                )))), null);

        final String correlationUid = this.createCorrelationUid(rtu);
        final String organisationIdentification = rtu.getOwner().getOrganisationIdentification();
        final String deviceIdentification = rtu.getDeviceIdentification();

        this.adHocManagementService.handleGetDataResponse(dataResponse, deviceIdentification,
                organisationIdentification, correlationUid, DeviceFunction.GET_DATA.toString(),
                ResponseMessageResultType.OK, null);
    }

    public void restoreCommunication(final RtuDevice rtu) {
        LOGGER.info("Restoring communication for device {}.", rtu.getDeviceIdentification());

        if (rtu.getOwner() == null) {
            LOGGER.warn("Device {} has no owner. Skipping communication recovery.", rtu.getDeviceIdentification());
            return;
        }

        final RequestMessage message = this.createMessage(rtu);
        this.osgpCoreRequestMessageSender.send(message, DeviceFunction.GET_DATA.toString(), rtu.getIpAddress());
    }

    private RequestMessage createMessage(final RtuDevice rtu) {
        LOGGER.debug("Creating message for device {}.", rtu.getDeviceIdentification());

        final String correlationUid = this.createCorrelationUid(rtu);
        final String organisationIdentification = rtu.getOwner().getOrganisationIdentification();
        final String deviceIdentification = rtu.getDeviceIdentification();
        final GetDataRequestDto request = this.createRequest(rtu);

        return new RequestMessage(correlationUid, organisationIdentification, deviceIdentification, request);
    }

    private String createCorrelationUid(final RtuDevice rtu) {
        LOGGER.debug("Creating correlation uid for device {}, with owner {}", rtu.getDeviceIdentification(),
                rtu.getOwner().getOrganisationIdentification());

        final String correlationUid = this.correlationIdProviderService
                .getCorrelationId(rtu.getOwner().getOrganisationIdentification(), rtu.getDeviceIdentification());

        LOGGER.debug("Correlation uid {} created.", correlationUid);

        return correlationUid;
    }

    private GetDataRequestDto createRequest(final RtuDevice rtu) {
        LOGGER.debug("Creating data request for rtu {}.", rtu.getDeviceIdentification());

        final List<MeasurementFilterDto> measurementFilters = new ArrayList<>();
        measurementFilters.add(new MeasurementFilterDto(MEASUREMENT_ID, MEASUREMENT_NODE, false));

        final List<SystemFilterDto> systemFilters = new ArrayList<>();
        systemFilters.add(new SystemFilterDto(SYSTEM_ID, SYSTEM_TYPE, measurementFilters, false));

        return new GetDataRequestDto(systemFilters);
    }

}
