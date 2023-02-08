package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.EpaWellboreTestUtil;

class LicenceBlockSubareaWellboreDtoTest {

  @Test
  void fromPortalSubarea_whenNoWellbores_thenEmptyList() {

    var portalSubarea = EpaSubareaTestUtil.builder()
        .withWellbores(Collections.emptyList())
        .build();

    var resultingSubareaDto = LicenceBlockSubareaWellboreDto.fromPortalSubarea(portalSubarea);

    assertThat(resultingSubareaDto)
        .extracting(
            subarea -> subarea.subareaId().id(),
            LicenceBlockSubareaWellboreDto::wellbores
        )
        .containsExactly(
            portalSubarea.getId(),
            Collections.emptyList()
        );
  }

  @Test
  void fromPortalSubarea_whenWellbores_thenPopulatedList() {

    var portalWellbore = EpaWellboreTestUtil.builder().build();

    var portalSubarea = EpaSubareaTestUtil.builder()
        .withWellbore(portalWellbore)
        .build();

    var resultingSubareaDto = LicenceBlockSubareaWellboreDto.fromPortalSubarea(portalSubarea);

    assertThat(resultingSubareaDto)
        .extracting(
            subarea -> subarea.subareaId().id(),
            subarea -> subarea.wellbores()
                .stream()
                .map(wellDto -> wellDto.wellboreId().id())
                .toList()
        )
        .containsExactly(
            portalSubarea.getId(),
            List.of(portalWellbore.getId())
        );
  }

}