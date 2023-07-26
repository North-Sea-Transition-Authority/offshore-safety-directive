package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.List;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;

record AssetAppointmentHistory(AssetName assetName, List<AssetTimelineItemView> timelineItemViews) {
}