/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.domain.smartmetering.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alliander.osgp.adapter.domain.smartmetering.application.mapping.InstallationMapper;
import com.alliander.osgp.adapter.domain.smartmetering.infra.jms.core.OsgpCoreRequestMessageSender;
import com.alliander.osgp.domain.core.entities.ProtocolInfo;
import com.alliander.osgp.domain.core.entities.SmartMeteringDevice;
import com.alliander.osgp.domain.core.repositories.DeviceAuthorizationRepository;
import com.alliander.osgp.domain.core.repositories.DeviceRepository;
import com.alliander.osgp.domain.core.repositories.OrganisationRepository;
import com.alliander.osgp.domain.core.repositories.ProtocolInfoRepository;
import com.alliander.osgp.domain.core.repositories.SmartMeteringDeviceRepository;
import com.alliander.osgp.domain.core.validation.Identification;
import com.alliander.osgp.shared.exceptionhandling.ComponentType;
import com.alliander.osgp.shared.exceptionhandling.FunctionalException;
import com.alliander.osgp.shared.exceptionhandling.FunctionalExceptionType;
import com.alliander.osgp.shared.infra.jms.RequestMessage;

/**
 * @author OSGP
 *
 */
@Service(value = "domainSmartMeteringInstallationService")
@Transactional(value = "transactionManager")
public class InstallationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallationService.class);

    @Autowired
    @Qualifier(value = "domainSmartMeteringOutgoingOsgpCoreRequestMessageSender")
    private OsgpCoreRequestMessageSender osgpCoreRequestMessageSender;

    @Autowired
    private SmartMeteringDeviceRepository smartMeteringDeviceRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceAuthorizationRepository deviceAuthorizationRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ProtocolInfoRepository protocolInfoRepository;

    @Autowired
    private InstallationMapper installationMapper;

    public InstallationService() {
        // Parameterless constructor required for transactions...
    }

    public void addMeter(@Identification final String organisationIdentification,
            @Identification final String deviceIdentification, final String correlationUid,
            final com.alliander.osgp.domain.core.valueobjects.SmartMeteringDevice smartMeteringDeviceValueObject,
            final String messageType) throws FunctionalException {

        LOGGER.info("addMeter for organisationIdentification: {} for deviceIdentification: {}",
                organisationIdentification, deviceIdentification);

        // TODO: bypassing authorization, this should be fixed.
        // Organisation organisation =
        // this.findOrganisation(organisationIdentification);
        // final Device device = this.findActiveDevice(deviceIdentification);

        SmartMeteringDevice device = this.smartMeteringDeviceRepository
                .findByDeviceIdentification(deviceIdentification);
        if (device == null) {

            device = this.installationMapper.map(smartMeteringDeviceValueObject, SmartMeteringDevice.class);

            final ProtocolInfo protocolInfo = this.protocolInfoRepository.findByProtocolAndProtocolVersion("DSMR",
                    smartMeteringDeviceValueObject.getDSMRVersion());
            device.updateProtocol(protocolInfo);

            // TODO deviceAuthorization
            // final DeviceAuthorization deviceAuthorization = new
            // DeviceAuthorization(dummy, organisation,
            // com.alliander.osgp.domain.core.valueobjects.DeviceFunctionGroup.OWNER);
            // this.deviceAuthorizationRepository.save(deviceAuthorization);

            this.smartMeteringDeviceRepository.save(device);

        } else {
            throw new FunctionalException(FunctionalExceptionType.EXISTING_DEVICE, ComponentType.DOMAIN_SMART_METERING);
        }

        final com.alliander.osgp.dto.valueobjects.SmartMeteringDevice smartMeteringDevicDto = this.installationMapper
                .map(smartMeteringDeviceValueObject, com.alliander.osgp.dto.valueobjects.SmartMeteringDevice.class);

        this.osgpCoreRequestMessageSender.send(new RequestMessage(correlationUid, organisationIdentification,
                deviceIdentification, smartMeteringDevicDto), messageType);
    }
}