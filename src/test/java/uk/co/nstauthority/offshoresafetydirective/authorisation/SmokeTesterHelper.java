package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;

public abstract class SmokeTesterHelper<T> {

  private final MockMvc mockMvc;

  private final Set<TestableEndpoint> testableEndpoints = new HashSet<>();

  protected ServiceUserDetail userToTestWith = ServiceUserDetailTestUtil.Builder().build();

  private final MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();

  private MockMultipartFile mockMultipartFile;

  public SmokeTesterHelper(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @SuppressWarnings("unchecked")
  public T withUser(ServiceUserDetail user) {
    this.userToTestWith = user;
    return (T) this;
  }

  public T withGetEndpoint(String endpoint) {
    return withGetEndpoint(endpoint, status().isOk(), status().isForbidden());
  }

  public T withGetEndpoint(String endpoint,
                           ResultMatcher accessGrantedResultMatcher,
                           ResultMatcher accessDeniedResultMatcher) {
    return withEndpoint(endpoint, SmokeTestHttpMethod.GET, accessGrantedResultMatcher, accessDeniedResultMatcher);
  }

  public T withPostEndpoint(String endpoint,
                            ResultMatcher accessGrantedResultMatcher,
                            ResultMatcher accessDeniedResultMatcher) {
    return withEndpoint(endpoint, SmokeTestHttpMethod.POST, accessGrantedResultMatcher, accessDeniedResultMatcher);
  }

  public T withMultipartFilePostEndpoint(String endpoint,
                                         MockMultipartFile mockMultipartFile,
                                         ResultMatcher accessGrantedResultMatcher,
                                         ResultMatcher accessDeniedResultMatcher) {
    this.mockMultipartFile = mockMultipartFile;
    return withEndpoint(endpoint, SmokeTestHttpMethod.POST_FILE, accessGrantedResultMatcher, accessDeniedResultMatcher);
  }

  @SuppressWarnings("unchecked")
  private T withEndpoint(String endpoint,
                         SmokeTestHttpMethod requestMethod,
                         ResultMatcher accessGrantedResultMatcher,
                         ResultMatcher accessDeniedResultMatcher) {
    testableEndpoints.add(
        new TestableEndpoint(endpoint, requestMethod, accessGrantedResultMatcher, accessDeniedResultMatcher)
    );
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T withBodyParam(String key, String value) {
    bodyParams.put(key, Collections.singletonList(value));
    return (T) this;
  }

  public record TestableEndpoint(
      String url,
      SmokeTestHttpMethod requestMethod,
      ResultMatcher accessGrantedResultMatcher,
      ResultMatcher accessDeniedResultMatcher
  ) {
  }

  public Set<TestableEndpoint> getTestableEndpoints() {
    return testableEndpoints;
  }

  public ResultActions performRequest(TestableEndpoint testableEndpoint) throws Exception {

    var requestToTest = constructRequestToTest(testableEndpoint);

    if (testableEndpoint.requestMethod().equals(SmokeTestHttpMethod.POST_FILE)) {
      var multipartRequestToTest = (MockMultipartHttpServletRequestBuilder) requestToTest;
      multipartRequestToTest.file(mockMultipartFile);
    }

    return mockMvc.perform(
        requestToTest
            .with(user(userToTestWith))
            .params(bodyParams)
    );
  }

  private MockHttpServletRequestBuilder constructRequestToTest(TestableEndpoint testableEndpoint) {

    MockHttpServletRequestBuilder requestToTest;

    if (testableEndpoint.requestMethod().equals(SmokeTestHttpMethod.GET)) {
      requestToTest = get(testableEndpoint.url());
    } else if (testableEndpoint.requestMethod().equals(SmokeTestHttpMethod.POST)) {
      requestToTest = post(testableEndpoint.url()).with(csrf());
    } else if (testableEndpoint.requestMethod().equals(SmokeTestHttpMethod.POST_FILE)) {
      requestToTest = multipart(testableEndpoint.url()).with(csrf());
    } else {
      throw new IllegalArgumentException(
          "Unsupported HttpMethod %s".formatted(testableEndpoint.requestMethod().name())
      );
    }

    return requestToTest;
  }
}
