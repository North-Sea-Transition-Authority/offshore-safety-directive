package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationPropertiesTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellPhase;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class AppointmentCorrectionValidatorTest {

  private static final String SERVICE_MNEMONIC = "MNEM";
  private static final ServiceConfigurationProperties SERVICE_CONFIGURATION_PROPERTIES =
      ServiceConfigurationPropertiesTestUtil.builder()
          .withServiceMnemonic(SERVICE_MNEMONIC)
          .build();
  private static final ServiceBrandingConfigurationProperties SERVICE_BRANDING_CONFIGURATION_PROPERTIES =
      ServiceBrandingConfigurationPropertiesTestUtil.builder()
          .withServiceConfigurationProperties(SERVICE_CONFIGURATION_PROPERTIES)
          .build();

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private AppointmentCorrectionDateValidator appointmentCorrectionDateValidator;

  @Mock
  private NominationDetailService nominationDetailService;

  private AppointmentCorrectionValidator appointmentCorrectionValidator;

  @BeforeEach
  void setUp() {
    appointmentCorrectionValidator = new AppointmentCorrectionValidator(
        portalOrganisationUnitQueryService,
        appointmentAccessService,
        appointmentCorrectionDateValidator,
        nominationDetailService,
        SERVICE_BRANDING_CONFIGURATION_PROPERTIES
    );
  }

  @Test
  void supports_doesSupport() {
    assertTrue(appointmentCorrectionValidator.supports(AppointmentCorrectionForm.class));
  }

  @Test
  void supports_doesNotSupport() {
    assertFalse(appointmentCorrectionValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_noValidationHint() {
    var form = AppointmentCorrectionFormTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    assertThatThrownBy(() -> appointmentCorrectionValidator.validate(form, bindingResult))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expected validator hint to be used");
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "ONLINE_NOMINATION")
  void validate_whenFullyPopulatedForm_thenNoErrors(AppointmentType appointmentType) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_CONSTRUCTION.name())
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
    verify(appointmentCorrectionDateValidator).validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @Test
  void validate_whenFullyPopulatedForm_andOnlineNominationAppointmentType_thenNoErrors() {
    var onlineReference = UUID.randomUUID();
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_CONSTRUCTION.name())
        .withAppointmentType(appointmentType)
        .withOnlineNominationReference(onlineReference.toString())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    var nominationId = new NominationId(onlineReference);
    var nominationDetail = NominationDetailTestUtil.builder()
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.APPOINTED)
    ))
        .thenReturn(Optional.of(nominationDetail));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
    verify(appointmentCorrectionDateValidator).validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @Test
  void validate_whenEmptyForm_thenErrors() {
    var form = new AppointmentCorrectionForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder().build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry("appointedOperatorId", Set.of("Select the appointed operator")),
            entry("hasEndDate", Set.of("Select Yes if the appointment has an end date")),
            entry("appointmentType", Set.of("Select the type of appointment")),
            entry("forAllPhases", Set.of("Select Yes if this appointment is for all activity phases")),
            entry("reason.inputValue", Set.of("Enter a reason for the correction"))
        );

    verify(appointmentCorrectionDateValidator).validateAppointmentEndDateIsBetweenAcceptableRange(
        form,
        bindingResult
    );
    verifyNoMoreInteractions(appointmentCorrectionDateValidator);
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class)
  void validate_whenAppointedOrganisationNoLongerInPortal_thenError(AppointmentType appointmentType) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(100)
        .withForAllPhases(true)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder().build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    when(portalOrganisationUnitQueryService.getOrganisationById(100))
        .thenReturn(Optional.empty());

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .contains(
            entry("appointedOperatorId", Set.of("Select a valid operator"))
        );

    verify(appointmentCorrectionDateValidator).validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class)
  void validate_whenNotForAllPhases_andNoPhasesSelected_thenError(AppointmentType appointmentType) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withForAllPhases(false)
        .withPhases(Set.of())
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder().build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .contains(
            entry("phases", Set.of("Select at least one activity phase"))
        );

    verify(appointmentCorrectionDateValidator).validateDates(
        form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class)
  void validate_whenNotForAllPhases_andPhaseIsNotValid_thenError(AppointmentType appointmentType) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase("NOT_A_VALID_PHASE")
        .withForAllPhases(false)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .contains(
            entry("phases", Set.of("Select a valid activity phase"))
        );

    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "ONLINE_NOMINATION")
  void validate_whenNotForAllPhases_andValidPhase_thenNoErrors(AppointmentType appointmentType) {
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_DESIGN.name())
        .withForAllPhases(false)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());

    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @Test
  void validate_whenNotForAllPhases_andValidPhase_thenNoErrors() {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_DESIGN.name())
        .withForAllPhases(false)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    var nominationId = new NominationId(UUID.fromString(form.getOnlineNominationReference()));
    var nominationDetail = NominationDetailTestUtil.builder()
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.APPOINTED)
    ))
        .thenReturn(Optional.of(nominationDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());

    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class)
  void validate_whenSubareaAsset_andNotForAllPhases_andOnlyDecommissioning(AppointmentType appointmentType) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhases(Set.of(WellPhase.DECOMMISSIONING.name()))
        .withForAllPhases(false)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);
    assertThat(errorMessages)
        .contains(
            entry("phases", Set.of("Select another phase in addition to decommissioning"))
        );

    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class, mode = EnumSource.Mode.EXCLUDE, names = "ONLINE_NOMINATION")
  void validate_whenSubareaAsset_andNotForAllPhases_andDecommissioningWithOtherPhaseSelected(
      AppointmentType appointmentType
  ) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhases(Set.of(WellPhase.DECOMMISSIONING.name(), WellPhase.EXPLORATION_AND_APPRAISAL.name()))
        .withForAllPhases(false)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @Test
  void validate_whenSubareaAsset_andNotForAllPhases_andDecommissioningWithOtherPhaseSelected_andOnlineNomination() {
    var appointmentType = AppointmentType.ONLINE_NOMINATION;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhases(Set.of(WellPhase.DECOMMISSIONING.name(), WellPhase.EXPLORATION_AND_APPRAISAL.name()))
        .withForAllPhases(false)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    var nominationId = new NominationId(UUID.fromString(form.getOnlineNominationReference()));
    var nominationDetail = NominationDetailTestUtil.builder()
        .build();

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.APPOINTED)
    ))
        .thenReturn(Optional.of(nominationDetail));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, names = {"INSTALLATION", "WELLBORE"})
  void validate_whenNotSubareaAsset_andNotForAllPhases_andOnlyDecommissioning(PortalAssetType portalAssetType) {
    var appointmentType = AppointmentType.DEEMED;

    var decomPhase = switch (portalAssetType) {
      case INSTALLATION -> InstallationPhase.DECOMMISSIONING;
      case WELLBORE -> WellPhase.DECOMMISSIONING;
      default -> throw new IllegalStateException("PortalAssetType [%s] has no known decommissioning phase to use");
    };

    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhases(Set.of(decomPhase.name()))
        .withForAllPhases(false)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(portalAssetType)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentType.class)
  void validate_whenAppointedOrganisationIsDuplicate_thenError(AppointmentType appointmentType) {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointedOperatorId(100)
        .withForAllPhases(true)
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var assetDto = AssetDtoTestUtil.builder().build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var duplicatePortalOrganisationUnit = PortalOrganisationDtoTestUtil.builder()
        .isDuplicate(true)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(duplicatePortalOrganisationUnit));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .contains(
            entry("appointedOperatorId", Set.of("Select a valid operator"))
        );

    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @Test
  void validate_whenAppointmentTypeIsInvalid_thenHasError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType("INVALID_APPOINTMENT_TYPE_ENUM_NAME")
        .withPhase(InstallationPhase.DEVELOPMENT_CONSTRUCTION.name())
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry("appointmentType", Set.of("Select the type of appointment"))
        );

    verify(appointmentCorrectionDateValidator).validateAppointmentEndDateIsBetweenAcceptableRange(
        form,
        bindingResult
    );
    verifyNoMoreInteractions(appointmentCorrectionDateValidator);
  }

  @Test
  void validate_whenNoDeemedAppointments_andAppointmentTypeIsDeemed_thenNoError() {
    var appointmentType = AppointmentType.DEEMED;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of());

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
    verify(appointmentCorrectionDateValidator).validateDates(form, bindingResult, hint, appointmentType, List.of());
  }

  @Test
  void validate_whenCurrentAppointmentIsDeemed_andAppointmentTypeIsDeemed_thenNoError() {
    var appointmentType = AppointmentType.DEEMED;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_CONSTRUCTION.name())
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .withAppointmentType(appointmentType)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    assertFalse(bindingResult.hasErrors());
    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(appointmentDto)
    );
  }

  @Test
  void validate_whenAnotherAppointmentIsDeemed_andAppointmentTypeIsDeemed_thenHasError() {
    var appointmentType = AppointmentType.DEEMED;
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withPhase(InstallationPhase.DEVELOPMENT_CONSTRUCTION.name())
        .withAppointmentType(appointmentType)
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();

    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .withAssetDto(assetDto)
        .withAppointmentType(appointmentType)
        .build();

    var existingDeemedAppointmentDto = AppointmentDtoTestUtil.builder()
        .withAppointmentId(UUID.randomUUID())
        .withAssetDto(assetDto)
        .withAppointmentType(appointmentType)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(existingDeemedAppointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsExactly(
            entry("appointmentType", Set.of("You can only have one deemed appointment"))
        );

    verify(appointmentCorrectionDateValidator).validateDates(form,
        bindingResult,
        hint,
        appointmentType,
        List.of(existingDeemedAppointmentDto)
    );
  }

  @Test
  void validate_whenOnlineAppointmentType_andReferenceIsEmpty_thenHasError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withOnlineNominationReference(null)
        .build();

    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    appointmentCorrectionValidator.validate(
        form,
        bindingResult,
        hint
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsEntry("onlineNominationReference", Set.of(
            "Enter a %s nomination reference".formatted(
                SERVICE_BRANDING_CONFIGURATION_PROPERTIES.getServiceConfigurationProperties().mnemonic()
            )
        ));
  }

  @Test
  void validate_whenOnlineAppointmentType_andNoAppointedNomination_thenHasError() {

    var nominationId = new NominationId(UUID.randomUUID());
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withAppointmentType(AppointmentType.ONLINE_NOMINATION)
        .withOnlineNominationReference(null)
        .withOnlineNominationReference(nominationId.id().toString())
        .build();

    var appointmentDto = AppointmentDtoTestUtil.builder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        appointmentDto.assetDto().assetId(),
        appointmentDto.assetDto().portalAssetType()
    );

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.APPOINTED)
    )).thenReturn(Optional.empty());

    appointmentCorrectionValidator.validate(
        form,
        bindingResult,
        hint
    );

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsEntry("onlineNominationReference", Set.of(
            "Enter a valid %s nomination reference".formatted(
                SERVICE_BRANDING_CONFIGURATION_PROPERTIES.getServiceConfigurationProperties().mnemonic()
            )
        ));
  }

  @Test
  void validate_whenReasonContainsOnlyWhitespace_thenError() {
    var form = AppointmentCorrectionFormTestUtil.builder()
        .withCorrectionReason("    ")
        .build();

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var assetDto = AssetDtoTestUtil.builder()
        .withPortalAssetType(PortalAssetType.INSTALLATION)
        .build();
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(assetDto)
        .build();

    var hint = new AppointmentCorrectionValidationHint(
        appointmentDto.appointmentId(),
        assetDto.assetId(),
        assetDto.portalAssetType()
    );

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder().build();

    when(portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId()))
        .thenReturn(Optional.of(portalOrgDto));

    when(appointmentAccessService.getActiveAppointmentDtosForAsset(assetDto.assetId()))
        .thenReturn(List.of(appointmentDto));

    appointmentCorrectionValidator.validate(form, bindingResult, hint);

    var errorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(errorMessages)
        .containsEntry("reason.inputValue", Set.of("Enter a reason for the correction"));
  }

  private static class UnsupportedClass {
  }
}