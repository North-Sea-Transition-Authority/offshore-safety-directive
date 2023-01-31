package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LicenceBlockSubareaComparatorTest {

  @Test
  void compare_whenSameBlockAndSubareaName_thenSortedByLicenceComponents() {

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(1)
        .build();

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(2)
        .build();

    var thirdSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .build();

    var fourthSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(1)
        .build();

    var unsortedSubareaList = List.of(
        fourthSubareaByLicence,
        thirdSubareaByLicence,
        secondSubareaByLicence,
        firstSubareaByLicence
    );

    var sortedSubareaList =  unsortedSubareaList
        .stream()
        .sorted(LicenceBlockSubareaDto.sort())
        .toList();

    assertThat(sortedSubareaList)
        .extracting(
            subareaDto -> subareaDto.licence().licenceType(),
            subareaDto -> subareaDto.licence().licenceNumber()
        )
        .containsExactly(
            tuple(
                firstSubareaByLicence.licence().licenceType(),
                firstSubareaByLicence.licence().licenceNumber()
            ),
            tuple(
                secondSubareaByLicence.licence().licenceType(),
                secondSubareaByLicence.licence().licenceNumber()
            ),
            tuple(
                thirdSubareaByLicence.licence().licenceType(),
                thirdSubareaByLicence.licence().licenceNumber()
            ),
            tuple(
                fourthSubareaByLicence.licence().licenceType(),
                fourthSubareaByLicence.licence().licenceNumber()
            )
        );
  }

  @Test
  void compare_whenSameLicenceAndSubareaName_thenSortedByLicenceBlockComponents() {

    var firstSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix(null)
        .build();

    var secondSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("a")
        .build();

    var thirdSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("B")
        .build();

    var fourthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(2)
        .build();

    var fifthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("10")
        .build();

    var sixthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("2")
        .build();

    var unsortedSubareaList = List.of(
        sixthSubareaByBlock,
        firstSubareaByBlock,
        thirdSubareaByBlock,
        secondSubareaByBlock,
        fifthSubareaByBlock,
        fourthSubareaByBlock
    );

    var sortedSubareaList =  unsortedSubareaList
        .stream()
        .sorted(LicenceBlockSubareaDto.sort())
        .toList();

    assertThat(sortedSubareaList)
        .extracting(
            subareaDto -> subareaDto.licenceBlock().quadrantNumber(),
            subareaDto -> subareaDto.licenceBlock().blockNumber(),
            subareaDto -> subareaDto.licenceBlock().blockSuffix()
        )
        .containsExactly(
            tuple(
                firstSubareaByBlock.licenceBlock().quadrantNumber(),
                firstSubareaByBlock.licenceBlock().blockNumber(),
                firstSubareaByBlock.licenceBlock().blockSuffix()
            ),
            tuple(
                secondSubareaByBlock.licenceBlock().quadrantNumber(),
                secondSubareaByBlock.licenceBlock().blockNumber(),
                secondSubareaByBlock.licenceBlock().blockSuffix()
            ),
            tuple(
                thirdSubareaByBlock.licenceBlock().quadrantNumber(),
                thirdSubareaByBlock.licenceBlock().blockNumber(),
                thirdSubareaByBlock.licenceBlock().blockSuffix()
            ),
            tuple(
                fourthSubareaByBlock.licenceBlock().quadrantNumber(),
                fourthSubareaByBlock.licenceBlock().blockNumber(),
                fourthSubareaByBlock.licenceBlock().blockSuffix()
            ),
            tuple(
                fifthSubareaByBlock.licenceBlock().quadrantNumber(),
                fifthSubareaByBlock.licenceBlock().blockNumber(),
                fifthSubareaByBlock.licenceBlock().blockSuffix()
            ),
            tuple(
                sixthSubareaByBlock.licenceBlock().quadrantNumber(),
                sixthSubareaByBlock.licenceBlock().blockNumber(),
                sixthSubareaByBlock.licenceBlock().blockSuffix()
            )
        );
  }

  @Test
  void compare_whenSameLicenceAndBlock_thenSortedBySubareaName() {

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .build();

    var thirdSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("c name")
        .build();

    var unsortedSubareaList = List.of(
        thirdSubareaByName,
        secondSubareaByName,
        firstSubareaByName
    );

    var sortedSubareaList =  unsortedSubareaList
        .stream()
        .sorted(LicenceBlockSubareaDto.sort())
        .toList();

    assertThat(sortedSubareaList)
        .extracting(subareaDto -> subareaDto.subareaName().value())
        .containsExactly(
            firstSubareaByName.subareaName().value(),
            secondSubareaByName.subareaName().value(),
            thirdSubareaByName.subareaName().value()
        );
  }

  @Test
  void compare_whenAllValuesNull_thenNoException() {

    var subareaWithAllNullValues = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType(null)
        .withLicenceNumber(null)
        .withQuadrantNumber(null)
        .withBlockNumber(null)
        .withBlockSuffix(null)
        .withSubareaName(null)
        .build();

    // without catering for nulls, the compareTo would throw a NullPointerException
    assertDoesNotThrow(() ->
        Stream.of(subareaWithAllNullValues, subareaWithAllNullValues)
            .sorted(LicenceBlockSubareaDto.sort())
            .toList()
    );
  }
}