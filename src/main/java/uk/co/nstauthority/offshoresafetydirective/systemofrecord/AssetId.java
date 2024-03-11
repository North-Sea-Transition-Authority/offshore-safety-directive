package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record AssetId(UUID id) {

  // Required so AssetId can be used as an @PathVariable in controllers.
  public static AssetId valueOf(String value) {
    try {
      return new AssetId(UUID.fromString(value));
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          String.format("Cannot find Asset with ID: %s", value)
      );
    }
  }

  public static AssetId fromAsset(Asset asset) {
    return new AssetId(asset.getId());
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
