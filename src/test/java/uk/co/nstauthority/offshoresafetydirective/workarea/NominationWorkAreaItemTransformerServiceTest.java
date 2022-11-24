package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;

@ExtendWith(MockitoExtension.class)
class NominationWorkAreaItemTransformerServiceTest {

  @Mock
  private NominationWorkAreaQueryService nominationWorkAreaQueryService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private NominationWorkAreaItemTransformerService nominationWorkAreaItemTransformerService;

  @Test
  void getWorkAreaDtos_whenPortalOrgsFound_thenAllFieldsMapped() {

    var baseInstant = Instant.now();

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withCreatedTime(baseInstant.minus(Period.ofDays(2)))
        .withSubmittedTime(baseInstant)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    when(portalOrganisationUnitQueryService.getOrganisationById(queryResult.getApplicantOrganisationId().id()))
        .thenReturn(Optional.of(
            new PortalOrganisationDto(queryResult.getApplicantOrganisationId().id().toString(), "app org")));

    when(portalOrganisationUnitQueryService.getOrganisationById(queryResult.getNominatedOrganisationId().id()))
        .thenReturn(Optional.of(
            new PortalOrganisationDto(queryResult.getNominatedOrganisationId().id().toString(), "nominated org")));

    var result = nominationWorkAreaItemTransformerService.getNominationWorkAreaItemDtos();

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
        NominationWorkAreaItemDto::nominationVersion
    ).containsExactly(
        Tuple.tuple(
            queryResult.getNominationId(),
            queryResult.getApplicantOrganisationId().id().toString(),
            queryResult.getNominationReference(),
            queryResult.getApplicantReference(),
            queryResult.getNominatedOrganisationId().id().toString(),
            queryResult.getNominationDisplayType(),
            queryResult.getNominationStatus(),
            queryResult.getCreatedTime(),
            queryResult.getSubmittedTime(),
            queryResult.getNominationVersion()
        )
    );
  }

  @Test
  void getWorkAreaDtos_whenPortalOrgsNotFound_thenNull() {

    var baseInstant = Instant.now();

    var queryResult = NominationWorkAreaQueryResultTestUtil.builder()
        .withCreatedTime(baseInstant.minus(Period.ofDays(2)))
        .withSubmittedTime(baseInstant)
        .build();

    when(nominationWorkAreaQueryService.getWorkAreaItems()).thenReturn(List.of(queryResult));

    when(portalOrganisationUnitQueryService.getOrganisationById(queryResult.getApplicantOrganisationId().id()))
        .thenReturn(Optional.empty());

    when(portalOrganisationUnitQueryService.getOrganisationById(queryResult.getNominatedOrganisationId().id()))
        .thenReturn(Optional.empty());

    var result = nominationWorkAreaItemTransformerService.getNominationWorkAreaItemDtos();

    assertThat(result).extracting(
        NominationWorkAreaItemDto::nominationId,
        NominationWorkAreaItemDto::applicantOrganisationUnitDto,
        NominationWorkAreaItemDto::nominationReference,
        NominationWorkAreaItemDto::applicantReference,
        NominationWorkAreaItemDto::nominatedOrganisationUnitDto,
        NominationWorkAreaItemDto::nominationDisplay,
        NominationWorkAreaItemDto::nominationStatus,
        NominationWorkAreaItemDto::createdTime,
        NominationWorkAreaItemDto::submittedTime,
        NominationWorkAreaItemDto::nominationVersion
    ).containsExactly(
        Tuple.tuple(
            queryResult.getNominationId(),
            null,
            queryResult.getNominationReference(),
            queryResult.getApplicantReference(),
            null,
            queryResult.getNominationDisplayType(),
            queryResult.getNominationStatus(),
            queryResult.getCreatedTime(),
            queryResult.getSubmittedTime(),
            queryResult.getNominationVersion()
        )
    );
  }
}