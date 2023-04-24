package uk.co.nstauthority.offshoresafetydirective.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
// Async method execution is not enabled for the integration-test profile, as some of our integration tests rely on
// async listeners firing before their assertions are called.
@Profile("!integration-test")
class AsyncConfiguration {
}