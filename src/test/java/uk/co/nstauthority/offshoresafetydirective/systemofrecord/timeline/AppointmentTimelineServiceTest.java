package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@ExtendWith(MockitoExtension.class)
class AppointmentTimelineServiceTest {

  @Mock
  private PortalAssetNameService portalAssetNameService;

  @Mock
  private AssetAccessService assetAccessService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @InjectMocks
  private AppointmentTimelineService appointmentTimelineService;

  @Test
  void getAppointmentHistoryForPortalAsset_whenNotInPortalOrSystemOfRecord_thenEmptyOptional() {

    var portalAssetId = new PortalAssetId("something not in sor or portal");
    var portalAssetType = PortalAssetType.INSTALLATION;

    given(portalAssetNameService.getAssetName(portalAssetId, portalAssetType))
        .willReturn(Optional.empty());

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.empty());

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
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

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
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

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(Collections.emptyList());

    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().appointments()).isEmpty();

    then(organisationUnitQueryService).shouldHaveNoInteractions();
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointment_thenPopulatedAppointmentViewList() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    // given an appointment with an operator known to the portal

    var appointedOperatorId = new PortalOrganisationUnitId(100);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    var expectedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(appointedOperatorId.id())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(expectedAppointment));

    // when we request the timeline history
    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then the expected appointment is returned
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().appointments())
        .extracting(
            AppointmentView::appointmentId,
            AppointmentView::portalAssetId,
            AppointmentView::appointedOperatorName,
            AppointmentView::appointmentFromDate,
            AppointmentView::appointmentToDate
        )
        .containsExactly(
            tuple(
                expectedAppointment.appointmentId(),
                expectedAppointment.portalAssetId(),
                appointedOperator.name(),
                expectedAppointment.appointmentFromDate(),
                expectedAppointment.appointmentToDate()
            )
        );
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenAppointmentButOperatorNotInPortal_thenUnknownOperatorName() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    // given an appointment with an operator not known to the portal

    var appointedOperatorId = new PortalOrganisationUnitId(-1);

    var expectedAppointment = AppointmentDtoTestUtil.builder()
        .withAppointedOperatorId(appointedOperatorId.id())
        .build();

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(Collections.emptyList());

    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(expectedAppointment));

    // when we request the timeline history
    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then the operator name will be a sensible default string
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().appointments())
        .extracting(AppointmentView::appointedOperatorName)
        .containsExactly("Unknown operator");
  }

  @Test
  void getAppointmentHistoryForPortalAsset_whenMultipleAppointments_thenOrderedByDescendingCreatedDatetime() {

    var portalAssetId = new PortalAssetId("something from system of record");

    var assetInSystemOfRecord = AssetDtoTestUtil.builder()
        .withAssetName("from system of record")
        .build();

    given(assetAccessService.getAsset(portalAssetId))
        .willReturn(Optional.of(assetInSystemOfRecord));

    var appointedOperatorId = new PortalOrganisationUnitId(100);

    var appointedOperator = PortalOrganisationDtoTestUtil.builder()
        .withId(appointedOperatorId.id())
        .build();

    // given multiple appointments with the same start date

    var appointmentDate = LocalDate.of(2023, 2, 24);

    var firstAppointmentByCreationDate = AppointmentDtoTestUtil.builder()
        .withAppointmentCreatedDatetime(Instant.now().minus(1, ChronoUnit.HOURS))
        .withAppointmentFromDate(appointmentDate)
        .withAppointmentToDate(appointmentDate)
        .withAppointedOperatorId(appointedOperatorId.id())
        .withAppointmentId(UUID.randomUUID())
        .build();

    var secondAppointmentByCreationDate = AppointmentDtoTestUtil.builder()
        .withAppointmentCreatedDatetime(Instant.now().plus(1, ChronoUnit.HOURS))
        .withAppointmentFromDate(appointmentDate)
        .withAppointmentToDate(appointmentDate)
        .withAppointedOperatorId(appointedOperatorId.id())
        .withAppointmentId(UUID.randomUUID())
        .build();

    // and the appointments are returned out of order
    given(appointmentAccessService.getAppointmentsForAsset(assetInSystemOfRecord.assetId()))
        .willReturn(List.of(firstAppointmentByCreationDate, secondAppointmentByCreationDate));

    given(organisationUnitQueryService.getOrganisationByIds(Set.of(appointedOperatorId)))
        .willReturn(List.of(appointedOperator));

    // when we request the timeline history
    var resultingAppointmentTimelineHistory = appointmentTimelineService.getAppointmentHistoryForPortalAsset(
        portalAssetId,
        PortalAssetType.INSTALLATION
    );

    // then the appointments are sorted by creation date descending
    assertThat(resultingAppointmentTimelineHistory).isPresent();
    assertThat(resultingAppointmentTimelineHistory.get().appointments())
        .extracting(AppointmentView::appointmentId)
        .containsExactly(
            secondAppointmentByCreationDate.appointmentId(),
            firstAppointmentByCreationDate.appointmentId()
        );

  }

}