/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web;

import org.apache.wicket.model.LoadableDetachableModel;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.web.GeoServerApplication;

/**
 * Model for the main {@link GeoServerSecurityManager} configuration.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class SecurityManagerConfigModel extends LoadableDetachableModel<SecurityManagerConfig> {

    @Override
    protected SecurityManagerConfig load() {
        return GeoServerApplication.get().getSecurityManager().getSecurityConfig();
    }

}
