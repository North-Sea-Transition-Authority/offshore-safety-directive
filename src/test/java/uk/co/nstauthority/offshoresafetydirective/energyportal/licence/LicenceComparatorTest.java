package uk.co.nstauthority.offshoresafetydirective.energyportal.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.Test;

class LicenceComparatorTest {

  @Test
  void compare_whenDifferentLicenceType_thenOrderedByLicenceType() {

    var firstLicence = LicenceDtoTestUtil.builder()
        .withLicenceType("a")
        .build();

    var secondLicence = LicenceDtoTestUtil.builder()
        .withLicenceType("b")
        .build();

    var thirdLicence = LicenceDtoTestUtil.builder()
        .withLicenceType("C")
        .build();

    var unsortedList = List.of(
        thirdLicence,
        secondLicence,
        firstLicence
    );

    var sortedList = unsortedList
        .stream()
        .sorted(new LicenceComparator())
        .toList();

    assertThat(sortedList)
        .extracting(LicenceDto::licenceType)
        .containsExactly(
            firstLicence.licenceType(),
            secondLicence.licenceType(),
            thirdLicence.licenceType()
        );
  }

  @Test
  void compare_whenDifferentLicenceNumber_thenOrderedByLicenceNumber() {

    var firstLicence = LicenceDtoTestUtil.builder()
        .withLicenceNumber(1)
        .build();

    var secondLicence = LicenceDtoTestUtil.builder()
        .withLicenceNumber(2)
        .build();

    var thirdLicence = LicenceDtoTestUtil.builder()
        .withLicenceNumber(10)
        .build();

    var unsortedList = List.of(
        thirdLicence,
        secondLicence,
        firstLicence
    );

    var sortedList = unsortedList
        .stream()
        .sorted(new LicenceComparator())
        .toList();

    assertThat(sortedList)
        .extracting(LicenceDto::licenceNumber)
        .containsExactly(
            firstLicence.licenceNumber(),
            secondLicence.licenceNumber(),
            thirdLicence.licenceNumber()
        );
  }

  @Test
  void compare_whenAllPropertiesNull_thenNullsFirstInList() {

    var licenceWithNullProperties = LicenceDtoTestUtil.builder()
        .withLicenceType(null)
        .withLicenceNumber(null)
        .withLicenceReference(null)
        .build();

    var licenceWithNoNullProperties = LicenceDtoTestUtil.builder().build();

    var unsortedList = List.of(licenceWithNoNullProperties, licenceWithNullProperties);

    var sortedList = unsortedList
        .stream()
        .sorted(new LicenceComparator())
        .toList();

    assertThat(sortedList)
        .extracting(
            LicenceDto::licenceType,
            LicenceDto::licenceNumber
        )
        .containsExactly(
            tuple(
                licenceWithNullProperties.licenceType(),
                licenceWithNullProperties.licenceNumber()
            ),
            tuple(
                licenceWithNoNullProperties.licenceType(),
                licenceWithNoNullProperties.licenceNumber()
            )
        );
  }
}