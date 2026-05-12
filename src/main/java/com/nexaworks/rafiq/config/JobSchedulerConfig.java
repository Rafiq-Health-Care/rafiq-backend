package com.nexaworks.rafiq.config;

import javax.sql.DataSource;

import org.jobrunr.configuration.JobRunr;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobSchedulerConfig {

    @Bean
    public JobScheduler jobScheduler(DataSource dataSource, JobActivator jobActivator) {
        return JobRunr.configure().useJobActivator(jobActivator)
                .useStorageProvider(SqlStorageProviderFactory.using(dataSource))
                .useBackgroundJobServer().useDashboard().initialize().getJobScheduler();
    }
}
