/**
 * Copyright 2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.ws.smartmetering.endpoints;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.alliander.osgp.adapter.ws.endpointinterceptors.MessagePriority;
import com.alliander.osgp.adapter.ws.endpointinterceptors.OrganisationIdentification;
import com.alliander.osgp.adapter.ws.schema.smartmetering.bundle.Actions;
import com.alliander.osgp.adapter.ws.schema.smartmetering.bundle.BundleAsyncRequest;
import com.alliander.osgp.adapter.ws.schema.smartmetering.bundle.BundleAsyncResponse;
import com.alliander.osgp.adapter.ws.schema.smartmetering.bundle.BundleRequest;
import com.alliander.osgp.adapter.ws.schema.smartmetering.bundle.BundleResponse;
import com.alliander.osgp.adapter.ws.schema.smartmetering.common.Action;
import com.alliander.osgp.adapter.ws.smartmetering.application.services.ActionMapperService;
import com.alliander.osgp.adapter.ws.smartmetering.application.services.BundleService;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.ActionValueObject;
import com.alliander.osgp.shared.exceptionhandling.ComponentType;
import com.alliander.osgp.shared.exceptionhandling.OsgpException;
import com.alliander.osgp.shared.wsheaderattribute.priority.MessagePriorityEnum;

//MethodConstraintViolationException is deprecated.
//Will by replaced by equivalent functionality defined
//by the Bean Validation 1.1 API as of Hibernate Validator 5.

@SuppressWarnings("deprecation")
@Endpoint
public class SmartMeteringBundleEndpoint extends SmartMeteringEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartMeteringBundleEndpoint.class);
    private static final String NAMESPACE = "http://www.alliander.com/schemas/osgp/smartmetering/sm-bundle/2014/10";
    private static final ComponentType COMPONENT_WS_SMART_METERING = ComponentType.WS_SMART_METERING;

    private final BundleService bundleService;
    private final ActionMapperService actionMapperService;

    @Autowired
    public SmartMeteringBundleEndpoint(
            @Qualifier(value = "wsSmartMeteringBundleService") final BundleService bundleService,
            @Qualifier(value = "wsSmartMeteringActionMapperService") final ActionMapperService actionMapperService) {
        this.bundleService = bundleService;
        this.actionMapperService = actionMapperService;
    }

    @PayloadRoot(localPart = "BundleRequest", namespace = NAMESPACE)
    @ResponsePayload
    public BundleAsyncResponse bundleRequest(@OrganisationIdentification final String organisationIdentification,
            @MessagePriority final String messagePriority, @RequestPayload final BundleRequest request)
            throws OsgpException {

        LOGGER.info("Bundle request for organisation: {} and device: {}.", organisationIdentification,
                request.getDeviceIdentification());

        BundleAsyncResponse response = null;
        try {
            // Create response.
            response = new BundleAsyncResponse();

            // Get the request parameters, make sure that date time are in UTC.
            final String deviceIdentification = request.getDeviceIdentification();

            final Actions actions = request.getActions();
            final List<? extends Action> actionList = actions.getActionList();

            final List<ActionValueObject> actionValueObjectList = this.actionMapperService.mapAllActions(actionList);

            final String correlationUid = this.bundleService.enqueueBundleRequest(organisationIdentification,
                    deviceIdentification, actionValueObjectList,
                    MessagePriorityEnum.getMessagePriority(messagePriority));

            response.setCorrelationUid(correlationUid);
            response.setDeviceIdentification(request.getDeviceIdentification());

        } catch (final Exception e) {
            this.handleException(e);
        }
        return response;
    }

    @PayloadRoot(localPart = "BundleAsyncRequest", namespace = NAMESPACE)
    @ResponsePayload
    public BundleResponse getBundleResponse(@OrganisationIdentification final String organisationIdentification,
            @RequestPayload final BundleAsyncRequest request) throws OsgpException {

        LOGGER.info("Get bundle response for organisation: {} and device: {}.", organisationIdentification,
                request.getDeviceIdentification());

        BundleResponse response = null;
        // Create response.
        response = new BundleResponse();

        // Get the request parameters, make sure that date time are in UTC.
        final String correlationUid = request.getCorrelationUid();

        // TODO:
        // final List<Action> actions =
        // this.bundleService.findActionsByCorrelationUid(organisationIdentification,
        // correlationUid);

        // LOGGER.info("getBundleResponse() number of actions: {}",
        // actions.size());
        LOGGER.info("mapping events to schema type...");
        // TODO:
        // response.getgetEvents().addAll(
        // this.managementMapper.mapAsList(events,
        // com.alliander.osgp.adapter.ws.schema.smartmetering.management.Event.class));
        LOGGER.info("mapping done, sending response...");
        return response;
    }

}
