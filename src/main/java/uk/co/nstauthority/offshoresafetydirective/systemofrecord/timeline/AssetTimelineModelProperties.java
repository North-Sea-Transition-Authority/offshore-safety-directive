package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.HashMap;
import java.util.Map;

public class AssetTimelineModelProperties {

  private final Map<String, Object> modelProperties = new HashMap<>();

  public AssetTimelineModelProperties addProperty(String key, Object value) {
    modelProperties.put(key, value);
    return this;
  }

  public Map<String, Object> getModelProperties() {
    return modelProperties;
  }

}
