package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class ForwardApprovedAppointmentRestServiceTest {

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @InjectMocks
  private ForwardApprovedAppointmentRestService forwardApprovedAppointmentRestService;

  @Test
  void searchSubareaAppointments_whenNoSubareasFoundInPortal_returnEmptyList() throws Exception {
    var searchTerm = "some subarea name";
    when(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestService.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .thenReturn(Set.of());

    var results = forwardApprovedAppointmentRestService.searchSubareaAppointments(searchTerm);

    assertThat(results).isEmpty();
  }

  @Test
  void searchSubareaAppointments_whenMultipleResultForSameBlockAndSubareaName_thenSortedByLicence() throws Exception {
    var searchTerm = "matching subareas";

    // given multiple different licences
    // then the results are sorted first by type and then number

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(1)
        .withSubareaId("1")
        .build();

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(2)
        .withSubareaId("2")
        .build();

    var thirdSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withSubareaId("3")
        .build();

    var fourthSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(1)
        .withSubareaId("4")
        .build();

    var unsortedSubareaList = Set.of(
        fourthSubareaByLicence,
        thirdSubareaByLicence,
        secondSubareaByLicence,
        firstSubareaByLicence
    );

    given(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestService.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .willReturn(unsortedSubareaList);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> portalAssetIdCaptor = ArgumentCaptor.forClass(List.class);

    when(appointmentAccessService.getAppointmentsForAssets(
        eq(ForwardApprovedAppointmentRestService.STATUSES),
        portalAssetIdCaptor.capture(),
        eq(PortalAssetType.SUBAREA))
    )
        .thenReturn(List.of(AppointmentTestUtil.builder().build()));

    forwardApprovedAppointmentRestService.searchSubareaAppointments(searchTerm);

    assertThat(portalAssetIdCaptor.getAllValues())
        .containsExactly(
            List.of(
                firstSubareaByLicence.subareaId().id(),
                secondSubareaByLicence.subareaId().id(),
                thirdSubareaByLicence.subareaId().id(),
                fourthSubareaByLicence.subareaId().id()
            ));
  }

  @Test
  void searchSubareaAppointments_whenMultipleResultForSameLicenceAndSubareaName_thenSortedByBlockComponents() throws Exception {
    var searchTerm = "matching subareas";

    // given multiple different blocks
    // then the results are sorted first by quadrant number, then block number then block suffix

    var firstSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withSubareaId("1")
        .withBlockNumber(1)
        .withBlockSuffix(null)
        .build();

    var secondSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("a")
        .withSubareaId("2")
        .build();

    var thirdSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("B")
        .withSubareaId("3")
        .build();

    var fourthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(2)
        .withSubareaId("4")
        .build();

    var fifthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("10")
        .withSubareaId("5")
        .build();

    var sixthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("2")
        .withSubareaId("6")
        .build();

    var unsortedSubareaList = Set.of(
        sixthSubareaByBlock,
        firstSubareaByBlock,
        thirdSubareaByBlock,
        secondSubareaByBlock,
        fifthSubareaByBlock,
        fourthSubareaByBlock
    );

    given(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestService.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .willReturn(unsortedSubareaList);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> portalAssetIdCaptor = ArgumentCaptor.forClass(List.class);

    when(appointmentAccessService.getAppointmentsForAssets(
        eq(ForwardApprovedAppointmentRestService.STATUSES),
        portalAssetIdCaptor.capture(),
        eq(PortalAssetType.SUBAREA))
    )
        .thenReturn(List.of(AppointmentTestUtil.builder().build()));

    forwardApprovedAppointmentRestService.searchSubareaAppointments(searchTerm);

    assertThat(portalAssetIdCaptor.getAllValues())
        .containsExactly(
            List.of(
                firstSubareaByBlock.subareaId().id(),
                secondSubareaByBlock.subareaId().id(),
                thirdSubareaByBlock.subareaId().id(),
                fourthSubareaByBlock.subareaId().id(),
                fifthSubareaByBlock.subareaId().id(),
                sixthSubareaByBlock.subareaId().id()
            ));
  }

  @Test
  void searchSubareaAppointments_whenMultipleResultForSameLicenceAndBlock_thenSortedBySubareaName() throws Exception {
    var searchTerm = "matching subareas";

    // given multiple different subarea names
    // then the results are sorted by subarea name

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("1")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .withSubareaId("2")
        .build();

    var thirdSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("c name")
        .withSubareaId("3")
        .build();

    var unsortedSubareaList = Set.of(
        thirdSubareaByName,
        secondSubareaByName,
        firstSubareaByName
    );

    given(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestService.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .willReturn(unsortedSubareaList);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> portalAssetIdCaptor = ArgumentCaptor.forClass(List.class);

    when(appointmentAccessService.getAppointmentsForAssets(
        eq(ForwardApprovedAppointmentRestService.STATUSES),
        portalAssetIdCaptor.capture(),
        eq(PortalAssetType.SUBAREA))
    )
        .thenReturn(List.of(AppointmentTestUtil.builder().build()));

    forwardApprovedAppointmentRestService.searchSubareaAppointments(searchTerm);

    assertThat(portalAssetIdCaptor.getAllValues())
        .containsExactly(
            List.of(
                firstSubareaByName.subareaId().id(),
                secondSubareaByName.subareaId().id(),
                thirdSubareaByName.subareaId().id()
            ));
  }

  @Test
  void searchSubareaAppointments_whenSubareasFoundInPortal_thenReturnList_andAssertSortedOrderForAppointments() throws Exception {
    var searchTerm = "some subarea name";

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("1")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .withSubareaId("2")
        .build();

    var unsortedSubareaList = Set.of(
        secondSubareaByName,
        firstSubareaByName
    );

    when(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestService.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .thenReturn(unsortedSubareaList);

    var firstSubareaByNameAsset = AssetTestUtil.builder()
        .withPortalAssetId(firstSubareaByName.subareaId().id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();

    var secondSubareaByNameAsset = AssetTestUtil.builder()
        .withPortalAssetId(secondSubareaByName.subareaId().id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();

    var portalAssetIds = List.of(firstSubareaByNameAsset.getPortalAssetId(), secondSubareaByNameAsset.getPortalAssetId());

    var latestAppointmentForFirstAsset = AppointmentTestUtil.builder()
        .withAsset(secondSubareaByNameAsset)
        .withResponsibleFromDate(LocalDate.now())
        .build();

    var earliestAppointmentForFirstAsset = AppointmentTestUtil.builder()
        .withAsset(secondSubareaByNameAsset)
        .withResponsibleFromDate(LocalDate.now().minusDays(2L))
        .build();

    var appointmentForSecondAsset = AppointmentTestUtil.builder()
        .withAsset(firstSubareaByNameAsset)
        .withResponsibleFromDate(LocalDate.now())
        .build();

    when(appointmentAccessService.getAppointmentsForAssets(
        ForwardApprovedAppointmentRestService.STATUSES,
        portalAssetIds,
        PortalAssetType.SUBAREA)
    )
        .thenReturn(List.of(latestAppointmentForFirstAsset, earliestAppointmentForFirstAsset, appointmentForSecondAsset));

    var results = forwardApprovedAppointmentRestService.searchSubareaAppointments(searchTerm);

    assertThat(results)
        .extracting(RestSearchItem::text)
        .containsExactly(
            ForwardApprovedAppointmentRestService.SEARCH_DISPLAY_STRING
                .formatted(firstSubareaByName.displayName(),
                    DateUtil.formatLongDate(appointmentForSecondAsset.getResponsibleFromDate())),
            ForwardApprovedAppointmentRestService.SEARCH_DISPLAY_STRING
                .formatted(secondSubareaByName.displayName(),
                    DateUtil.formatLongDate(earliestAppointmentForFirstAsset.getResponsibleFromDate())),
            ForwardApprovedAppointmentRestService.SEARCH_DISPLAY_STRING
                .formatted(secondSubareaByName.displayName(),
                    DateUtil.formatLongDate(latestAppointmentForFirstAsset.getResponsibleFromDate()))
        );
  }

  @Test
  void isValidSubareaAppointmentId_appointmentDoesNotExist() {
    var appointmentId = new AppointmentId(UUID.randomUUID());

    when(appointmentAccessService.findAppointmentDtoById(appointmentId)).thenReturn(Optional.empty());

    assertThat(forwardApprovedAppointmentRestService.isValidSubareaAppointmentId(appointmentId)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = PortalAssetType.class, names = "SUBAREA", mode = EnumSource.Mode.EXCLUDE)
  void isValidSubareaAppointmentId_appointmentAssetHasIncorrectType(PortalAssetType portalAssetType) {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(AssetDtoTestUtil.builder()
            .withPortalAssetType(portalAssetType)
            .build())
        .build();

    when(appointmentAccessService.findAppointmentDtoById(appointmentId)).thenReturn(Optional.of(appointmentDto));

    assertThat(forwardApprovedAppointmentRestService.isValidSubareaAppointmentId(appointmentId)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentStatus.class, names = { "EXTANT", "TERMINATED" }, mode = EnumSource.Mode.EXCLUDE)
  void isValidSubareaAppointmentId_appointmentHasIncorrectStatus(AppointmentStatus appointmentStatus) {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(AssetDtoTestUtil.builder()
            .withPortalAssetType(PortalAssetType.SUBAREA)
            .build())
        .withAppointmentStatus(appointmentStatus)
        .build();

    when(appointmentAccessService.findAppointmentDtoById(appointmentId)).thenReturn(Optional.of(appointmentDto));

    assertThat(forwardApprovedAppointmentRestService.isValidSubareaAppointmentId(appointmentId)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = AppointmentStatus.class, names = { "EXTANT", "TERMINATED" })
  void isValidSubareaAppointmentId_valid(AppointmentStatus appointmentStatus) {
    var appointmentId = new AppointmentId(UUID.randomUUID());
    var appointmentDto = AppointmentDtoTestUtil.builder()
        .withAssetDto(AssetDtoTestUtil.builder()
            .withPortalAssetType(PortalAssetType.SUBAREA)
            .build())
        .withAppointmentStatus(appointmentStatus)
        .build();

    when(appointmentAccessService.findAppointmentDtoById(appointmentId)).thenReturn(Optional.of(appointmentDto));

    assertThat(forwardApprovedAppointmentRestService.isValidSubareaAppointmentId(appointmentId)).isTrue();
  }
}
