package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.mockito.BDDMockito.given;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationDto;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationFileController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class TerminationTimelineItemServiceTest {

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private AppointmentTerminationService appointmentTerminationService;

  @Mock
  private FileAssociationService fileAssociationService;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private RegulatorTeamService regulatorTeamService;

  @InjectMocks
  private TerminationTimelineItemService terminationTimelineItemService;

  @Test
  void getTimelineItemViews_whenTermination_andRegulator_thenPopulatedTerminationViewList() {
    var wuaId = 1L;
    var termination = AppointmentTerminationTestUtil.builder()
        .withCreatedTimestamp(Instant.now())
        .withCorrectedByWuaId(wuaId)
        .withReasonForTermination("reason")
        .withTerminationDate(LocalDate.of(2023, 8, 15))
        .build();

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .build();

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);
    given(regulatorTeamService.isMemberOfRegulatorTeam(loggedInUser)).willReturn(true);

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
        .willReturn(List.of(energyPortalUser));

    var appointments = List.of(AppointmentTestUtil.builder().build());

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    assertThat(resultingTerminationViewList)
        .extracting(
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminationDate"),
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("reasonForTermination"),
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminatedBy")

        )
        .containsExactly(
            tuple(
                DateUtil.formatLongDate(termination.getTerminationDate()),
                termination.getReasonForTermination(),
                energyPortalUser.displayName()
            )
        );

    assertThat(resultingTerminationViewList)
        .extracting(
            AssetTimelineItemView::title,
            AssetTimelineItemView::timelineEventType,
            AssetTimelineItemView::createdInstant,
            AssetTimelineItemView::eventDate
        ).containsExactly(
            tuple(
                 "Termination of appointment",
                 TimelineEventType.TERMINATION,
                 termination.getCreatedTimestamp(),
                 termination.getTerminationDate()
            )
        );
  }

  @Test
  void getTimelineItemViews_whenTermination_thenPopulatedFilesField() {
    var wuaId = 1L;
    var termination = AppointmentTerminationTestUtil.builder()
        .withCorrectedByWuaId(wuaId)
        .build();

    var appointments = List.of(termination.getAppointment());

    Map<String, Appointment> appointmentIdMap = Map.of(
        termination.getAppointment().getId().toString(),
        termination.getAppointment()
    );

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .build();

    var uploadedFileA = UploadedFileTestUtil.builder().withFilename("a").build();
    var uploadedFileViewA = UploadedFileViewTestUtil.fromUploadedFile(uploadedFileA);
    var uploadedFileB = UploadedFileTestUtil.builder().withFilename("B").build();
    var uploadedFileViewB = UploadedFileViewTestUtil.fromUploadedFile(uploadedFileB);
    var uploadedFileC = UploadedFileTestUtil.builder().withFilename("c").build();
    var uploadedFileViewC = UploadedFileViewTestUtil.fromUploadedFile(uploadedFileC);

    var fileAssociationDtoA = FileAssociationDto.from(
        FileAssociationTestUtil.builder()
            .withReferenceId(termination.getAppointment().getId().toString())
            .withUploadedFile(uploadedFileA)
            .build()
    );

    var fileAssociationDtoB = FileAssociationDto.from(
        FileAssociationTestUtil.builder()
            .withReferenceId(termination.getAppointment().getId().toString())
            .withUploadedFile(uploadedFileB)
            .build()
    );

    var fileAssociationDtoC = FileAssociationDto.from(
        FileAssociationTestUtil.builder()
            .withUploadedFile(uploadedFileC)
            .withReferenceId(termination.getAppointment().getId().toString())
            .build()
    );

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    given(fileAssociationService.getSubmittedUploadedFileAssociations(
        FileAssociationType.APPOINTMENT, appointmentIdMap.keySet()))
        .willReturn(List.of(fileAssociationDtoA, fileAssociationDtoB, fileAssociationDtoC));

    //return unordered files
    var fileUploadIds = List.of(
        fileAssociationDtoA.uploadedFileId(), fileAssociationDtoB.uploadedFileId(), fileAssociationDtoC.uploadedFileId()
    );

    given(fileUploadService.getUploadedFileViewList(fileUploadIds))
        .willReturn(List.of(uploadedFileViewA, uploadedFileViewC, uploadedFileViewB));

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
        .willReturn(List.of(energyPortalUser));

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);
    given(regulatorTeamService.isMemberOfRegulatorTeam(loggedInUser)).willReturn(true);

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    assertThat(resultingTerminationViewList)
        .extracting(
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminationFiles")
        ).containsExactly(
            List.of(
                new FileSummaryView(
                    uploadedFileViewA,
                    ReverseRouter.route(on(AppointmentTerminationFileController.class)
                        .download(
                            new AppointmentId(termination.getAppointment().getId()),
                            new UploadedFileId(UUID.fromString(uploadedFileViewA.fileId())
                            )
                        )
                    )
                ),
                new FileSummaryView(
                    uploadedFileViewB, //assert files are ordered regardless of case
                    ReverseRouter.route(on(AppointmentTerminationFileController.class)
                        .download(
                            new AppointmentId(termination.getAppointment().getId()),
                            new UploadedFileId(UUID.fromString(uploadedFileViewB.fileId())
                            )
                        )
                    )
                ),
                new FileSummaryView(
                    uploadedFileViewC,
                    ReverseRouter.route(on(AppointmentTerminationFileController.class)
                        .download(
                            new AppointmentId(termination.getAppointment().getId()),
                            new UploadedFileId(UUID.fromString(uploadedFileViewC.fileId())
                            )
                        )
                    )
                )
            ));
  }

  @Test
  void getTimelineItemViews_whenTermination_andUserIsNotRegulator_thenOnlyDateField() {
    var wuaId = 1L;
    var termination = AppointmentTerminationTestUtil.builder()
        .withCreatedTimestamp(Instant.now())
        .withCorrectedByWuaId(wuaId)
        .withReasonForTermination("reason")
        .withTerminationDate(LocalDate.of(2023, 8, 15))
        .build();
    var appointments = List.of(termination.getAppointment());

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .build();

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);
    given(regulatorTeamService.isMemberOfRegulatorTeam(loggedInUser)).willReturn(false);
    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
        .willReturn(List.of(energyPortalUser));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(List.of(termination.getAppointment()));
    var resultingModelProperties = resultingTerminationViewList.get(0).assetTimelineModelProperties().getModelProperties();

    assertThat(resultingModelProperties)
        .containsExactly(
            entry("terminationDate", DateUtil.formatLongDate(termination.getTerminationDate()))
        );
  }

  @Test
  void getTimelineItemViews_whenTermination_andUserIsNotLoggedIn_thenOnlyDateField() {
    var wuaId = 1L;
    var termination = AppointmentTerminationTestUtil.builder()
        .withCreatedTimestamp(Instant.now())
        .withCorrectedByWuaId(wuaId)
        .withReasonForTermination("reason")
        .withTerminationDate(LocalDate.of(2023, 8, 15))
        .build();
    var appointments = List.of(termination.getAppointment());

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .build();

    given(userDetailService.getUserDetail()).willThrow(InvalidAuthenticationException.class);
    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
        .willReturn(List.of(energyPortalUser));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(List.of(termination.getAppointment()));
    var resultingModelProperties = resultingTerminationViewList.get(0).assetTimelineModelProperties().getModelProperties();

    assertThat(resultingModelProperties)
        .containsExactly(
            entry("terminationDate", DateUtil.formatLongDate(termination.getTerminationDate()))
        );
  }

  @Test
  void getTimelineItemViews_whenWuaIdNotInEnergyPortal_thenThrow() {
     var wuaId = 1L;
     var terminations = List.of(AppointmentTerminationTestUtil.builder().withCorrectedByWuaId(wuaId).build());
     var appointments = List.of(AppointmentTestUtil.builder().build());

     given(appointmentTerminationService.getTerminations(appointments))
         .willReturn(terminations);

     given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
         .willReturn(Collections.emptyList());

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);
    given(regulatorTeamService.isMemberOfRegulatorTeam(loggedInUser)).willReturn(true);

     var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    assertThat(resultingTerminationViewList)
        .extracting(
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminatedBy")

        )
        .containsExactly("Unknown");
  }
}