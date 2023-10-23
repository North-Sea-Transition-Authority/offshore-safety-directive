package uk.co.nstauthority.offshoresafetydirective.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MetricsProvider {

  private static final String METRIC_PREFIX = "osd";
  private final Timer workAreaQueryTimer;
  private final Timer systemOfRecordSearchTimer;
  private final Counter wonsApplicationMessagesReceivedCounter;
  private final Counter pearsLicenceMessagesReceivedCounter;
  private final Counter appointmentsPublishedCounter;
  private final Counter nominationsPublishedCounter;

  public MetricsProvider(MeterRegistry registry) {
    this.workAreaQueryTimer = registry.timer("%s.workAreaJooqQueryTimer".formatted(METRIC_PREFIX));
    this.systemOfRecordSearchTimer = registry.timer("%s.systemOfRecordSearchTimer".formatted(METRIC_PREFIX));

    this.wonsApplicationMessagesReceivedCounter =
        registry.counter("%s.wonsApplicationMessagesReceivedCounter".formatted(METRIC_PREFIX));
    this.pearsLicenceMessagesReceivedCounter =
        registry.counter("%s.pearsLicenceMessagesReceivedCounter".formatted(METRIC_PREFIX));
    this.appointmentsPublishedCounter = registry.counter("%s.appointmentsPublishedCounter".formatted(METRIC_PREFIX));
    this.nominationsPublishedCounter = registry.counter("%s.nominationsPublishedCounter".formatted(METRIC_PREFIX));
  }

  public Timer getWorkAreaQueryTimer() {
    return workAreaQueryTimer;
  }

  public Timer getSystemOfRecordSearchTimer() {
    return systemOfRecordSearchTimer;
  }

  public Counter getWonsApplicationMessagesReceivedCounter() {
    return wonsApplicationMessagesReceivedCounter;
  }

  public Counter getPearsLicenceMessagesReceivedCounter() {
    return pearsLicenceMessagesReceivedCounter;
  }

  public Counter getAppointmentsPublishedCounter() {
    return appointmentsPublishedCounter;
  }

  public Counter getNominationsPublishedCounter() {
    return nominationsPublishedCounter;
  }
}
