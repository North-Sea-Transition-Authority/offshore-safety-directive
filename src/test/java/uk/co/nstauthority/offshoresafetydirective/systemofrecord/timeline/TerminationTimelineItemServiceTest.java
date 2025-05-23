package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTermination;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationFileController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class TerminationTimelineItemServiceTest {

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private AppointmentTerminationService appointmentTerminationService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private FileService fileService;

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

    given(teamQueryService.userHasAtLeastOneStaticRole(
        loggedInUser.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .willReturn(true);

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
        .willReturn(List.of(energyPortalUser));

    var appointments = List.of(AppointmentTestUtil.builder().build());

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    var file = UploadedFileTestUtil.builder().build();

    given(fileService.findAll(
        termination.getId().toString(),
        FileUsageType.TERMINATION.getUsageType(),
        FileDocumentType.TERMINATION.name()
    ))
        .willReturn(List.of(file));

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

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .build();

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
        .willReturn(List.of(energyPortalUser));

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    given(teamQueryService.userHasAtLeastOneStaticRole(
        loggedInUser.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .willReturn(true);

    var firstFile = UploadedFileTestUtil.builder()
        .withName("file_a")
        .build();
    var secondFile = UploadedFileTestUtil.builder()
        .withName("file_B")
        .build();
    var thirdFile = UploadedFileTestUtil.builder()
        .withName("file_c")
        .build();

    given(fileService.findAll(
        termination.getId().toString(),
        FileUsageType.TERMINATION.getUsageType(),
        FileDocumentType.TERMINATION.name()
    ))
        .willReturn(List.of(secondFile, firstFile, thirdFile));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    var terminationFiles = resultingTerminationViewList.stream()
        .map(assetTimelineItemView -> {
          @SuppressWarnings("unchecked")
          var fileCast = (List<FileSummaryView>) assetTimelineItemView.assetTimelineModelProperties()
              .getModelProperties()
              .get("terminationFiles");
          return fileCast;
        })
        .flatMap(Collection::stream)
        .toList();

    assertThat(terminationFiles)
        .extracting(
            FileSummaryView::uploadedFileView,
            FileSummaryView::downloadUrl
        )
        .containsExactly(
            Tuple.tuple(
                UploadedFileView.from(firstFile),
                ReverseRouter.route(on(AppointmentTerminationFileController.class)
                    .download(
                        termination.getId(),
                        firstFile.getId()
                    ))
            ),
            Tuple.tuple(
                UploadedFileView.from(secondFile),
                ReverseRouter.route(on(AppointmentTerminationFileController.class)
                    .download(
                        termination.getId(),
                        secondFile.getId()
                    ))
            ),
            Tuple.tuple(
                UploadedFileView.from(thirdFile),
                ReverseRouter.route(on(AppointmentTerminationFileController.class)
                    .download(
                        termination.getId(),
                        thirdFile.getId()
                    ))
            )
        );
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

    given(teamQueryService.userHasAtLeastOneStaticRole(
        loggedInUser.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .willReturn(false);

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

    given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
        .willReturn(List.of(energyPortalUser));

    var file = UploadedFileTestUtil.builder().build();

    given(fileService.findAll(
        termination.getId().toString(),
        FileUsageType.TERMINATION.getUsageType(),
        FileDocumentType.TERMINATION.name()
    ))
        .willReturn(List.of(file));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(
        List.of(termination.getAppointment()));

    var resultingModelProperties = resultingTerminationViewList.get(0)
        .assetTimelineModelProperties()
        .getModelProperties();

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

    var file = UploadedFileTestUtil.builder().build();

    given(fileService.findAll(
        termination.getId().toString(),
        FileUsageType.TERMINATION.getUsageType(),
        FileDocumentType.TERMINATION.name()
    ))
        .willReturn(List.of(file));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(
        List.of(termination.getAppointment()));
    var resultingModelProperties = resultingTerminationViewList.get(
        0).assetTimelineModelProperties().getModelProperties();

    assertThat(resultingModelProperties)
        .containsExactly(
            entry("terminationDate", DateUtil.formatLongDate(termination.getTerminationDate()))
        );
  }

  @Test
  void getTimelineItemViews_whenWuaIdNotInEnergyPortal_thenThrow() {
    var wuaId = 1L;
    var termination = AppointmentTerminationTestUtil.builder().withCorrectedByWuaId(wuaId).build();
    var appointments = List.of(AppointmentTestUtil.builder().build());

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(List.of(termination));

     given(energyPortalUserService.findByWuaIds(Set.of(new WebUserAccountId(wuaId)), TerminationTimelineItemService.TERMINATED_BY_USER_PURPOSE))
         .willReturn(Collections.emptyList());

    var loggedInUser = ServiceUserDetailTestUtil.Builder().build();

    given(userDetailService.getUserDetail()).willReturn(loggedInUser);

    given(teamQueryService.userHasAtLeastOneStaticRole(
        loggedInUser.wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    ))
        .willReturn(true);

    var file = UploadedFileTestUtil.builder().build();

    given(fileService.findAll(
        termination.getId().toString(),
        FileUsageType.TERMINATION.getUsageType(),
        FileDocumentType.TERMINATION.name()
    ))
        .willReturn(List.of(file));

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    assertThat(resultingTerminationViewList)
        .extracting(
            assetTimelineItemView -> assetTimelineItemView.assetTimelineModelProperties()
                .getModelProperties().get("terminatedBy")

        )
        .containsExactly("Unknown");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getTimelineItemViews_whenNoTerminations(List<AppointmentTermination> nullOrEmptyTerminations) {

    var appointments = List.of(AppointmentTestUtil.builder().build());

    given(appointmentTerminationService.getTerminations(appointments))
        .willReturn(nullOrEmptyTerminations);

    var resultingTerminationViewList = terminationTimelineItemService.getTimelineItemViews(appointments);

    assertThat(resultingTerminationViewList).isEmpty();

    then(energyPortalUserService)
        .should(never())
        .findByWuaIds(anyCollection(), any(RequestPurpose.class));
  }
}