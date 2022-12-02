package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SmokeTesterHelper;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class NominationStatusSecurityTestUtil {

  private NominationStatusSecurityTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static SmokeTester smokeTester(MockMvc mockMvc) {
    return new SmokeTester(mockMvc);
  }

  public static class SmokeTester extends SmokeTesterHelper<SmokeTester> {

    private final Set<NominationStatus> permittedNominationStatuses = new HashSet<>();

    private NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    private SmokeTester(MockMvc mockMvc) {
      super(mockMvc);
    }

    public SmokeTester withPermittedNominationStatus(NominationStatus nominationStatus) {
      permittedNominationStatuses.add(nominationStatus);
      return this;
    }

    public SmokeTester withNominationDetail(NominationDetail nominationDetail) {
      this.nominationDetail = nominationDetail;
      return this;
    }

    public void test() {

      getTestableEndpoints().forEach(testableEndpoint ->

          Arrays.stream(NominationStatus.values()).forEach(nominationStatus -> {

            nominationDetail.setStatus(nominationStatus);

            try {

              var response = performRequest(testableEndpoint);

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
