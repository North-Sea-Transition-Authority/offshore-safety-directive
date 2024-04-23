package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = {NominationReferenceRestController.class})
class NominationReferenceRestControllerTest extends AbstractNominationControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @SecurityTest
  void searchPostSubmissionNominations_whenUnauthenticated_thenRedirectedToLogin() throws Exception {
    var searchTerm = "search";
    mockMvc.perform(get(ReverseRouter.route(on(NominationReferenceRestController.class)
        .searchPostSubmissionNominations(searchTerm))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void searchPostSubmissionNominations_whenIncorrectRole() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    var searchTerm = "search";
    mockMvc.perform(get(ReverseRouter.route(on(NominationReferenceRestController.class)
        .searchPostSubmissionNominations(searchTerm)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void searchPostSubmissionNominations() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);

    var searchTerm = "search";

    var firstNominationId = new NominationId(UUID.randomUUID());
    var firstNominationReference = "NOM/REF/1";
    var firstNominationDto = NominationDtoTestUtil.builder()
        .withNominationId(firstNominationId)
        .withNominationReference(firstNominationReference)
        .build();

    var secondNominationId = new NominationId(UUID.randomUUID());
    var secondNominationReference = "NOM/REF/2";
    var secondNominationDto = NominationDtoTestUtil.builder()
        .withNominationId(secondNominationId)
        .withNominationReference(secondNominationReference)
        .build();

    when(nominationDetailService.getNominationsByReferenceLikeWithStatuses(
        searchTerm,
        EnumSet.of(NominationStatus.APPOINTED)
    )).thenReturn(Set.of(secondNominationDto, firstNominationDto));

    var result = mockMvc.perform(get(ReverseRouter.route(on(NominationReferenceRestController.class)
        .searchPostSubmissionNominations(searchTerm)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    var mappedResult = OBJECT_MAPPER.readValue(result, RestSearchResult.class);

    assertThat(mappedResult.getResults())
        .extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(
            tuple(firstNominationId.toString(), firstNominationReference),
            tuple(secondNominationId.toString(), secondNominationReference)
        );
  }
}