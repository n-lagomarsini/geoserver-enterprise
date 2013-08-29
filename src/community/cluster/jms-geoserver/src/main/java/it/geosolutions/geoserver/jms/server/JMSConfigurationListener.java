/* Copyright (c) 2011 GeoSolutions - http://www.geo-solutions.it/.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.geosolutions.geoserver.jms.server;

import it.geosolutions.geoserver.jms.JMSApplicationListener;
import it.geosolutions.geoserver.jms.JMSPublisher;
import it.geosolutions.geoserver.jms.impl.events.configuration.JMSGlobalModifyEvent;
import it.geosolutions.geoserver.jms.impl.events.configuration.JMSServiceModifyEvent;
import it.geosolutions.geoserver.jms.impl.utils.BeanUtils;

import java.util.List;

import javax.jms.JMSException;

import org.geoserver.catalog.impl.ModificationProxy;
import org.geoserver.config.ConfigurationListener;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMS MASTER (Producer) Listener used to send GeoServer JMSGeoServerConfigurationExt events over the JMS channel.
 * 
 * @see {@link JMSApplicationListener}
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class JMSConfigurationListener extends JMSAbstractGeoServerProducer implements
        ConfigurationListener {

    private final GeoServer geoserver;

    private final static Logger LOGGER = LoggerFactory.getLogger(JMSConfigurationListener.class);

    private final JMSPublisher jmsPublisher;

    /**
     * 
     * @param topicTemplate the JmsTemplate object used to send message to the topic queue
     * @param geoserver
     * @param props properties to attach to all the message. May contains at least the producer name which should be unique.
     */
    public JMSConfigurationListener(final GeoServer geoserver, final JMSPublisher jmsPublisher) {
        super();
        // store GeoServer reference
        this.geoserver = geoserver;
        // add this as geoserver listener
        this.geoserver.addListener(this);
        // the publisher
        this.jmsPublisher = jmsPublisher;
    }

    @Override
    public void handleGlobalChange(GeoServerInfo global, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Incoming event");
        }

        // skip incoming events if producer is not Enabled
        if (!isEnabled()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("skipping incoming event: context is not initted");
            }
            return;
        }

        try {

            // propagate the event
            jmsPublisher.publish(getTopic(), getJmsTemplate(), config.getConfigurations(),
                    new JMSGlobalModifyEvent(ModificationProxy.unwrap(global), propertyNames,
                            oldValues, newValues));

        } catch (JMSException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void handleLoggingChange(LoggingInfo logging, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Incoming event");
        }

        // skip incoming events if producer is not Enabled
        if (!isEnabled()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("skipping incoming event: context is not initted");
            }
            return;
        }

        try {
            // update the logging event with changes
            BeanUtils.smartUpdate(ModificationProxy.unwrap(logging), propertyNames, newValues);

            // propagate the event
            jmsPublisher.publish(getTopic(), getJmsTemplate(), config.getConfigurations(),
                    logging);

        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void handleServiceChange(ServiceInfo service, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Incoming event of type");
        }

        // skip incoming events if producer is not Enabled
        if (!isEnabled()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("skipping incoming event: context is not initted");
            }
            return;
        }

        try {
            // propagate the event
            jmsPublisher.publish(getTopic(), getJmsTemplate(), config.getConfigurations(),
                    new JMSServiceModifyEvent(ModificationProxy.unwrap(service), propertyNames,
                            oldValues, newValues));

        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public void handlePostLoggingChange(LoggingInfo logging) {
        // send(xstream.toXML(logging), JMSConfigEventType.LOGGING_CHANGE);
    }

    @Override
    public void handlePostServiceChange(ServiceInfo service) {
        // send(xstream.toXML(service), JMSConfigEventType.POST_SERVICE_CHANGE);
    }

    @Override
    public void handlePostGlobalChange(GeoServerInfo global) {
        // no op.s
    }

    @Override
    public void reloaded() {

        // skip incoming events until context is loaded
        if (!isEnabled()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("skipping incoming event: context is not initted");
            }
            return;
        }

        // EAT EVENT
        // TODO check why reloaded here? check differences from CatalogListener
        // reloaded() method?
        // TODO disable and re-enable the producer!!!!!
        // this is potentially a problem since this listener should be the first
        // called by the GeoServer.

    }

    @Override
    public void handleSettingsAdded(SettingsInfo settings) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleSettingsModified(SettingsInfo settings, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleSettingsPostModified(SettingsInfo settings) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleSettingsRemoved(SettingsInfo settings) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleServiceRemove(ServiceInfo service) {
        // TODO Auto-generated method stub

    }
}
