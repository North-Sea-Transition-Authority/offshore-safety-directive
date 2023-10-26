package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionFormTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.AppointmentCorrectionService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

  private static final Instant FIXED_INSTANT = Instant.now();
  private static final EnumSet<AppointmentStatus> ACTIVE_APPOINTMENT_STATUSES = EnumSet.of(
      AppointmentStatus.EXTANT
  );

  @Mock
  private AppointmentRepository appointmentRepository;

  @Mock
  private NomineeDetailAccessService nomineeDetailAccessService;

  @Mock
  private AssetRepository assetRepository;

  @Mock
  private AppointmentCorrectionService appointmentCorrectionService;

  @Mock
  private AppointmentRemovedEventPublisher appointmentRemovedEventPublisher;

  @Mock
  private AppointmentAddedEventPublisher appointmentAddedEventPublisher;

  private AppointmentService appointmentService;

  @BeforeEach
  void setup() {

    var clock = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());

    appointmentService = new AppointmentService(
        appointmentRepository,
        nomineeDetailAccessService,
        assetRepository,
        clock,
        appointmentRemovedEventPublisher,
        appointmentAddedEventPublisher,
        appointmentCorrectionService);
  }

  @Test
  void confirmAppointmentsForNomination_whenExistingAppointments_verifyEnded() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var newAppointmentConfirmationDate = LocalDate.now().minusDays(1);
    var asset = AssetTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder().build();
    var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

    var existingAppointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withResponsibleFromDate(newAppointmentConfirmationDate.minusDays(1))
        .withResponsibleToDate(null)
        .build();

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(
        List.of(asset),
        ACTIVE_APPOINTMENT_STATUSES
    ))
        .thenReturn(List.of(existingAppointment));

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetailDto));

    var appointments = appointmentService.createAppointmentsFromNomination(
        nominationDetail,
        newAppointmentConfirmationDate,
        List.of(asset)
    );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Appointment>> appointmentsPersisted = ArgumentCaptor.forClass(List.class);
    verify(appointmentRepository).saveAll(appointmentsPersisted.capture());

    // then the existing appointments for the asset are ended
    // and the new appointment created
    assertThat(appointmentsPersisted.getValue())
        .extracting(
            Appointment::getAsset,
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate,
            Appointment::getCreatedByNominationId,
            Appointment::getAppointmentType,
            Appointment::getAppointedPortalOperatorId,
            Appointment::getCreatedDatetime,
            Appointment::getAppointmentStatus
        )
        .containsExactly(
            tuple(
                asset,
                existingAppointment.getResponsibleFromDate(),
                newAppointmentConfirmationDate,
                existingAppointment.getCreatedByNominationId(),
                existingAppointment.getAppointmentType(),
                existingAppointment.getAppointedPortalOperatorId(),
                existingAppointment.getCreatedDatetime(),
                AppointmentStatus.EXTANT
            ),
            tuple(
                asset,
                newAppointmentConfirmationDate,
                null,
                nominationDetail.getNomination().getId(),
                AppointmentType.ONLINE_NOMINATION,
                nomineeDetailDto.nominatedOrganisationId().id(),
                FIXED_INSTANT,
                AppointmentStatus.EXTANT
            )
        );

    // and only the new appointment is returned
    assertThat(appointments)
        .extracting(
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate
        )
        .containsExactly(
            tuple(newAppointmentConfirmationDate, null)
        );
  }

  @Test
  void confirmAppointmentsForNomination_whenNoExistingAppointments_verifyNoExistingUpdated() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var confirmationDate = LocalDate.now().minusDays(1);
    var asset = AssetTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder().build();
    var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(
        List.of(asset),
        ACTIVE_APPOINTMENT_STATUSES
    ))
        .thenReturn(List.of());

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetailDto));

    var appointments = appointmentService.createAppointmentsFromNomination(nominationDetail, confirmationDate, List.of(asset));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Appointment>> appointmentsPersisted = ArgumentCaptor.forClass(List.class);
    verify(appointmentRepository, times(1)).saveAll(appointmentsPersisted.capture());

    var savedNewAppointments = appointmentsPersisted.getAllValues().get(0);

    assertThat(savedNewAppointments)
        .extracting(
            Appointment::getAsset,
            Appointment::getResponsibleFromDate,
            Appointment::getResponsibleToDate,
            Appointment::getCreatedByNominationId,
            Appointment::getAppointmentType,
            Appointment::getAppointedPortalOperatorId,
            Appointment::getCreatedDatetime
        )
        .containsExactly(
            tuple(
                asset,
                confirmationDate,
                null,
                nominationDetail.getNomination().getId(),
                AppointmentType.ONLINE_NOMINATION,
                nomineeDetailDto.nominatedOrganisationId().id(),
                FIXED_INSTANT
            )
        );

    assertThat(appointments).isEqualTo(appointmentsPersisted.getValue());
  }

  @Test
  void confirmAppointmentsForNomination_whenNoNomineeDetailDto_verifyError() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    var confirmationDate = LocalDate.now().minusDays(1);
    var asset = AssetTestUtil.builder().build();
    var assetList = List.of(asset);

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> appointmentService.createAppointmentsFromNomination(nominationDetail, confirmationDate, assetList))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to get NomineeDetailDto for NominationDetail [%s]".formatted(
            nominationDetailDto.nominationDetailId()
        ));
  }

  @Test
  void addManualAppointment_verifyCalls() {
    var portalAssetId = "123";
    var portalAssetType = PortalAssetType.INSTALLATION;

    var form = AppointmentCorrectionFormTestUtil.builder().build();
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetId(portalAssetId)
        .withPortalAssetType(portalAssetType)
        .build();

    var appointment = AppointmentTestUtil.builder().build();

    var appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);

    when(appointmentCorrectionService.applyCorrectionToAppointment(eq(form), eq(assetDto), appointmentArgumentCaptor.capture()))
        .thenReturn(appointment);

    appointmentService.addManualAppointment(
        form,
        assetDto
    );

    verify(appointmentCorrectionService).applyCorrectionToAppointment(form, assetDto, appointmentArgumentCaptor.getValue());
    verify(appointmentAddedEventPublisher).publish(new AppointmentId(appointment.getId()));
  }

  @Test
  void removeAppointment_whenAppointmentIsExtant_thenVerify() {
    var appointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.EXTANT)
        .build();
    appointmentService.removeAppointment(appointment);

    assertThat(appointment.getAppointmentStatus()).isEqualTo(AppointmentStatus.REMOVED);
    verify(appointmentRepository).save(appointment);
    verify(appointmentRemovedEventPublisher).publish(new AppointmentId(appointment.getId()));
  }

  @Test
  void removeAppointment_whenAppointmentAlreadyRemoved_thenError() {
    var appointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.REMOVED)
        .build();

    assertThatThrownBy(() -> appointmentService.removeAppointment(appointment))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Appointment with ID [%s] has already been removed".formatted(appointment.getId()));
  }

  @Test
  void removeAppointment_whenAppointmentTerminated_thenError() {
    var appointment = AppointmentTestUtil.builder()
        .withAppointmentStatus(AppointmentStatus.TERMINATED)
        .build();

    assertThatThrownBy(() -> appointmentService.removeAppointment(appointment))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Appointment with ID [%s] cannot be removed as it has been terminated".formatted(appointment.getId()));
  }

  @Test
  void endAppointmentsForSubareas_whenActiveAppointmentsWithExtantStatus_thenEndAppointment_andRemoveAssets() {
    var licenceBlockSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();
    var asset = AssetTestUtil.builder()
        .withPortalAssetId(licenceBlockSubarea.subareaId().id())
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .withResponsibleToDate(null)
        .build();

    when(assetRepository.findByPortalAssetIdInAndPortalAssetType(
        List.of(licenceBlockSubarea.subareaId().id()), PortalAssetType.SUBAREA
    )).thenReturn(List.of(asset));

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(
        List.of(asset),
        EnumSet.of(AppointmentStatus.EXTANT)
    )).thenReturn(List.of(appointment));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Appointment>> appointmentCaptor = ArgumentCaptor.forClass(List.class);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Asset>> assetCaptor = ArgumentCaptor.forClass(List.class);

    appointmentService.endAppointmentsForSubareas(List.of(licenceBlockSubarea), "correctionId");

    verify(appointmentRepository).saveAll(appointmentCaptor.capture());

    assertThat(appointmentCaptor.getValue().get(0)).isInstanceOf(Appointment.class);

    var resultingAppointment = (Appointment) appointmentCaptor.getValue().get(0);
    assertThat(resultingAppointment.getId()).isEqualTo(appointment.getId());
    assertThat(resultingAppointment.getAsset()).isEqualTo(appointment.getAsset());
    assertThat(resultingAppointment.getResponsibleToDate()).isEqualTo(LocalDate.now());

    verify(assetRepository).saveAll(assetCaptor.capture());

    var resultingAsset = assetCaptor.getValue().get(0);
    assertThat(resultingAsset.getId()).isEqualTo(asset.getId());
    assertThat(resultingAsset.getStatus()).isEqualTo(asset.getStatus());
    assertThat(resultingAsset.getPortalEventType()).isEqualTo(PortalEventType.PEARS_CORRECTION);
    assertThat(resultingAsset.getPortalEventId()).isEqualTo("correctionId");
  }

  @Test
  void endAppointmentsForSubareas_whenNoActiveAppointmentsWithExtantStatus_andAssetsExist_thenRemoveAssets() {
    var licenceBlockSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();
    var asset = AssetTestUtil.builder()
        .withPortalAssetId(licenceBlockSubarea.subareaId().id())
        .build();

    when(assetRepository.findByPortalAssetIdInAndPortalAssetType(
        List.of(licenceBlockSubarea.subareaId().id()), PortalAssetType.SUBAREA
    )).thenReturn(List.of(asset));

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(
        List.of(asset),
        EnumSet.of(AppointmentStatus.EXTANT)
    )).thenReturn(List.of());

    appointmentService.endAppointmentsForSubareas(List.of(licenceBlockSubarea), "correctionId");

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Asset>> assetCaptor = ArgumentCaptor.forClass(List.class);

    verify(appointmentRepository, never()).saveAll(any());
    verify(assetRepository).saveAll(assetCaptor.capture());

    var resultingAsset = assetCaptor.getValue().get(0);
    assertThat(resultingAsset.getId()).isEqualTo(asset.getId());
    assertThat(resultingAsset.getStatus()).isEqualTo(AssetStatus.REMOVED);
    assertThat(resultingAsset.getPortalEventType()).isEqualTo(PortalEventType.PEARS_CORRECTION);
    assertThat(resultingAsset.getPortalEventId()).isEqualTo("correctionId");
  }

  @Test
  void endAppointmentsForSubareas_whenNoActiveAppointmentsWithExtantStatus_andNoAssetsExist_thenDoNothing() {
    var licenceBlockSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    when(assetRepository.findByPortalAssetIdInAndPortalAssetType(
        List.of(licenceBlockSubarea.subareaId().id()), PortalAssetType.SUBAREA
    )).thenReturn(List.of());

    when(appointmentRepository.findAllByAssetInAndResponsibleToDateIsNullAndAppointmentStatusIn(
        List.of(),
        EnumSet.of(AppointmentStatus.EXTANT)
    )).thenReturn(List.of());

    appointmentService.endAppointmentsForSubareas(List.of(licenceBlockSubarea), "correctionId");

    verify(appointmentRepository, never()).saveAll(any());
    verify(assetRepository, never()).saveAll(any());
  }
}