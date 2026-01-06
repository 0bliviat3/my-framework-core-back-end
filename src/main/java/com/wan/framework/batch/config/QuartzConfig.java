package com.wan.framework.batch.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Quartz 스케줄러 설정
 * - Spring Bean 의존성 주입 지원
 * - Job Store 설정
 * - Thread Pool 설정
 */
@Configuration
public class QuartzConfig {

    /**
     * Spring Bean JobFactory
     * - Quartz Job에서 Spring Bean 사용 가능하도록 설정
     */
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * Scheduler Factory Bean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            JobFactory jobFactory,
            DataSource dataSource) {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProperties());
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);

        return factory;
    }

    /**
     * Quartz Scheduler
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.start();
        return scheduler;
    }

    /**
     * Quartz 속성 설정
     */
    private Properties quartzProperties() {
        Properties properties = new Properties();

        // Scheduler 설정
        properties.setProperty("org.quartz.scheduler.instanceName", "BatchScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        // Thread Pool 설정
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");

        // Job Store 설정 (DB 사용)
        properties.setProperty("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");

        return properties;
    }
}
