package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.time.Instant;

public record AssetTimelineItemView(
    TimelineEventType timelineEventType,
    String title,
    AssetTimelineModelProperties assetTimelineModelProperties,
    Instant createdInstant
) {
}
