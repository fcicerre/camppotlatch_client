package org.coursera.camppotlatch.client.androidservice;

interface IDataRefreshServiceCallback {
    /**
     *  Notify the client about the data update
     */
    void notifyUpdate();
}
