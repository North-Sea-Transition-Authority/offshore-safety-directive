package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.deletion.DeleteNominationController;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListItemView;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListLabel;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListLabelType;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionView;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = NominationTaskListController.class)
class NominationTaskListControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail NOMINATION_CREATOR_USER = ServiceUserDetailTestUtil.Builder().build();

  private static final TeamMember NOMINATION_CREATOR_TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
      .build();

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private NominationDetail nominationDetail;

  @MockBean
  private NominationTaskListSection nominationTaskListSection;

  @MockBean
  private NominationTaskListItem nominationTaskListItem;

  @BeforeEach
  void setup() {

    nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    when(teamMemberService.getUserAsTeamMembers(NOMINATION_CREATOR_USER))
        .thenReturn(Collections.singletonList(NOMINATION_CREATOR_TEAM_MEMBER));
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    setupMockTaskListSection("sectionName", 10, "sectionWarningText");

    var nominationTaskListItemType = new NominationTaskListItemType(nominationDetail);

    var expectedTaskListItemView = TaskListTestUtil.getItemViewBuilder(20, "display name", "/action-url")
        .build();

    setupMockTaskListItem(expectedTaskListItemView, nominationTaskListItemType);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        )
        .test();
  }

  @SecurityTest
  void smokeTestPermissions_onlyCreateNominationPermissionAllowed() {

    setupMockTaskListSection("sectionName", 10, "sectionWarningText");

    var nominationTaskListItemType = new NominationTaskListItemType(nominationDetail);

    var expectedTaskListItemView = TaskListTestUtil.getItemViewBuilder(20, "display name", "/action-url")
        .build();

    setupMockTaskListItem(expectedTaskListItemView, nominationTaskListItemType);

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.CREATE_NOMINATION))
        .withUser(NOMINATION_CREATOR_USER)
        .withGetEndpoint(
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        )
        .test();
  }

  @Test
  void getTaskList_assertModelProperties() throws Exception {

    var sectionName = "section name";
    var sectionDisplayOrder = 10;
    var sectionWarningText = "section warning text";

    setupMockTaskListSection(sectionName, sectionDisplayOrder, sectionWarningText);

    var nominationTaskListItemType = new NominationTaskListItemType(nominationDetail);

    var expectedTaskListItemView = TaskListTestUtil.getItemViewBuilder(20, "display name", "/action-url")
        .withTaskListLabels(true)
        .withNotCompletedLabel(false)
        .withItemValid(true)
        .withCustomTaskListLabel(new TaskListLabel("label text", TaskListLabelType.GREY))
        .build();

    setupMockTaskListItem(expectedTaskListItemView, nominationTaskListItemType);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
                .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/tasklist/taskList"))
        .andExpect(model().attribute(
            "breadcrumbsList",
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), WorkAreaController.WORK_AREA_TITLE
            )
        ))
        .andExpect(model().attribute("currentPage", NominationTaskListController.PAGE_NAME))
        .andExpect(model().attributeExists("taskListSections"))
        .andExpect(model().attribute("deleteNominationUrl",
            ReverseRouter.route(on(DeleteNominationController.class).deleteNomination(NOMINATION_ID, null))))
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

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

  @Test
  void getTaskList_whenFirstNominationVersion_thenAssertModelProperties() throws Exception {

    givenTaskListSectionsExist();

    nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(model().attribute("deleteNominationButtonPrompt", "Delete nomination"));
  }

  @Test
  void getTaskList_whenNotFirstNominationVersion_thenAssertModelProperties() throws Exception {

    givenTaskListSectionsExist();

    nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(2)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
        )
        .andExpect(model().attribute("deleteNominationButtonPrompt", "Delete draft update"));
  }

  @Test
  void getTaskList_whenFirstNominationVersion_thenUpdateReasonNotRetrieved() throws Exception {
    givenTaskListSectionsExist();

    nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
    ).andExpect(model().attributeDoesNotExist("reasonForUpdate"));

    verify(nominationDetailService, never()).getLatestNominationDetailWithStatuses(eq(NOMINATION_ID), any());
    verify(caseEventQueryService, never()).getLatestReasonForUpdate(any());
  }

  @Test
  void getTaskList_whenNotFirstNomination_andNoUpdateRequested_thenNoUpdateReasonInModel() throws Exception {
    givenTaskListSectionsExist();

    nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(2)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    var latestSubmittedNominationDetail = NominationDetailTestUtil.builder().build();
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    ))
        .thenReturn(Optional.of(latestSubmittedNominationDetail));

    when(caseEventQueryService.getLatestReasonForUpdate(latestSubmittedNominationDetail))
        .thenReturn(Optional.empty());

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
    ).andExpect(model().attributeDoesNotExist("reasonForUpdate"));
  }

  @Test
  void getTaskList_whenNotFirstNomination_andUpdateRequested_thenUpdateReasonInModel() throws Exception {
    givenTaskListSectionsExist();

    nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(2)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    var latestSubmittedNominationDetail = NominationDetailTestUtil.builder().build();
    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        EnumSet.of(NominationStatus.SUBMITTED)
    ))
        .thenReturn(Optional.of(latestSubmittedNominationDetail));

    var reasonForUpdate = "reason";
    when(caseEventQueryService.getLatestReasonForUpdate(latestSubmittedNominationDetail))
        .thenReturn(Optional.of(reasonForUpdate));

    mockMvc.perform(
        get(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)))
            .with(user(NOMINATION_CREATOR_USER))
    ).andExpect(model().attribute("reasonForUpdate", reasonForUpdate));
  }

  private void givenTaskListSectionsExist() {
    var sectionName = "section name";
    var sectionDisplayOrder = 10;
    var sectionWarningText = "section warning text";

    setupMockTaskListSection(sectionName, sectionDisplayOrder, sectionWarningText);

    var nominationTaskListItemType = new NominationTaskListItemType(nominationDetail);

    var expectedTaskListItemView = TaskListTestUtil.getItemViewBuilder(20, "display name", "/action-url")
        .withTaskListLabels(true)
        .withNotCompletedLabel(false)
        .withItemValid(true)
        .withCustomTaskListLabel(new TaskListLabel("label text", TaskListLabelType.GREY))
        .build();

    setupMockTaskListItem(expectedTaskListItemView, nominationTaskListItemType);
  }

  private void setupMockTaskListItem(TaskListItemView taskListItemViewToReturn,
                                     NominationTaskListItemType nominationTaskListItemType) {

    doReturn(nominationTaskListSection.getClass()).when(nominationTaskListItem).getTaskListSection();

    when(nominationTaskListItem.isVisible(nominationTaskListItemType)).thenReturn(true);

    when(nominationTaskListItem.getDisplayOrder())
        .thenReturn(taskListItemViewToReturn.getDisplayOrder());

    when(nominationTaskListItem.getActionUrl(nominationTaskListItemType))
        .thenReturn(taskListItemViewToReturn.getActionUrl());

    when(nominationTaskListItem.getItemDisplayText())
        .thenReturn(taskListItemViewToReturn.getDisplayName());

    when(nominationTaskListItem.showTaskListLabels(nominationTaskListItemType))
        .thenReturn(taskListItemViewToReturn.showTaskListLabels());

    when(nominationTaskListItem.showNotCompletedLabels(nominationTaskListItemType))
        .thenReturn(taskListItemViewToReturn.showNotCompletedLabel());

    when(nominationTaskListItem.isValid(nominationTaskListItemType))
        .thenReturn(taskListItemViewToReturn.isItemValid());

    when(nominationTaskListItem.getCustomTaskListLabel(nominationTaskListItemType))
        .thenReturn(taskListItemViewToReturn.getCustomTaskListLabel());
  }

  private void setupMockTaskListSection(String sectionName, int sectionDisplayOrder, String sectionWarningText) {
    when(nominationTaskListSection.getSectionName()).thenReturn(sectionName);
    when(nominationTaskListSection.getDisplayOrder()).thenReturn(sectionDisplayOrder);
    when(nominationTaskListSection.getSectionWarningText()).thenReturn(sectionWarningText);
  }

}