package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;

class LicenceBlockSubareaDtoTest {

  @Test
  void displayName_verifyFormat() {

    var licenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder().build();

    assertThat(licenceBlockSubareaDto.displayName()).isEqualTo(
        "%s %s %s".formatted(
            licenceBlockSubareaDto.licence().licenceReference().value(),
            licenceBlockSubareaDto.licenceBlock().reference().value(),
            licenceBlockSubareaDto.subareaName().value()
        )
    );
  }

  @Test
  void displayName_whenAllPropertiesNull() {

    var licenceBlockSubareaDto = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceReference(null)
        .withBlockReference(null)
        .withSubareaName(null)
        .build();

    assertThat(licenceBlockSubareaDto.displayName()).isNull();
  }

  @Test
  void fromPortalSubarea_verifyMappings() {

    var portalSubarea = EpaSubareaTestUtil.builder().build();

    var resultingLicenceBlockSubarea = LicenceBlockSubareaDto.fromPortalSubarea(portalSubarea);

    assertThat(resultingLicenceBlockSubarea)
        .extracting(
            subareaDto -> subareaDto.subareaId().id(),
            subareaDto -> subareaDto.subareaName().value(),
            subareaDto -> subareaDto.licenceBlock().quadrantNumber().value(),
            subareaDto -> subareaDto.licenceBlock().blockNumber().value(),
            subareaDto -> subareaDto.licenceBlock().blockSuffix().value(),
            subareaDto -> subareaDto.licenceBlock().reference().value(),
            subareaDto -> subareaDto.licence().licenceType().value(),
            subareaDto -> subareaDto.licence().licenceNumber().value(),
            subareaDto -> subareaDto.licence().licenceReference().value()
        )
        .containsExactly(
            portalSubarea.getId(),
            portalSubarea.getName(),
            portalSubarea.getLicenceBlock().getQuadrantNumber(),
            portalSubarea.getLicenceBlock().getBlockNumber(),
            portalSubarea.getLicenceBlock().getSuffix(),
            portalSubarea.getLicenceBlock().getReference(),
            portalSubarea.getLicence().getLicenceType(),
            portalSubarea.getLicence().getLicenceNo(),
            portalSubarea.getLicence().getLicenceRef()
        );
  }

  @Test
  void fromPortalSubarea_verifyExtantMapping() {

    var portalSubarea = EpaSubareaTestUtil.builder()
        .withStatus(SubareaStatus.EXTANT)
        .build();

    var resultingLicenceBlockSubarea = LicenceBlockSubareaDto.fromPortalSubarea(portalSubarea);

    assertThat(resultingLicenceBlockSubarea)
        .extracting(LicenceBlockSubareaDto::isExtant)
        .isEqualTo(true);
  }

  @Test
  void fromPortalSubarea_verifyNonExtantMapping() {

    var portalSubarea = EpaSubareaTestUtil.builder()
        .withStatus(SubareaStatus.NOT_EXTANT)
        .build();

    var resultingLicenceBlockSubarea = LicenceBlockSubareaDto.fromPortalSubarea(portalSubarea);

    assertThat(resultingLicenceBlockSubarea)
        .extracting(LicenceBlockSubareaDto::isExtant)
        .isEqualTo(false);
  }

  @Test
  void notOnPortal_verifyMapping() {
    var subareaId = new LicenceBlockSubareaId(UUID.randomUUID().toString());
    var subareaName = new SubareaName("subarea name");
    var result = LicenceBlockSubareaDto.notOnPortal(subareaId, subareaName);
    assertThat(result)
        .extracting(
            SubareaDto::subareaId,
            LicenceBlockSubareaDto::subareaName,
            LicenceBlockSubareaDto::licenceBlock,
            LicenceBlockSubareaDto::licence,
            LicenceBlockSubareaDto::isExtant
        )
        .containsExactly(
            subareaId,
            subareaName,
            null,
            null,
            false
        );
  }

}