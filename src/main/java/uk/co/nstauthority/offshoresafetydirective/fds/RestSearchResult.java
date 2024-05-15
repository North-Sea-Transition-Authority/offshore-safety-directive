package uk.co.nstauthority.offshoresafetydirective.fds;


import java.util.List;

/**
 * A RestSearchResult is used as response for producing options within a search selector.
 */
public class RestSearchResult {
  List<RestSearchItem> results;

  // No-args constructor required for Jackson mapping in controller test
  private RestSearchResult() {
  }

  public RestSearchResult(List<RestSearchItem> results) {
    this.results = results;
  }

  public List<RestSearchItem> getResults() {
    return results;
  }
}
