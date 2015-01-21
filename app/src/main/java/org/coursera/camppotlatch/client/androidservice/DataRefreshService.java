package org.coursera.camppotlatch.client.androidservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DataRefreshService extends Service {
    //private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Timer mTimer;
    private NotifyUpdateTask mUpdateTask;

    private Map<Integer, IDataRefreshServiceCallback> mUpdateClientsMap;
    private int mUpdatePeriod = 0;

    private final IDataRefreshService.Stub mBinder = new IDataRefreshService.Stub() {
        private int clientId = 0;
        @Override
        public int addUpdatePeriodClient(IDataRefreshServiceCallback client) throws RemoteException {
            synchronized (this) {
                mUpdateClientsMap.put(clientId++, client);
                if (mUpdateClientsMap.size() == 1) {
                    startPeriodicUpdateNotificationTask();
                }
            }

            return clientId;

        }

        @Override
        public void removeUpdatePeriodClient(int clientId) throws RemoteException {
            synchronized (this) {
                mUpdateClientsMap.remove(clientId);
                if (mUpdateClientsMap.size() == 0) {
                    stopPeriodicUpdateNotificationTask();
                }
            }
        }

        @Override
        public void setUpdatePeriod(int updatePeriod) throws RemoteException {
            synchronized (this) {
                mUpdatePeriod = updatePeriod;
                if (mUpdatePeriod > 0) {
                    startPeriodicUpdateNotificationTask();
                } else {
                    stopPeriodicUpdateNotificationTask();
                }
            }
        }
    };

    private class NotifyUpdateTask extends TimerTask {
        @Override
        public void run() {
            for(IDataRefreshServiceCallback client : mUpdateClientsMap.values()) {
                try {
                    client.notifyUpdate();
                } catch (RemoteException ex) {
                }
            }
        }
    };

    public DataRefreshService() {
        mUpdateClientsMap =
                Collections.synchronizedMap(new HashMap<Integer, IDataRefreshServiceCallback>());
        mUpdatePeriod = 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        return mBinder;
    }

    private void startPeriodicUpdateNotificationTask() {
        if (mUpdatePeriod > 0 && mUpdateClientsMap.size() > 0) {
            //scheduler.scheduleWithFixedDelay(notifyUpdateTask, mUpdatePeriod,
            //        mUpdatePeriod, TimeUnit.MINUTES);
            synchronized (this) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                }

                mTimer = new Timer();
                mUpdateTask = new NotifyUpdateTask();
                mTimer.schedule(
                        mUpdateTask, mUpdatePeriod * 60 * 1000, mUpdatePeriod * 60 * 1000);
            }
        } else {
            stopPeriodicUpdateNotificationTask();
        }
    }

    private void stopPeriodicUpdateNotificationTask() {
        synchronized (this) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }
    }
}
