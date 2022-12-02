package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominationStatusSecurityTestUtil {

  private NominationStatusSecurityTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static SmokeTester smokeTester(MockMvc mockMvc) {
    return new SmokeTester(mockMvc);
  }

  private record TestableEndpoint(
      String url,
      HttpMethod requestMethod,
      ResultMatcher accessGrantedResultMatcher,
      ResultMatcher accessDeniedResultMatcher
  ) {}

  public static class SmokeTester {

    private final MockMvc mockMvc;

    private final Set<NominationStatus> permittedNominationStatuses = new HashSet<>();

    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    private ServiceUserDetail userToTestWith = ServiceUserDetailTestUtil.Builder().build();

    private final Set<TestableEndpoint> testableEndpoints = new HashSet<>();

    private SmokeTester(MockMvc mockMvc) {
      this.mockMvc = mockMvc;
    }

    public SmokeTester withPermittedNominationStatus(NominationStatus nominationStatus) {
      permittedNominationStatuses.add(nominationStatus);
      return this;
    }

    public SmokeTester withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public SmokeTester withGetEndpoint(String endpoint) {
      return withGetEndpoint(endpoint, status().isOk(), status().isForbidden());
    }

    public SmokeTester withGetEndpoint(String endpoint,
                                       ResultMatcher accessGrantedResultMatcher,
                                       ResultMatcher accessDeniedResultMatcher) {
      return withEndpoint(endpoint, HttpMethod.GET, accessGrantedResultMatcher, accessDeniedResultMatcher);
    }

    public SmokeTester withPostEndpoint(String endpoint,
                                        ResultMatcher accessGrantedResultMatcher,
                                        ResultMatcher accessDeniedResultMatcher) {
      return withEndpoint(endpoint, HttpMethod.POST, accessGrantedResultMatcher, accessDeniedResultMatcher);
    }

    private SmokeTester withEndpoint(String endpoint,
                                     HttpMethod requestMethod,
                                     ResultMatcher accessGrantedResultMatcher,
                                     ResultMatcher accessDeniedResultMatcher) {
      testableEndpoints.add(
          new TestableEndpoint(endpoint, requestMethod, accessGrantedResultMatcher, accessDeniedResultMatcher)
      );
      return this;
    }

    public SmokeTester withUser(ServiceUserDetail userToTestWith) {
      this.userToTestWith = userToTestWith;
      return this;
    }

    public void test() {

      testableEndpoints.forEach(testableEndpoint ->
          Arrays.stream(NominationStatus.values()).forEach(nominationStatus -> {

            nominationDetail.setStatus(nominationStatus);

            ResultActions response;
            try {

              MockHttpServletRequestBuilder requestToTest;

              if (testableEndpoint.requestMethod().equals(HttpMethod.GET)) {
                requestToTest = get(testableEndpoint.url());
              } else if (testableEndpoint.requestMethod().equals(HttpMethod.POST)) {
                requestToTest = post(testableEndpoint.url()).with(csrf());
              } else {
                throw new IllegalArgumentException(
                    "Unsupported HttpMethod %s".formatted(testableEndpoint.requestMethod().name())
                );
              }

              response = mockMvc.perform(requestToTest.with(user(userToTestWith)));

              if (permittedNominationStatuses.contains(nominationStatus)) {
                response.andExpect(testableEndpoint.accessGrantedResultMatcher());
              } else {
                response.andExpect(testableEndpoint.accessDeniedResultMatcher());
              }
            } catch (AssertionError | Exception exception) {
              throw new AssertionError(
                  "Assertion failed on nomination status %s for endpoint %s"
                      .formatted(nominationStatus, testableEndpoint.url()),
                  exception
              );
            }
          }));
    }
  }
}
