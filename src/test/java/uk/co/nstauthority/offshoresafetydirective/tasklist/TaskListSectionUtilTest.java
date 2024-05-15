package uk.co.nstauthority.offshoresafetydirective.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskListSectionUtilTest {

  private static final Object TARGET = new Object();

  @Mock
  private TaskListSection<Object> taskListSection;

  @Mock
  private TaskListItem<Object> taskListItemA;

  @Mock
  private TaskListItem<Object> taskListItemB;

  @Test
  void createSectionView_verifySectionViewPropertyMappings() {

    // given a section
    var sectionDisplayName = "section name";
    var sectionDisplayOrder = 10;
    var sectionWarningText = "section warning text";

    when(taskListSection.getSectionName()).thenReturn(sectionDisplayName);
    when(taskListSection.getDisplayOrder()).thenReturn(sectionDisplayOrder);
    when(taskListSection.getSectionWarningText()).thenReturn(sectionWarningText);

    // with one visible item
    var itemDisplayName = "item name";
    var itemSectionClass = taskListSection.getClass();
    var itemIsVisible = true;

    when(taskListItemA.getItemDisplayText()).thenReturn(itemDisplayName);
    when(taskListItemA.isVisible(TARGET)).thenReturn(itemIsVisible);
    doReturn(itemSectionClass).when(taskListItemA).getTaskListSection();

    // when section and item is provided
    var resultingTaskListSectionView = TaskListSectionUtil.createSectionView(
        taskListSection,
        List.of(taskListItemA),
        TARGET
    );

    // then all properties are mapped correctly
    assertEquals(resultingTaskListSectionView.sectionName(), sectionDisplayName);
    assertEquals(resultingTaskListSectionView.displayOrder(), sectionDisplayOrder);
    assertEquals(resultingTaskListSectionView.sectionWarningText(), sectionWarningText);
    assertThat(resultingTaskListSectionView.taskListItemViews())
        .extracting(TaskListItemView::getDisplayName)
        .containsExactly(itemDisplayName);
  }

  @Test
  void createSectionView_whenMultipleItemsInSection_thenSortedByItemDisplayOrder() {

    var firstItemByDisplayOrder = taskListItemA;
    var secondItemByDisplayOrder = taskListItemB;

    // given two task list items with different display orders
    when(firstItemByDisplayOrder.getDisplayOrder()).thenReturn(10);
    when(secondItemByDisplayOrder.getDisplayOrder()).thenReturn(20);

    // and each item is part of the section being tested
    doReturn(taskListSection.getClass()).when(firstItemByDisplayOrder).getTaskListSection();
    doReturn(taskListSection.getClass()).when(secondItemByDisplayOrder).getTaskListSection();

    // and each item is visible
    when(firstItemByDisplayOrder.isVisible(TARGET)).thenReturn(true);
    when(secondItemByDisplayOrder.isVisible(TARGET)).thenReturn(true);

    // when they task list items are added out or order
    var resultingTaskListSectionView = TaskListSectionUtil.createSectionView(
        taskListSection,
        List.of(secondItemByDisplayOrder, firstItemByDisplayOrder),
        TARGET
    );

    // then the returned task list items are sorted by display order
    assertThat(resultingTaskListSectionView.taskListItemViews())
        .extracting(TaskListItemView::getDisplayOrder)
        .containsExactly(
            firstItemByDisplayOrder.getDisplayOrder(),
            secondItemByDisplayOrder.getDisplayOrder()
        );
  }

  @Test
  void createSectionView_whenItemsNotInSection_thenOnlySectionScopedItemsAreReturned() {

    var otherTaskListSection = mock(OtherTaskListSection.class);

    var taskListItemInSection = taskListItemA;
    var taskListItemNotInSection = taskListItemB;

    // given task list items in different task list sections
    doReturn(taskListSection.getClass()).when(taskListItemInSection).getTaskListSection();
    doReturn(otherTaskListSection.getClass()).when(taskListItemNotInSection).getTaskListSection();

    // and the item in the section being tested is visible
    when(taskListItemInSection.isVisible(TARGET)).thenReturn(true);
    when(taskListItemInSection.getItemDisplayText()).thenReturn("task list item in section");

    // when they task list items in different sections are added
    var resultingTaskListSectionView = TaskListSectionUtil.createSectionView(
        taskListSection,
        List.of(taskListItemInSection, taskListItemNotInSection),
        TARGET
    );

    // then only the task list item in the relevant section is returned
    assertThat(resultingTaskListSectionView.taskListItemViews())
        .extracting(TaskListItemView::getDisplayName)
        .containsExactly(
            taskListItemInSection.getItemDisplayText()
        );
  }

  @Test
  void createSectionView_whenSomeItemsNotVisible_thenOnlyVisibleItemsAreReturned() {

    var visibleTaskListItem = taskListItemA;
    var notVisibleTaskListItem = taskListItemB;

    // given a visible and not visible item
    when(visibleTaskListItem.isVisible(TARGET)).thenReturn(true);
    when(notVisibleTaskListItem.isVisible(TARGET)).thenReturn(false);

    // and they are in the same section
    doReturn(taskListSection.getClass()).when(visibleTaskListItem).getTaskListSection();
    doReturn(taskListSection.getClass()).when(notVisibleTaskListItem).getTaskListSection();

    when(visibleTaskListItem.getItemDisplayText()).thenReturn("visible task list item");

    // when both items are provided
    var resultingTaskListSectionView = TaskListSectionUtil.createSectionView(
        taskListSection,
        List.of(visibleTaskListItem, notVisibleTaskListItem),
        TARGET
    );

    // then only the visible item is returned
    assertThat(resultingTaskListSectionView.taskListItemViews())
        .extracting(TaskListItemView::getDisplayName)
        .containsExactly(
            visibleTaskListItem.getItemDisplayText()
        );
  }

  @Test
  void createTaskListView_verifyPropertiesAreMappedCorrectly() {

    var expectedTaskListItemView = TaskListTestUtil.getItemViewBuilder(10, "display name", "/action-url")
        .withTaskListLabels(true)
        .withNotCompletedLabel(false)
        .withItemValid(true)
        .withCustomTaskListLabel(new TaskListLabel("label text", TaskListLabelType.GREY))
        .build();

    when(taskListItemA.getDisplayOrder()).thenReturn(expectedTaskListItemView.getDisplayOrder());
    when(taskListItemA.getItemDisplayText()).thenReturn(expectedTaskListItemView.getDisplayName());
    when(taskListItemA.getActionUrl(TARGET)).thenReturn(expectedTaskListItemView.getActionUrl());
    when(taskListItemA.isValid(TARGET)).thenReturn(expectedTaskListItemView.isItemValid());
    when(taskListItemA.showTaskListLabels(TARGET)).thenReturn(expectedTaskListItemView.showTaskListLabels());
    when(taskListItemA.showNotCompletedLabels(TARGET)).thenReturn(expectedTaskListItemView.showNotCompletedLabel());
    when(taskListItemA.getCustomTaskListLabel(TARGET)).thenReturn(expectedTaskListItemView.getCustomTaskListLabel());

    var resultingTaskListItem = TaskListSectionUtil.createTaskListItemView(
        taskListItemA,
        TARGET
    );

    assertEquals(resultingTaskListItem.getDisplayOrder(), expectedTaskListItemView.getDisplayOrder());
    assertEquals(resultingTaskListItem.getDisplayName(), expectedTaskListItemView.getDisplayName());
    assertEquals(resultingTaskListItem.getActionUrl(), expectedTaskListItemView.getActionUrl());
    assertEquals(resultingTaskListItem.isItemValid(), expectedTaskListItemView.isItemValid());
    assertEquals(resultingTaskListItem.showTaskListLabels(), expectedTaskListItemView.showTaskListLabels());
    assertEquals(resultingTaskListItem.showNotCompletedLabel(), expectedTaskListItemView.showNotCompletedLabel());
    assertEquals(resultingTaskListItem.getCustomTaskListLabel(), expectedTaskListItemView.getCustomTaskListLabel());
  }

  @Test
  void createSectionViews_verifySectionViewPropertyMappings() {

    // given a section
    var sectionDisplayName = "section name";
    var sectionDisplayOrder = 10;
    var sectionWarningText = "section warning text";

    when(taskListSection.getSectionName()).thenReturn(sectionDisplayName);
    when(taskListSection.getDisplayOrder()).thenReturn(sectionDisplayOrder);
    when(taskListSection.getSectionWarningText()).thenReturn(sectionWarningText);

    // with one visible item
    var itemDisplayName = "item display name";
    when(taskListItemA.getItemDisplayText()).thenReturn(itemDisplayName);
    when(taskListItemA.isVisible(TARGET)).thenReturn(true);
    doReturn(taskListSection.getClass()).when(taskListItemA).getTaskListSection();

    // when section and item is provided
    var resultingTaskListSectionViews = TaskListSectionUtil.createSectionViews(
        List.of(taskListSection),
        List.of(taskListItemA),
        TARGET
    );

    // then all properties are mapped correctly
    assertThat(resultingTaskListSectionViews)
        .extracting(
            TaskListSectionView::displayOrder,
            TaskListSectionView::sectionName,
            TaskListSectionView::sectionWarningText
        )
        .containsExactly(
            tuple(
                sectionDisplayOrder,
                sectionDisplayName,
                sectionWarningText
            )
        );

    assertThat(resultingTaskListSectionViews.get(0).taskListItemViews())
        .extracting(TaskListItemView::getDisplayName)
        .containsExactly(itemDisplayName);
  }

  @Test
  void createSectionViews_whenMultipleSections_thenSortedByDisplayOrder() {

    var firstNumericalDisplayOrder = 10;
    var secondNumericalDisplayOrder = 20;

    var firstSectionByDisplayOrder = taskListSection;
    var secondSectionByDisplayOrder = mock(OtherTaskListSection.class);

    // given two task list sections with different display orders
    when(firstSectionByDisplayOrder.getDisplayOrder()).thenReturn(firstNumericalDisplayOrder);
    when(secondSectionByDisplayOrder.getDisplayOrder()).thenReturn(secondNumericalDisplayOrder);

    // and both task list sections contain at least one visible task list item
    doReturn(firstSectionByDisplayOrder.getClass()).when(taskListItemA).getTaskListSection();
    when(taskListItemA.isVisible(TARGET)).thenReturn(true);

    doReturn(secondSectionByDisplayOrder.getClass()).when(taskListItemB).getTaskListSection();
    when(taskListItemB.isVisible(TARGET)).thenReturn(true);

    // when the sections are passed in out of order
    var resultingTaskListSectionViews = TaskListSectionUtil.createSectionViews(
        List.of(secondSectionByDisplayOrder, firstSectionByDisplayOrder),
        List.of(taskListItemA, taskListItemB),
        TARGET
    );

    // then the sections are sorted by display order
    assertThat(resultingTaskListSectionViews)
        .extracting(TaskListSectionView::displayOrder)
        .containsExactly(
            firstNumericalDisplayOrder,
            secondNumericalDisplayOrder
        );
  }

  @Test
  void createSectionViews_whenSectionHasNoItems_thenSectionIsNotReturned() {

    // given two task list sections
    var sectionWithTaskListItem = taskListSection;
    var sectionWithoutTaskListItem = mock(OtherTaskListSection.class);

    when(sectionWithTaskListItem.getSectionName()).thenReturn("section with task list item");

    // and all task list items are visible and associated to only one section
    doReturn(sectionWithTaskListItem.getClass()).when(taskListItemA).getTaskListSection();
    when(taskListItemA.isVisible(TARGET)).thenReturn(true);

    doReturn(sectionWithTaskListItem.getClass()).when(taskListItemB).getTaskListSection();

    var resultingTaskListSectionViews = TaskListSectionUtil.createSectionViews(
        List.of(sectionWithTaskListItem, sectionWithoutTaskListItem),
        List.of(taskListItemA, taskListItemB),
        TARGET
    );

    assertThat(resultingTaskListSectionViews)
        .extracting(TaskListSectionView::sectionName)
        .containsExactly(sectionWithTaskListItem.getSectionName());
  }

  @Test
  void createSectionViews_whenNoSectionContainItems_thenNoSectionsReturned() {

    // give a task list item in a section different to the one being tested
    doReturn(OtherTaskListSection.class).when(taskListItemA).getTaskListSection();

    // when the task list section views are created
    var resultingTaskListSectionViews = TaskListSectionUtil.createSectionViews(
        List.of(taskListSection),
        List.of(taskListItemA),
        TARGET
    );

    // then there will be no sections returned
    assertThat(resultingTaskListSectionViews).isEmpty();
  }

  private interface OtherTaskListSection extends TaskListSection<Object> {}
}