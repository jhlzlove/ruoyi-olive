package com.olive.job;

import org.jobrunr.configuration.JobRunr;
import org.jobrunr.storage.InMemoryStorageProvider;

/**
 * @author jhlz
 * @version x.x.x
 */
public class Jobconfig {
    public static void main(String[] args) {

        JobRunr.configure()
                .useStorageProvider(new InMemoryStorageProvider())
                .useBackgroundJobServer()
                .useDashboard()
                .initialize();
    }
}
