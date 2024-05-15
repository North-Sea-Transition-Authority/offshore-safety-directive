package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections.wellbore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@ExtendWith(MockitoExtension.class)
class WellboreAppointmentRestServiceTest {

  @Mock
  private WellQueryService wellQueryService;

  @Mock
  private AppointmentAccessService appointmentAccessService;

  @InjectMocks
  private WellboreAppointmentRestService wellboreAppointmentRestService;

  @Test
  void searchWellboreAppointments_whenHasResults_thenVerifyList() {
    var searchTerm = "search term";

    Integer wellId = 123;
    var wellDto = WellDtoTestUtil.builder()
        .withWellboreId(wellId)
        .build();
    when(wellQueryService.searchWellsByRegistrationNumber(searchTerm, WellboreAppointmentRestService.WELL_SEARCH_PURPOSE))
        .thenReturn(List.of(wellDto));

    var asset = AssetTestUtil.builder()
        .withPortalAssetId(wellId.toString())
        .build();
    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();
    when(appointmentAccessService.getAppointmentsForAssets(
        EnumSet.of(AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED),
        List.of(wellId.toString()),
        PortalAssetType.WELLBORE
    ))
        .thenReturn(List.of(appointment));

    var result = wellboreAppointmentRestService.searchWellboreAppointments(searchTerm);

    assertThat(result)
        .extracting(
            RestSearchItem::id,
            RestSearchItem::text
        )
        .containsExactly(
            Tuple.tuple(
                appointment.getId().toString(),
                WellboreAppointmentRestService.formatSearchItemName(wellDto.name(), appointment.getResponsibleFromDate())
            )
        );
  }

  @Test
  void searchWellboreAppointments_whenHasMultipleWells_andHasMultipleAppointments_thenVerifyOrder() {
    var searchTerm = "search term";

    Integer firstWellId = 123;
    var firstWellDto = WellDtoTestUtil.builder()
        .withWellboreId(firstWellId)
        .build();
    Integer secondWellId = 456;
    var secondWellDto = WellDtoTestUtil.builder()
        .withWellboreId(secondWellId)
        .build();
    when(wellQueryService.searchWellsByRegistrationNumber(searchTerm, WellboreAppointmentRestService.WELL_SEARCH_PURPOSE))
        .thenReturn(List.of(firstWellDto, secondWellDto));

    var assetForFirstWellAppointment = AssetTestUtil.builder()
        .withPortalAssetId(firstWellId.toString())
        .build();
    var appointmentForFirstWell = AppointmentTestUtil.builder()
        .withAsset(assetForFirstWellAppointment)
        .build();

    var firstAssetForSecondWellAppointment = AssetTestUtil.builder()
        .withPortalAssetId(secondWellId.toString())
        .build();
    var firstAppointmentForSecondWellDate = LocalDate.now().minusDays(1);
    var firstAppointmentForSecondWell = AppointmentTestUtil.builder()
        .withAsset(firstAssetForSecondWellAppointment)
        .withResponsibleFromDate(firstAppointmentForSecondWellDate)
        .build();
    var secondAssetForSecondWellAppointment = AssetTestUtil.builder()
        .withPortalAssetId(secondWellId.toString())
        .build();
    var secondAppointmentForSecondWellDate = LocalDate.now();
    var secondAppointmentForSecondWell = AppointmentTestUtil.builder()
        .withAsset(secondAssetForSecondWellAppointment)
        .withResponsibleFromDate(secondAppointmentForSecondWellDate)
        .build();

    when(appointmentAccessService.getAppointmentsForAssets(
        EnumSet.of(AppointmentStatus.EXTANT, AppointmentStatus.TERMINATED),
        List.of(firstWellId.toString(), secondWellId.toString()),
        PortalAssetType.WELLBORE
    ))
        .thenReturn(List.of(firstAppointmentForSecondWell, secondAppointmentForSecondWell, appointmentForFirstWell));

    var result = wellboreAppointmentRestService.searchWellboreAppointments(searchTerm);

    assertThat(result)
        .extracting(
            RestSearchItem::id
        )
        .containsExactly(
            appointmentForFirstWell.getId().toString(),
            secondAppointmentForSecondWell.getId().toString(),
            firstAppointmentForSecondWell.getId().toString()
        );
  }

  @Test
  void formatSearchItemName() {
    var wellName = "well name";
    var responsibleDate = LocalDate.of(2024, 1, 1);

    assertThat(WellboreAppointmentRestService.formatSearchItemName(wellName, responsibleDate))
        .isEqualTo("%s: %s".formatted(
            wellName,
            "1 January 2024"
        ));
  }
}