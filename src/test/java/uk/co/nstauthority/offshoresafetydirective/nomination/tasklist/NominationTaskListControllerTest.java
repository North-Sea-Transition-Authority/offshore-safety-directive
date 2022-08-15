package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListItemView;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListLabel;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListLabelType;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionView;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListTestUtil;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@WebMvcTest
@ContextConfiguration(classes = NominationTaskListController.class)
@WithMockUser
class NominationTaskListControllerTest extends AbstractControllerTest {

  @MockBean
  NominationDetailService nominationDetailService;

  @MockBean
  NominationTaskListSection nominationTaskListSection;

  @MockBean
  NominationTaskListItem nominationTaskListItem;

  @Test
  void getTaskList_assertModelProperties() throws Exception {

    var nominationId = new NominationId(1);
    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(nominationId)
        .build();

    when(nominationDetailService.getLatestNominationDetail(nominationId)).thenReturn(nominationDetail);

    var sectionName = "section name";
    var sectionDisplayOrder = 10;
    var sectionWarningText = "section warning text";

    when(nominationTaskListSection.getSectionName()).thenReturn(sectionName);
    when(nominationTaskListSection.getDisplayOrder()).thenReturn(sectionDisplayOrder);
    when(nominationTaskListSection.getSectionWarningText()).thenReturn(sectionWarningText);

    var nominationTaskListItemType = new NominationTaskListItemType(nominationDetail);

    var expectedTaskListItemView = TaskListTestUtil.getItemViewBuilder(10, "display name", "/action-url")
        .withTaskListLabels(true)
        .withNotCompletedLabel(false)
        .withItemValid(true)
        .withCustomTaskListLabel(new TaskListLabel("label text", TaskListLabelType.GREY))
        .build();

    doReturn(nominationTaskListSection.getClass()).when(nominationTaskListItem).getTaskListSection();
    when(nominationTaskListItem.isVisible(nominationTaskListItemType)).thenReturn(true);

    when(nominationTaskListItem.getDisplayOrder()).thenReturn(expectedTaskListItemView.getDisplayOrder());
    when(nominationTaskListItem.getActionUrl(nominationTaskListItemType)).thenReturn(expectedTaskListItemView.getActionUrl());
    when(nominationTaskListItem.getItemDisplayText()).thenReturn(expectedTaskListItemView.getDisplayName());
    when(nominationTaskListItem.showTaskListLabels(nominationTaskListItemType)).thenReturn(expectedTaskListItemView.showTaskListLabels());
    when(nominationTaskListItem.showNotCompletedLabels(nominationTaskListItemType)).thenReturn(expectedTaskListItemView.showNotCompletedLabel());
    when(nominationTaskListItem.isValid(nominationTaskListItemType)).thenReturn(expectedTaskListItemView.isItemValid());
    when(nominationTaskListItem.getCustomTaskListLabel(nominationTaskListItemType)).thenReturn(expectedTaskListItemView.getCustomTaskListLabel());

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    assertEquals(modelAndView.getViewName(), "osd/nomination/tasklist/taskList");

    assertThat(modelAndView.getModel()).containsOnlyKeys(
        "taskListSections",
        "breadcrumbsList",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "currentPage",
        "navigationItems",
        "currentEndPoint"
    );

    @SuppressWarnings("unchecked")
    var breadcrumbs = (Map<String, String>) modelAndView.getModel().get("breadcrumbsList");

    assertThat(breadcrumbs)
        .containsExactly(
            entry(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), WorkAreaController.WORK_AREA_TITLE)
        );

    assertEquals(modelAndView.getModel().get("currentPage"), NominationTaskListController.PAGE_NAME);

    @SuppressWarnings("unchecked")
    var taskListSectionViews = (List<TaskListSectionView>) modelAndView.getModel().get("taskListSections");

    assertThat(taskListSectionViews)
        .extracting(
            TaskListSectionView::sectionName,
            TaskListSectionView::displayOrder,
            TaskListSectionView::sectionWarningText
        )
        .containsExactly(
            tuple(
                sectionName,
                sectionDisplayOrder,
                sectionWarningText
            )
        );

    assertThat(taskListSectionViews.get(0).taskListItemViews())
        .extracting(
            TaskListItemView::getDisplayOrder,
            TaskListItemView::getDisplayName,
            TaskListItemView::getActionUrl,
            TaskListItemView::isItemValid,
            TaskListItemView::showTaskListLabels,
            TaskListItemView::showNotCompletedLabel,
            TaskListItemView::getCustomTaskListLabel
        )
        .containsExactly(
            tuple(
                expectedTaskListItemView.getDisplayOrder(),
                expectedTaskListItemView.getDisplayName(),
                expectedTaskListItemView.getActionUrl(),
                expectedTaskListItemView.isItemValid(),
                expectedTaskListItemView.showTaskListLabels(),
                expectedTaskListItemView.showNotCompletedLabel(),
                expectedTaskListItemView.getCustomTaskListLabel()
            )
        );
  }

}