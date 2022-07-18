package uk.co.nstauthority.offshoresafetydirective.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.function.Function;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskListSectionUtilsTest {

  private TaskListSection<Object> sectionA;
  private TaskListSection<Object> sectionB;

  @BeforeEach
  void setUp() {
    sectionA = createDummySection("Section A", 10);
    sectionB = createDummySection("Section B", 20);
  }

  @Test
  void createSectionView_assertPropertiesAreMapped() {
    TaskListItem<Object> itemA = createDummyItem("Item 1", 10, "/a", TestTaskListSection.class, obj -> true);
    TaskListItem<Object> itemB = createDummyItem("Item 2", 20, "/b", TestTaskListSection.class, obj -> true);

    var sectionView = TaskListSectionUtils.createSectionView(sectionB, List.of(itemB, itemA));
    assertThat(sectionView.taskListItemViews()).extracting(
        TaskListItemView::name,
        TaskListItemView::displayOrder,
        TaskListItemView::actionUrl
    ).containsExactly(
        Tuple.tuple(itemA.getName(), itemA.getDisplayOrder(), itemA.getActionUrl()),
        Tuple.tuple(itemB.getName(), itemB.getDisplayOrder(), itemB.getActionUrl())
    );
    assertThat(sectionView).extracting(TaskListSectionView::sectionName, TaskListSectionView::displayOrder)
        .containsExactly(sectionB.getSectionName(), sectionB.getDisplayOrder());
  }

  @Test
  void createSectionViews_assertPropertiesAreMapped() {
    var target = new Object();
    var sectionViews = TaskListSectionUtils.createSectionViews(List.of(sectionB, sectionA), target);
    assertThat(sectionViews).extracting(TaskListSectionView::sectionName, TaskListSectionView::displayOrder)
        .containsExactly(
            Tuple.tuple(sectionA.getSectionName(), sectionA.getDisplayOrder()),
            Tuple.tuple(sectionB.getSectionName(), sectionB.getDisplayOrder())
        );
  }

  @Test
  void getActiveTaskListItems_whenItemsInMultipleSections_thenOnlyItemsInSectionReturned() {
    TaskListItem<Object> visibleItemInSection = createDummyItem(
        "Item 1",
        10,
        "/a",
        TestTaskListSection.class,
        bj -> true
    );
    TaskListItem<Object> notVisibleItemInSection = createDummyItem(
        "Item 2",
        20,
        "/b",
        TestTaskListSection.class,
        obj -> false
    );
    TaskListItem<Object> visibleItemNotInSection = createDummyItem(
        "Item 3",
        20,
        "/c",
        OtherTestTaskListSection.class,
        obj -> true
    );

    var activeItems = TaskListSectionUtils.getActiveTaskListItems(List.of(visibleItemNotInSection, visibleItemInSection, notVisibleItemInSection), null,
        TestTaskListSection.class);

    assertThat(activeItems).extracting(TaskListItem::getName)
        .containsExactly(visibleItemInSection.getName())
        .doesNotContain(notVisibleItemInSection.getName(), visibleItemNotInSection.getName());
  }

  @Test
  void createSectionView_verifyTaskListItemsAreSortedByDisplayOrder() {
    TaskListItem<Object> firstTaskListItem = createDummyItem("Item 1", 10, "/a", TestTaskListSection.class, obj -> true);
    TaskListItem<Object> secondTaskListItem = createDummyItem("Item 2", 20, "/b", TestTaskListSection.class, obj -> true);

    var sectionView = TaskListSectionUtils.createSectionView(sectionB, List.of(secondTaskListItem, firstTaskListItem));

    assertThat(sectionView.taskListItemViews()).extracting(TaskListItemView::displayOrder, TaskListItemView::name)
        .containsExactly(
            tuple(firstTaskListItem.getDisplayOrder(), firstTaskListItem.getName()),
            tuple(secondTaskListItem.getDisplayOrder(), secondTaskListItem.getName())
        );
  }

  @Test
  void createSectionViews_verifyTaskListSectionsAreSortedByDisplayOrder() {

    var firstTaskListSection = createDummySection("Section A", 10);
    var lastTaskListSection= createDummySection("Section B", 20);

    var sectionView = TaskListSectionUtils.createSectionViews(
        List.of(lastTaskListSection, firstTaskListSection),
        new Object()
    );

    assertThat(sectionView).extracting(TaskListSectionView::displayOrder, TaskListSectionView::sectionName)
        .containsExactly(
            tuple(firstTaskListSection.getDisplayOrder(), firstTaskListSection.getSectionName()),
            tuple(lastTaskListSection.getDisplayOrder(), lastTaskListSection.getSectionName())
        );
  }

  private TaskListSection<Object> createDummySection(String sectionName, int displayOrder) {
    return new TaskListSection<>() {
      @Override
      public String getSectionName() {
        return sectionName;
      }

      @Override
      public int getDisplayOrder() {
        return displayOrder;
      }

      @Override
      public TaskListSectionView getSectionView(Object target) {
        return new TaskListSectionView(displayOrder, sectionName, List.of(
            new TaskListItemView(20, "", ""),
            new TaskListItemView(10, "", "")
        ));
      }
    };
  }

  private TaskListItem<Object> createDummyItem(String itemName, int displayOrder, String actionUrl,
                                               Class<? extends TaskListSection<Object>> associatedClass,
                                               Function<Object, Boolean> visibilityFunction) {
    return new TaskListItem<>() {
      @Override
      public String getName() {
        return itemName;
      }

      @Override
      public String getActionUrl() {
        return actionUrl;
      }

      @Override
      public int getDisplayOrder() {
        return displayOrder;
      }

      @Override
      public Class<? extends TaskListSection<Object>> getTaskListSection() {
        return associatedClass;
      }

      @Override
      public boolean isVisible(Object target) {
        return visibilityFunction.apply(target);
      }
    };
  }

  private interface TestTaskListSection extends TaskListSection<Object> {
  }

  private interface OtherTestTaskListSection extends TaskListSection<Object> {
  }
}