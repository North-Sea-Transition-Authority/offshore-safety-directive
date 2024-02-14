package uk.co.nstauthority.offshoresafetydirective.nomination.operatorinvolvement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;

@ExtendWith(MockitoExtension.class)
class NominationOperatorServiceTest {

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

  @Mock
  private ApplicantDetailAccessService applicantDetailAccessService;

  @Mock
  private NomineeDetailAccessService nomineeDetailAccessService;

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @InjectMocks
  private NominationOperatorService nominationOperatorService;

  @DisplayName("GIVEN I want to know which operators are involved in a nomination")
  @Nested
  class GetNominationOperators {

    @DisplayName("WHEN no applicant information found")
    @Nested
    class WhenNoApplicantDetailFound {

      @DisplayName("THEN an exception is thrown")
      @Test
      void thenExceptionThrown() {

        given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> nominationOperatorService.getNominationOperators(NOMINATION_DETAIL))
            .isInstanceOf(IllegalStateException.class);

        then(nomineeDetailAccessService).shouldHaveNoInteractions();

        then(organisationUnitQueryService).shouldHaveNoInteractions();
      }
    }

    @DisplayName("WHEN no nominee information found")
    @Nested
    class WhenNoNomineeDetailFound {

      @DisplayName("THEN an exception is thrown")
      @Test
      void thenExceptionThrown() {

        var applicant = ApplicantDetailDto.fromApplicantDetail(ApplicantDetailTestUtil.builder().build());

        given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
            .willReturn(Optional.of(applicant));

        given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(NOMINATION_DETAIL))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> nominationOperatorService.getNominationOperators(NOMINATION_DETAIL))
            .isInstanceOf(IllegalStateException.class);

        then(organisationUnitQueryService).shouldHaveNoInteractions();
      }
    }

    @DisplayName("WHEN applicant and nominee provided")
    @Nested
    class WhenApplicantAndNomineeProvided {

      @DisplayName("THEN applicant and nominee organisations populated")
      @Test
      void thenApplicantAndNomineePopulated() {

        var applicantDetail = ApplicantDetailTestUtil.builder()
            .withPortalOrganisationId(100)
            .build();

        var applicantDto = ApplicantDetailDto.fromApplicantDetail(applicantDetail);

        var applicantPortalOrganisation = PortalOrganisationDtoTestUtil.builder()
            .withId(applicantDto.applicantOrganisationId().id())
            .build();

        given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
            .willReturn(Optional.of(applicantDto));

        var nomineeDetail = NomineeDetailTestingUtil.builder()
            .withNominatedOrganisationId(200)
            .build();

        var nomineeDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

        var nomineePortalOrganisation = PortalOrganisationDtoTestUtil.builder()
            .withId(nomineeDto.nominatedOrganisationId().id())
            .build();

        given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(NOMINATION_DETAIL))
            .willReturn(Optional.of(nomineeDto));

        given(organisationUnitQueryService.getOrganisationByIds(
            refEq(
                Set.of(
                  new PortalOrganisationUnitId(nomineeDto.nominatedOrganisationId().id()),
                  new PortalOrganisationUnitId(applicantDto.applicantOrganisationId().id())
                )
            ),
            any(RequestPurpose.class)
        ))
            .willReturn(List.of(applicantPortalOrganisation, nomineePortalOrganisation));

        var resultingNominationOperators = nominationOperatorService.getNominationOperators(NOMINATION_DETAIL);

        assertThat(resultingNominationOperators)
            .extracting(NominationOperators::applicant, NominationOperators::nominee)
            .containsExactly(applicantPortalOrganisation, nomineePortalOrganisation);
      }
    }
  }
}
