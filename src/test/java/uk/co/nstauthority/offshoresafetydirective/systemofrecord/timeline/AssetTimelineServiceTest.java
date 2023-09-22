package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDto;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationTestUtil;

@ExtendWith(MockitoExtension.class)
class AssetTimelineServiceTest {

  @Mock
  private PortalAssetNameService portalAssetNameService;

  @Mock
  private AssetAccessService assetAccessService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private AppointmentTimelineItemService appointmentTimelineItemService;

  @Mock
  private TerminationTimelineItemService terminationTimelineItemService;

  @InjectMocks
  private AssetTimelineService assetTimelineService;

  @Test
  void getAppointmentHistoryForPortalAsset_whenNotInPortalOrSystemOfRecord_thenEmptyOptional() {

    var portalAssetId = new PortalAssetId("something not in sor or portal");
    var portalAssetType = PortalAssetType.INSTALLATION;

    given(portalAssetNameService.getAssetName(portalAssetId, portalAssetType))
        .willReturn(Optional.empty());

    given(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistory = assetTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    assertThat(resultingAppointmentTimelineHistory).isEmpty();
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAssetFromPortal_thenAssetNameIsPortalName() {

    var portalAssetId = new PortalAssetId("something in portal");
    var portalAssetType = PortalAssetType.INSTALLATION;

    given(portalAssetNameService.getAssetName(portalAssetId, portalAssetType))
        .willReturn(Optional.of(new AssetName("from portal")));

    given(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistory = assetTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get())
        .extracting(assetAppointmentHistory -> assetAppointmentHistory.assetName().value())
        .isEqualTo("from portal");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAssetFromSystemOfRecord_thenAssetNameCachedAssetName() {

    var portalAssetId = new PortalAssetId("something in sor and not from portal");
    var portalAssetType = PortalAssetType.INSTALLATION;

    given(portalAssetNameService.getAssetName(portalAssetId, portalAssetType))
        .willReturn(Optional.empty());

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .willReturn(Optional.of(assetInSystemOfRecord));

    var resultingAppointmentTimelineHistory = assetTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        portalAssetType
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get())
        .extracting(AssetAppointmentHistory::assetName)
        .isEqualTo(assetInSystemOfRecord.assetName());
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenNoAppointments_thenEmptyAppointmentViewList() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var portalAssetType = PortalAssetType.INSTALLATION;
    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .willReturn(Optional.of(assetInSystemOfRecord));

    var resultingAppointmentTimelineHistory = assetTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews()).isEmpty();
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenMultipleItems_thenOrderedByDescendingEventDate() {
    var portalAssetId = new PortalAssetId("something from system of record");

    var portalAssetType = PortalAssetType.INSTALLATION;
    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .willReturn(Optional.of(assetInSystemOfRecord));

    var firstAppointmentByEventDate = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.of(2023, 8, 16))
        .build();
    var terminationOfFirstAppointment = AppointmentTerminationTestUtil.builder()
        .withTerminationDate(LocalDate.of(2023, 8, 17))
        .build();
    var secondAppointmentByEventDate = AppointmentTestUtil.builder()
        .withResponsibleFromDate(LocalDate.of(2023, 8, 18))
        .build();

    var appointmentDto1 = AppointmentDto.fromAppointment(firstAppointmentByEventDate);
    var appointmentDto2 = AppointmentDto.fromAppointment(secondAppointmentByEventDate);

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(firstAppointmentByEventDate, secondAppointmentByEventDate));

    given(appointmentTimelineItemService.getTimelineItemViews(List.of(appointmentDto1, appointmentDto2), assetInSystemOfRecord))
        .willReturn(
            List.of(
                new AssetTimelineItemView(
                    TimelineEventType.APPOINTMENT,
                    "appointment 1",
                    new AssetTimelineModelProperties(),
                    Instant.now(),
                    appointmentDto1.appointmentFromDate().value()
                ),
                new AssetTimelineItemView(
                    TimelineEventType.APPOINTMENT,
                    "appointment 2",
                    new AssetTimelineModelProperties(),
                    Instant.now(),
                    appointmentDto2.appointmentFromDate().value()
                )
            )
        );

    given(terminationTimelineItemService.getTimelineItemViews(List.of(firstAppointmentByEventDate, secondAppointmentByEventDate)))
        .willReturn(
            List.of(
                new AssetTimelineItemView(
                    TimelineEventType.TERMINATION,
                    "Termination of appointment",
                    new AssetTimelineModelProperties(),
                    Instant.now(),
                    terminationOfFirstAppointment.getTerminationDate()
                )
            )
        );

    var resultingAppointmentTimelineHistory = assetTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews())
        .extracting(AssetTimelineItemView::eventDate)
        .containsExactly(
            appointmentDto2.appointmentFromDate().value(),
            terminationOfFirstAppointment.getTerminationDate(),
            appointmentDto1.appointmentFromDate().value()
        );
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenMultipleItemsWithSameEventDate_thenOrderedByDescendingCreationDateTime() {
    var portalAssetId = new PortalAssetId("something from system of record");

    var portalAssetType = PortalAssetType.INSTALLATION;
    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId, portalAssetType))
        .willReturn(Optional.of(assetInSystemOfRecord));

    Instant instant = Instant.now();

    var firstAppointmentInstant = instant.atZone(ZoneOffset.UTC)
        .withDayOfYear(1)
        .withHour(1)
        .toInstant();

    var terminationInstant = instant.atZone(ZoneOffset.UTC)
        .withDayOfYear(1)
        .withHour(2)
        .toInstant();

    var secondAppointmentInstant = instant.atZone(ZoneOffset.UTC)
        .withDayOfYear(1)
        .withHour(3)
        .toInstant();

    var firstAppointmentByCreatedTimestamp = AppointmentTestUtil.builder()
        .withCreatedDatetime(firstAppointmentInstant)
        .build();
    var terminationOfFirstAppointment = AppointmentTerminationTestUtil.builder()
        .withCreatedTimestamp(terminationInstant)
        .build();
    var secondAppointmentByCreatedTimestamp = AppointmentTestUtil.builder()
        .withCreatedDatetime(secondAppointmentInstant)
        .build();

    var appointmentDto1 = AppointmentDto.fromAppointment(firstAppointmentByCreatedTimestamp);
    var appointmentDto2 = AppointmentDto.fromAppointment(secondAppointmentByCreatedTimestamp);

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(firstAppointmentByCreatedTimestamp, secondAppointmentByCreatedTimestamp));

    given(appointmentTimelineItemService.getTimelineItemViews(List.of(appointmentDto1, appointmentDto2), assetInSystemOfRecord))
        .willReturn(
            List.of(
                new AssetTimelineItemView(
                    TimelineEventType.APPOINTMENT,
                    "appointment 1",
                    new AssetTimelineModelProperties(),
                    firstAppointmentInstant,
                    LocalDate.now()
                ),
                new AssetTimelineItemView(
                    TimelineEventType.APPOINTMENT,
                    "appointment 2",
                    new AssetTimelineModelProperties(),
                    secondAppointmentInstant,
                    LocalDate.now()
                )
            )
        );

    given(terminationTimelineItemService.getTimelineItemViews(List.of(firstAppointmentByCreatedTimestamp, secondAppointmentByCreatedTimestamp)))
        .willReturn(
            List.of(
                new AssetTimelineItemView(
                    TimelineEventType.TERMINATION,
                    "Termination of appointment",
                    new AssetTimelineModelProperties(),
                    terminationInstant,
                    LocalDate.now()
                )
            )
        );

    var resultingAppointmentTimelineHistory = assetTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().timelineItemViews())
        .extracting(AssetTimelineItemView::createdInstant)
        .containsExactly(
            appointmentDto2.appointmentCreatedDate(),
            terminationOfFirstAppointment.getCreatedTimestamp(),
            appointmentDto1.appointmentCreatedDate()
        );
  }
}