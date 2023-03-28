package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;

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

}