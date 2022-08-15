package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;

@IntegrationTest
class TaskListEnforceUniqueValuesTest {

  @Autowired
  private List<NominationTaskListSection> nominationTaskListSections;

  @Autowired
  private List<NominationTaskListItem> nominationTaskListItems;

  @ParameterizedTest
  @ValueSource(strings = {"getDisplayOrder", "getSectionName"})
  void nominationTaskListSections_verifyAllImplementationsHaveUniqueValues(String methodName) {

    var distinctValuesList = nominationTaskListSections
        .stream()
        .map(nominationTaskListSection -> invokeMethod(nominationTaskListSection, methodName))
        .distinct()
        .toList();

    var uniquenessViolationErrorMessagesSuffix = nominationTaskListSections
        .stream()
        .map(nominationTaskListSection -> "[%s: %s]".formatted(
            nominationTaskListSection.getClass().getSimpleName(),
            invokeMethod(nominationTaskListSection, methodName)
        ))
        .toList();

    assertEquals(
        distinctValuesList.size(),
        nominationTaskListSections.size(),
        (
            "A NominationTaskListSection has the same return value for %s as another section. " +
            "Expectation is that each section has a unique value for this method. %s"
        ).formatted(
            methodName,
            uniquenessViolationErrorMessagesSuffix
        )
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"getDisplayOrder", "getItemDisplayText"})
  void nominationTaskListItems_verifyAllImplementationsHaveUniqueValues(String methodName) {

    var sectionToItemMap = nominationTaskListItems
        .stream()
        .collect(Collectors.groupingBy(NominationTaskListItem::getTaskListSection));

    sectionToItemMap.forEach((nominationTaskListSection, nominationTaskListSectionItems) -> {

      var distinctValuesList = nominationTaskListSectionItems
          .stream()
          .map(nominationTaskListItem -> invokeMethod(nominationTaskListItem, methodName))
          .distinct()
          .toList();

      var uniquenessViolationErrorMessagesSuffix = nominationTaskListSectionItems
          .stream()
          .map(nominationTaskListItem -> "[%s: %s]".formatted(
              nominationTaskListItem.getClass().getSimpleName(),
              invokeMethod(nominationTaskListItem, methodName)
          ))
          .toList();

      assertEquals(
          distinctValuesList.size(),
          nominationTaskListSectionItems.size(),
          (
              "A NominationTaskListItem in section %s has the same return value for %s as another item " +
              "in the section. Expectation is that each item has a unique value for this method in each section. %s"
          ).formatted(
              nominationTaskListSection.getSimpleName(),
              methodName,
              uniquenessViolationErrorMessagesSuffix
          )
      );
    });
  }

  private Object invokeMethod(NominationTaskListSection nominationTaskListSection, String methodName) {
    try {
      return Objects.requireNonNull(
          BeanUtils.findMethod(nominationTaskListSection.getClass(), methodName)
      ).invoke(nominationTaskListSection);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Object invokeMethod(NominationTaskListItem nominationTaskListItem, String methodName) {

    var method = BeanUtils.findMethod(nominationTaskListItem.getClass(), methodName);
    assert method != null;

    // setting method accessible as interface implementations are package
    // private, and we don't want this to change just for the tests
    method.setAccessible(true);

    try {
      return Objects.requireNonNull(method).invoke(nominationTaskListItem);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
