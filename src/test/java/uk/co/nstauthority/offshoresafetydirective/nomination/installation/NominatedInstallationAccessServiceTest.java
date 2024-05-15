package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class NominatedInstallationAccessServiceTest {

  @Mock
  private NominatedInstallationRepository nominatedInstallationRepository;

  @InjectMocks
  private NominatedInstallationAccessService nominatedInstallationAccessService;

  @Test
  void getNominatedInstallations_whenNoInstallationsNominated_thenEmptyListReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    given(nominatedInstallationRepository.findAllByNominationDetail(nominationDetail))
        .willReturn(Collections.emptyList());

    var resultingNominatedInstallations = nominatedInstallationAccessService
        .getNominatedInstallations(nominationDetail);

    assertThat(resultingNominatedInstallations).isEmpty();
  }

  @Test
  void getNominatedInstallations_whenInstallationsNominated_thenPopulatedListReturned() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    var firstNominatedInstallation = NominatedInstallationTestUtil.builder()
        .withInstallationId(10)
        .build();

    var secondNominatedInstallation = NominatedInstallationTestUtil.builder()
        .withInstallationId(20)
        .build();

    given(nominatedInstallationRepository.findAllByNominationDetail(nominationDetail))
        .willReturn(List.of(firstNominatedInstallation, secondNominatedInstallation));

    var resultingNominatedInstallations = nominatedInstallationAccessService
        .getNominatedInstallations(nominationDetail);

    assertThat(resultingNominatedInstallations)
        .containsExactly(
            firstNominatedInstallation,
            secondNominatedInstallation
        );
  }

}