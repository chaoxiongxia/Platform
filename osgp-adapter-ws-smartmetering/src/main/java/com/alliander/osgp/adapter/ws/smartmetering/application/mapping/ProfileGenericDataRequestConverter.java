/**
 * Copyright 2015 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.ws.smartmetering.application.mapping;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

import com.alliander.osgp.adapter.ws.schema.smartmetering.monitoring.PeriodicReadsRequest;
import com.alliander.osgp.adapter.ws.schema.smartmetering.monitoring.ProfileGenericDataRequest;
import com.alliander.osgp.domain.core.valueobjects.smartmetering.ProfileGenericDataQuery;

public class ProfileGenericDataRequestConverter extends CustomConverter<PeriodicReadsRequest, ProfileGenericDataQuery> {

    @Override
    public ProfileGenericDataQuery convert(final PeriodicReadsRequest source,
            final Type<? extends ProfileGenericDataQuery> destinationType) {
        return new ProfileGenericDataQuery(
                com.alliander.osgp.domain.core.valueobjects.smartmetering.PeriodType.valueOf(source
                        .getPeriodicReadsRequestData().getPeriodType().name()), source.getPeriodicReadsRequestData()
                        .getBeginDate().toGregorianCalendar().getTime(), source.getPeriodicReadsRequestData()
                        .getEndDate().toGregorianCalendar().getTime(), source instanceof ProfileGenericDataRequest, "");
    }

}
