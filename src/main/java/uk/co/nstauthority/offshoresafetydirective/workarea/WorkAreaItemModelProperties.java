package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkAreaItemModelProperties {

  private final HashMap<String, Object> properties = new LinkedHashMap<>();

  WorkAreaItemModelProperties addProperty(String key, Object value) {
    properties.put(key, value);
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

}
