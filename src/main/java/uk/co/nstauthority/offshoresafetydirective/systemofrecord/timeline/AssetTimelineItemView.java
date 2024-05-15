package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.time.Instant;
import java.time.LocalDate;

public record AssetTimelineItemView(
    TimelineEventType timelineEventType,
    String title,
    AssetTimelineModelProperties assetTimelineModelProperties,
    Instant createdInstant,
    LocalDate eventDate
) {
}
