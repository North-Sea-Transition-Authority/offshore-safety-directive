package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action;

import java.util.HashMap;
import java.util.Map;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;

public class NominationManagementInteractable {

  private final NominationManagementItem item;
  private final NominationManagementGroup group;
  private final CaseProcessingAction caseProcessingAction;
  private final String submitUrl;
  private final Map<String, Object> modelProperties;

  private NominationManagementInteractable(NominationManagementItem item, NominationManagementGroup group,
                                           CaseProcessingAction caseProcessingAction, String submitUrl,
                                           Map<String, Object> modelProperties) {
    this.item = item;
    this.group = group;
    this.caseProcessingAction = caseProcessingAction;
    this.submitUrl = submitUrl;
    this.modelProperties = modelProperties;
  }

  public NominationManagementItem getItem() {
    return item;
  }

  public NominationManagementGroup getGroup() {
    return group;
  }

  public String getSubmitUrl() {
    return submitUrl;
  }

  public CaseProcessingAction getCaseProcessingAction() {
    return caseProcessingAction;
  }

  public Map<String, Object> getModelProperties() {
    return modelProperties;
  }

  public static Builder builder(NominationManagementItem key, NominationManagementGroup group,
                                CaseProcessingAction caseProcessingAction, String submitUrl) {
    return new Builder(key, group, caseProcessingAction, submitUrl);
  }

  public static class Builder {

    private final NominationManagementItem key;
    private final NominationManagementGroup group;
    private final CaseProcessingAction caseProcessingAction;
    private final String submitUrl;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public Builder withAdditionalProperty(String key, Object value) {
      this.additionalProperties.put(key, value);
      return this;
    }

    private Builder(NominationManagementItem key, NominationManagementGroup group,
                    CaseProcessingAction caseProcessingAction, String submitUrl) {
      this.key = key;
      this.group = group;
      this.caseProcessingAction = caseProcessingAction;
      this.submitUrl = submitUrl;
    }

    public NominationManagementInteractable build() {
      return new NominationManagementInteractable(
          key,
          group,
          caseProcessingAction,
          submitUrl,
          additionalProperties
      );
    }

  }

}
