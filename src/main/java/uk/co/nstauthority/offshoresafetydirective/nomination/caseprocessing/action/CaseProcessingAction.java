package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import java.util.HashMap;
import java.util.Map;

public class CaseProcessingAction {

  private final CaseProcessingActionItem item;
  private final CaseProcessingActionGroup group;
  private final CaseProcessingActionIdentifier caseProcessingActionIdentifier;
  private final String submitUrl;
  private final Map<String, Object> modelProperties;

  private CaseProcessingAction(CaseProcessingActionItem item, CaseProcessingActionGroup group,
                               CaseProcessingActionIdentifier caseProcessingActionIdentifier, String submitUrl,
                               Map<String, Object> modelProperties) {
    this.item = item;
    this.group = group;
    this.caseProcessingActionIdentifier = caseProcessingActionIdentifier;
    this.submitUrl = submitUrl;
    this.modelProperties = modelProperties;
  }

  public CaseProcessingActionItem getItem() {
    return item;
  }

  public CaseProcessingActionGroup getGroup() {
    return group;
  }

  public String getSubmitUrl() {
    return submitUrl;
  }

  public CaseProcessingActionIdentifier getCaseProcessingAction() {
    return caseProcessingActionIdentifier;
  }

  public Map<String, Object> getModelProperties() {
    return modelProperties;
  }

  public static Builder builder(CaseProcessingActionItem key, CaseProcessingActionGroup group,
                                CaseProcessingActionIdentifier caseProcessingActionIdentifier, String submitUrl) {
    return new Builder(key, group, caseProcessingActionIdentifier, submitUrl);
  }

  public static class Builder {

    private final CaseProcessingActionItem key;
    private final CaseProcessingActionGroup group;
    private final CaseProcessingActionIdentifier caseProcessingActionIdentifier;
    private final String submitUrl;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public Builder withAdditionalProperty(String key, Object value) {
      this.additionalProperties.put(key, value);
      return this;
    }

    private Builder(CaseProcessingActionItem key, CaseProcessingActionGroup group,
                    CaseProcessingActionIdentifier caseProcessingActionIdentifier, String submitUrl) {
      this.key = key;
      this.group = group;
      this.caseProcessingActionIdentifier = caseProcessingActionIdentifier;
      this.submitUrl = submitUrl;
    }

    public CaseProcessingAction build() {
      return new CaseProcessingAction(
          key,
          group,
          caseProcessingActionIdentifier,
          submitUrl,
          additionalProperties
      );
    }

  }

}
