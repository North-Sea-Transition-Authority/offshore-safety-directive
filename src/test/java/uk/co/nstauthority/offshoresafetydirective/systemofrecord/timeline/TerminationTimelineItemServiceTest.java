package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationTestUtil;

@ExtendWith(MockitoExtension.class)
class TerminationTimelineItemServiceTest {

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private AppointmentTerminationService appointmentTerminationService;

  @InjectMocks
  private TerminationTimelineItemService terminationTimelineItemService;

  @Test
  void getTimelineItemViews_whenTermination_thenPopulatedTerminationViewList() {
    var wuaId = 1L;
    var termination = AppointmentTerminationTestUtil.builder()
        .withCreatedTimestamp(Instant.now())
        .withCorrectedByWuaId(wuaId)
        .withReasonForTermination("reason")
        .withTerminationDate(LocalDate.of(2023, 8, 15))
        .build();

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .build();

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId))))
        .willReturn(List.of(energyPortalUser));

    var appointments = List.of(AppointmentTestUtil.builder().build());

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    assertThat(resultingTerminationViewList)
        .extracting(
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminationDate"),
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("reasonForTermination"),
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminatedBy")

        )
        .containsExactly(
            tuple(
                DateUtil.formatLongDate(termination.getTerminationDate()),
                termination.getReasonForTermination(),
                energyPortalUser.displayName()
            )
        );

    assertThat(resultingTerminationViewList)
        .extracting(
            AssetTimelineItemView::title,
            AssetTimelineItemView::timelineEventType,
            AssetTimelineItemView::createdInstant,
            AssetTimelineItemView::eventDate
        ).containsExactly(
            tuple(
                 "Termination of appointment",
                 TimelineEventType.TERMINATION,
                 termination.getCreatedTimestamp(),
                 termination.getTerminationDate()
            )
        );
  }

  @Test
  void getTimelineItemViews_whenWuaIdNotInEnergyPortal_thenThrow() {
     var wuaId = 1L;
     var terminations = List.of(AppointmentTerminationTestUtil.builder().withCorrectedByWuaId(wuaId).build());
     var appointments = List.of(AppointmentTestUtil.builder().build());

     given(appointmentTerminationService.getTerminations(appointments))
         .willReturn(terminations);

     given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId))))
         .willReturn(Collections.emptyList());

     var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    assertThat(resultingTerminationViewList)
        .extracting(
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminatedBy")

        )
        .containsExactly("Unknown");
  }
}