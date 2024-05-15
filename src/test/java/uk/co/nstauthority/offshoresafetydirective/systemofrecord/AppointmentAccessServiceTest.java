package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class AppointmentAccessServiceTest {

  private static final EnumSet<AppointmentStatus> ACTIVE_STATUSES = EnumSet.of(
      AppointmentStatus.EXTANT,
      AppointmentStatus.TERMINATED
  );

  @Mock
  private AppointmentRepository appointmentRepository;

  @InjectMocks
  private AppointmentAccessService appointmentAccessService;

  @Test
  void getActiveAppointmentDtosForAsset_whenNoAppointments_themEmptyListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    given(appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(
        assetId.id(),
        ACTIVE_STATUSES
    ))
        .willReturn(Collections.emptyList());

    var resultingAppointmentDtos = appointmentAccessService.getActiveAppointmentDtosForAsset(assetId);

    assertThat(resultingAppointmentDtos).isEmpty();
  }

  @Test
  void getActiveAppointmentDtosForAsset_whenAppointments_thenPopulatedListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    var expectedAppointmentDtos = AppointmentTestUtil.builder().build();

    given(appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(
        assetId.id(),
        ACTIVE_STATUSES
    ))
        .willReturn(List.of(expectedAppointmentDtos));

    var resultingAppointmentDtos = appointmentAccessService.getActiveAppointmentDtosForAsset(assetId);

    assertThat(resultingAppointmentDtos)
        .extracting(
            appointmentDto -> appointmentDto.appointmentId().id(),
            appointmentDto -> appointmentDto.appointedOperatorId().id(),
            appointmentDto -> appointmentDto.appointmentFromDate().value(),
            appointmentDto -> appointmentDto.appointmentToDate().value(),
            AppointmentDto::appointmentCreatedDate,
            AppointmentDto::assetDto
        )
        .containsExactly(
            tuple(
                expectedAppointmentDtos.getId(),
                String.valueOf(expectedAppointmentDtos.getAppointedPortalOperatorId()),
                expectedAppointmentDtos.getResponsibleFromDate(),
                expectedAppointmentDtos.getResponsibleToDate(),
                expectedAppointmentDtos.getCreatedDatetime(),
                AssetDto.fromAsset(expectedAppointmentDtos.getAsset())
            )
        );
  }

  @Test
  void getAppointment_appointmentFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder().withId(appointmentId.id()).build();

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.of(appointment));

    assertThat(appointmentAccessService.getAppointment(appointmentId)).contains(appointment);
  }

  @Test
  void getAppointment_appointmentNotFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentRepository.findById(appointmentId.id())).thenReturn(Optional.empty());

    assertThat(appointmentAccessService.getAppointment(appointmentId)).isEmpty();
  }

  @Test
  void findAppointmentDtoById_appointmentFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    var asset = AssetTestUtil.builder().build();

    var appointedOperatorId = 100;
    var fromDate = LocalDate.now();
    var toDate = LocalDate.now();
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var nominationId = UUID.randomUUID();
    var createdDatetime = Instant.now();
    var legacyReference = "legacy reference";
    var appointmentStatus = AppointmentStatus.EXTANT;

    var appointment = AppointmentTestUtil.builder()
        .withId(appointmentId.id())
        .withAsset(asset)
        .withAppointedPortalOperatorId(appointedOperatorId)
        .withResponsibleFromDate(fromDate)
        .withResponsibleToDate(toDate)
        .withAppointmentType(appointmentType)
        .withCreatedByNominationId(nominationId)
        .withCreatedByLegacyNominationReference(legacyReference)
        .withCreatedByAppointmentId(appointmentId.id())
        .withCreatedDatetime(createdDatetime)
        .withAppointmentStatus(appointmentStatus)
        .build();

    when(appointmentRepository.findById(appointmentId.id()))
        .thenReturn(Optional.of(appointment));

    var result = appointmentAccessService.findAppointmentDtoById(appointmentId);

    assertThat(result).isNotEmpty();

    PropertyObjectAssert.thenAssertThat(result.get())
        .hasFieldOrPropertyWithValue("appointmentId", appointmentId)
        .hasFieldOrPropertyWithValue(
            "appointedOperatorId",
            new AppointedOperatorId(String.valueOf(appointedOperatorId))
        )
        .hasFieldOrPropertyWithValue("appointmentFromDate", new AppointmentFromDate(fromDate))
        .hasFieldOrPropertyWithValue("appointmentToDate", new AppointmentToDate(toDate))
        .hasFieldOrPropertyWithValue("appointmentCreatedDate", createdDatetime)
        .hasFieldOrPropertyWithValue("appointmentType", appointmentType)
        .hasFieldOrPropertyWithValue("legacyNominationReference", legacyReference)
        .hasFieldOrPropertyWithValue("nominationId", new NominationId(nominationId))
        .hasFieldOrPropertyWithValue("assetDto", AssetDto.fromAsset(asset))
        .hasFieldOrPropertyWithValue("appointmentStatus", appointmentStatus)
        .hasFieldOrPropertyWithValue("createdByAppointmentId", appointmentId)
        .hasAssertedAllProperties();
  }

  @Test
  void findAppointmentDtoById_appointmentNotFound() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentRepository.findById(appointmentId.id()))
        .thenReturn(Optional.empty());

    var result = appointmentAccessService.findAppointmentDtoById(appointmentId);

    assertThat(result).isEmpty();
  }

  @Test
  void getActiveAppointmentsForAsset_whenNoAppointments_themEmptyListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    given(appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(
        assetId.id(),
        ACTIVE_STATUSES
    ))
        .willReturn(Collections.emptyList());

    var resultingAppointments = appointmentAccessService.getAppointmentsForAsset(assetId);

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void getActiveAppointmentsForAsset_whenAppointments_thenPopulatedListReturned() {

    var assetId = new AssetId(UUID.randomUUID());

    var expectedAppointments = AppointmentTestUtil.builder().build();

    given(appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(
        assetId.id(),
        ACTIVE_STATUSES
    ))
        .willReturn(List.of(expectedAppointments));

    var resultingAppointments = appointmentAccessService.getAppointmentsForAsset(assetId);

    assertThat(resultingAppointments)
        .extracting(
            Appointment::getId,
            Appointment::getAppointmentType,
            Appointment::getAsset,
            Appointment::getAppointedPortalOperatorId,
            Appointment::getCreatedDatetime,
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate
        )
        .containsExactly(
            tuple(
                expectedAppointments.getId(),
                expectedAppointments.getAppointmentType(),
                expectedAppointments.getAsset(),
                expectedAppointments.getAppointedPortalOperatorId(),
                expectedAppointments.getCreatedDatetime(),
                expectedAppointments.getResponsibleFromDate(),
                expectedAppointments.getResponsibleToDate()
            )
        );
  }

  @Test
  void getAppointmentsForAsset_whenNoAppointmentFound_thenReturnEmptyList() {
    var portalAssetIds = List.of("portal-asset-id");
    var statuses = EnumSet.of(AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED);

    when(appointmentRepository.findAppointmentsByAppointmentStatusInAndAsset_PortalAssetIdInAndAsset_PortalAssetType(
        statuses,
        portalAssetIds,
        PortalAssetType.SUBAREA)
    )
        .thenReturn(List.of());

    var resultingAppointments = appointmentAccessService.getAppointmentsForAssets(
        statuses,
        portalAssetIds,
        PortalAssetType.SUBAREA
    );

    assertThat(resultingAppointments).isEmpty();
  }

  @Test
  void getAppointmentsForAsset_whenAppointmentFound_thenReturnList() {
    var portalAssetIds = List.of("portal-asset-id");
    var statuses = EnumSet.of(AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED);

    var expectedAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .build();

    when(appointmentRepository.findAppointmentsByAppointmentStatusInAndAsset_PortalAssetIdInAndAsset_PortalAssetType(
        statuses,
        portalAssetIds,
        PortalAssetType.SUBAREA)
    )
        .thenReturn(List.of(expectedAppointment));

    var resultingAppointments = appointmentAccessService.getAppointmentsForAssets(
        statuses,
        portalAssetIds,
        PortalAssetType.SUBAREA
    );

    assertThat(resultingAppointments)
        .extracting(
            Appointment::getId,
            Appointment::getAsset,
            Appointment::getAppointmentStatus
        )
        .contains(
            tuple(
                expectedAppointment.getId(),
                expectedAppointment.getAsset(),
                expectedAppointment.getAppointmentStatus()
            ));
  }

  @Test
  void getActiveAppointments_whenAppointmentFound_thenReturn() {
    var status = AppointmentStatus.EXTANT;
    var expectedAppointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(status)
        .build();

    when(appointmentRepository.findByIdAndAppointmentStatus(expectedAppointment.getId(), status))
        .thenReturn(Optional.of(expectedAppointment));

    var resultingAppointments = appointmentAccessService.getAppointmentByStatus(
        new AppointmentId(expectedAppointment.getId()),
        AppointmentStatus.EXTANT
    );

    assertThat(resultingAppointments).contains(expectedAppointment);
  }

  @Test
  void getActiveAppointments_whenNoAppointmentFound_thenReturnEmptyOptional() {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var status = AppointmentStatus.EXTANT;
    when(appointmentRepository.findByIdAndAppointmentStatus(appointmentId.id(), status))
        .thenReturn(Optional.empty());

    var resultingAppointments = appointmentAccessService.getAppointmentByStatus(appointmentId, status);

    assertTrue(resultingAppointments.isEmpty());
  }

  @Test
  void getCurrentAppointmentForAsset_whenAppointmentIsNotEnded_thenAssertContent() {
    var assetId = new AssetId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(null)
        .build();

    when(appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(
        assetId.id(),
        EnumSet.of(AppointmentStatus.EXTANT)
    ))
        .thenReturn(List.of(appointment));

    var result = appointmentAccessService.getCurrentAppointmentForAsset(assetId);
    assertThat(result).contains(appointment);
  }

  @Test
  void getCurrentAppointmentForAsset_whenAppointmentIsEnded_thenAssertEmpty() {
    var assetId = new AssetId(UUID.randomUUID());
    var appointment = AppointmentTestUtil.builder()
        .withResponsibleToDate(LocalDate.now().minusDays(1))
        .build();

    when(appointmentRepository.findAllByAsset_idAndAppointmentStatusIn(
        assetId.id(),
        EnumSet.of(AppointmentStatus.EXTANT)
    ))
        .thenReturn(List.of(appointment));

    var result = appointmentAccessService.getCurrentAppointmentForAsset(assetId);
    assertThat(result).isEmpty();
  }

}
