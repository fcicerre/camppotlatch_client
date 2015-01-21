package org.coursera.camppotlatch.client.androidservice;

import org.coursera.camppotlatch.client.androidservice.IDataRefreshServiceCallback;

interface IDataRefreshService {
    /**
     * Set the automatic update period
     */
    void setUpdatePeriod(int updatePeriod);

    /**
     *  Add a client to the periodic update service
     */
    int addUpdatePeriodClient(IDataRefreshServiceCallback client);

    /*
     * Remove a client from the periodic update service
     */
    void removeUpdatePeriodClient(int clientId);
}
