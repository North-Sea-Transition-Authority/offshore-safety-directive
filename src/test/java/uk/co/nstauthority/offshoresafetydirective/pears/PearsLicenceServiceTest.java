package uk.co.nstauthority.offshoresafetydirective.pears;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.fivium.energyportalmessagequeue.message.pears.PearsCorrectionAppliedEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentService;

@ExtendWith(MockitoExtension.class)
class PearsLicenceServiceTest {

  @Mock
  private LicenceQueryService licenceQueryService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private AppointmentService appointmentService;

  @InjectMocks
  private PearsLicenceService pearsLicenceService;

  @Test
  void handlePearsCorrectionApplied_whenHasNonExtantSubareas_thenVerifyAppointmentsEnded() {
    var licenceId = 123;
    var licenceDto = LicenceDtoTestUtil.builder().build();

    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_CORRECTION_APPLIED_PURPOSE))
        .thenReturn(Optional.of(licenceDto));

    var blockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();
    when(licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
        licenceDto.licenceReference().value(),
        List.of(SubareaStatus.NOT_EXTANT)
    ))
        .thenReturn(List.of(blockSubareaDto));

    var message = new PearsCorrectionAppliedEpmqMessage(
        String.valueOf(licenceId),
        null,
        null,
        null
    );

    pearsLicenceService.handlePearsCorrectionApplied(message);

    verify(appointmentService).endAppointmentsForSubareas(List.of(blockSubareaDto), message.getCorrectionId());
  }

  @Test
  void handlePearsCorrectionApplied_whenHasNoNonExtantSubareas_thenVerifyCalls() {
    var licenceId = 123;
    var licenceDto = LicenceDtoTestUtil.builder().build();

    when(licenceQueryService.getLicenceById(
        new LicenceId(licenceId),
        PearsLicenceService.PEARS_CORRECTION_APPLIED_PURPOSE))
        .thenReturn(Optional.of(licenceDto));

    when(licenceBlockSubareaQueryService.searchSubareasByLicenceReferenceWithStatuses(
        licenceDto.licenceReference().value(),
        List.of(SubareaStatus.NOT_EXTANT)
    ))
        .thenReturn(List.of());

    var message = new PearsCorrectionAppliedEpmqMessage(
        String.valueOf(licenceId),
        null,
        null,
        null
    );

    pearsLicenceService.handlePearsCorrectionApplied(message);

    verifyNoInteractions(appointmentService);
  }

  @Test
  void handlePearsCorrectionApplied_whenLicenceIdIsNotFound_thenVerifyNoCalls() {
    var licenceId = 123;

    when(licenceQueryService.getLicenceById(
            new LicenceId(licenceId),
            PearsLicenceService.PEARS_CORRECTION_APPLIED_PURPOSE))
        .thenReturn(Optional.empty());

    var message = new PearsCorrectionAppliedEpmqMessage(
        String.valueOf(licenceId),
        null,
        null,
        null
    );

    pearsLicenceService.handlePearsCorrectionApplied(message);

    verifyNoInteractions(appointmentService, licenceBlockSubareaQueryService);
  }
}