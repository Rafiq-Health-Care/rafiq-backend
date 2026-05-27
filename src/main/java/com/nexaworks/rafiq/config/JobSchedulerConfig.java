package com.nexaworks.rafiq.config;

import javax.sql.DataSource;

import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobSchedulerConfig {

    @Bean
    public JobScheduler jobScheduler(DataSource dataSource, JobActivator jobActivator,
            @Value("${jobrunr.dashboard.enabled:true}") boolean dashboardEnabled) {
        JobRunrConfiguration configuration = JobRunr.configure().useJobActivator(jobActivator)
                .useStorageProvider(SqlStorageProviderFactory.using(dataSource))
                .useBackgroundJobServer();

        if (dashboardEnabled) {
            configuration.useDashboard();
        }

        return configuration.initialize().getJobScheduler();
    }
}
