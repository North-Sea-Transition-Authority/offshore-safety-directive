package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

@ExtendWith(MockitoExtension.class)
class NominationWorkAreaItemTransformerServiceTest {

  private static final WorkAreaFilter FILTER = new WorkAreaFilter(EnumSet.allOf(NominationStatus.class));

  @Mock
  private NominationWorkAreaQueryService nominationWorkAreaQueryService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private NominationWorkAreaItemTransformerService nominationWorkAreaItemTransformerService;

  @Test
  void getWorkAreaDtos() {

    var now = Instant.now();

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withCreatedTime(now.minus(Period.ofDays(2)))
        .withSubmittedTime(now)
        .withPlannedAppointmentDate(LocalDate.now().minusDays(1))
        .withFirstSubmittedOn(now)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    var applicantOrganisationId = queryResult.getApplicantOrganisationId().id();
    var nominatedOrganisationId = queryResult.getNominatedOrganisationId().id();

    var ids = Stream.of(applicantOrganisationId, nominatedOrganisationId)
        .map(PortalOrganisationUnitId::new)
        .toList();

    var portalApplicationOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantOrganisationId)
        .build();

    var portalNominatedOrganisationDto = PortalOrganisationDtoTestUtil.builder()
        .withId(nominatedOrganisationId)
        .build();

    when(portalOrganisationUnitQueryService.getOrganisationByIds(ids, NominationWorkAreaItemTransformerService.NOMINATED_OPERATORS_PURPOSE))
        .thenReturn(List.of(portalApplicationOrganisationDto, portalNominatedOrganisationDto));

    var result = nominationWorkAreaItemTransformerService.getNominationWorkAreaItemDtos(FILTER);

    assertThat(result).extracting(
        NominationWorkAreaItemDto::nominationId,
        dto -> dto.applicantOrganisationUnitDto().id(),
        NominationWorkAreaItemDto::nominationReference,
        NominationWorkAreaItemDto::applicantReference,
        dto -> dto.nominatedOrganisationUnitDto().id(),
        NominationWorkAreaItemDto::nominationDisplay,
        NominationWorkAreaItemDto::nominationStatus,
        NominationWorkAreaItemDto::createdTime,
        NominationWorkAreaItemDto::submittedTime,
        NominationWorkAreaItemDto::nominationVersion,
        NominationWorkAreaItemDto::nominationHasUpdateRequest,
        NominationWorkAreaItemDto::plannedAppointmentDate,
        NominationWorkAreaItemDto::nominationFirstSubmittedOn
    ).containsExactly(
        tuple(
            queryResult.getNominationId(),
            queryResult.getApplicantOrganisationId().id(),
            queryResult.getNominationReference(),
            queryResult.getApplicantReference(),
            queryResult.getNominatedOrganisationId().id(),
            queryResult.getNominationDisplayType(),
            queryResult.getNominationStatus(),
            queryResult.getCreatedTime(),
            queryResult.getSubmittedTime(),
            queryResult.getNominationVersion(),
            queryResult.getNominationHasUpdateRequest(),
            queryResult.getPlannedAppointmentDate(),
            queryResult.getNominationFirstSubmittedOn()
        )
    );
  }

  @Test
  void getWorkAreaDtos_whenPortalOrgsNotFound_thenNull() {

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withApplicantOrganisationId(100)
        .withNominatedOrganisationId(200)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    var applicantOrganisationId = queryResult.getApplicantOrganisationId().id();
    var nominatedOrganisationId = queryResult.getNominatedOrganisationId().id();

    var ids = Stream.of(applicantOrganisationId, nominatedOrganisationId)
        .map(PortalOrganisationUnitId::new)
        .toList();

    when(portalOrganisationUnitQueryService.getOrganisationByIds(ids, NominationWorkAreaItemTransformerService.NOMINATED_OPERATORS_PURPOSE))
        .thenReturn(List.of());

    var result = nominationWorkAreaItemTransformerService.getNominationWorkAreaItemDtos(FILTER);

    assertThat(result)
        .extracting(
            NominationWorkAreaItemDto::applicantOrganisationUnitDto,
            NominationWorkAreaItemDto::nominatedOrganisationUnitDto
        )
        .containsExactly(
            tuple(null, null)
        );
  }

  @Test
  void getWorkAreaDtos_whenStatusNotInFilter_thenExcluded() {
    var draft = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.DRAFT)
        .build();
    var submitted = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .build();
    var appointed = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.APPOINTED)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems())
        .thenReturn(List.of(draft, submitted, appointed));

    when(portalOrganisationUnitQueryService.getOrganisationByIds(anyList(),
        eq(NominationWorkAreaItemTransformerService.NOMINATED_OPERATORS_PURPOSE)))
        .thenReturn(List.of());

    var filter = new WorkAreaFilter(EnumSet.of(NominationStatus.DRAFT, NominationStatus.SUBMITTED));

    var result = nominationWorkAreaItemTransformerService.getNominationWorkAreaItemDtos(filter);

    assertThat(result)
        .extracting(NominationWorkAreaItemDto::nominationId)
        .containsExactlyInAnyOrder(draft.getNominationId(), submitted.getNominationId());
  }

  @Test
  void getWorkAreaDtos_whenFilterEmpty_thenNoResults() {
    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withNominationStatus(NominationStatus.SUBMITTED)
        .build();
    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));
    when(portalOrganisationUnitQueryService.getOrganisationByIds(anyList(),
        eq(NominationWorkAreaItemTransformerService.NOMINATED_OPERATORS_PURPOSE)))
        .thenReturn(List.of());

    var result = nominationWorkAreaItemTransformerService
        .getNominationWorkAreaItemDtos(new WorkAreaFilter(EnumSet.noneOf(NominationStatus.class)));

    assertThat(result).isEmpty();
  }
}