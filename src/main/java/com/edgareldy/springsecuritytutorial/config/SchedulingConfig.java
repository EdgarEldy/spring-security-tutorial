package com.edgareldy.springsecuritytutorial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables {@code @Scheduled} methods, used by
 * {@code scheduler.TokenCleanupScheduler} (feature/tokens) to purge expired
 * activation, password reset, and blacklisted tokens on a daily cron.
 * <p>
 * Created by edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-security-tutorial
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
