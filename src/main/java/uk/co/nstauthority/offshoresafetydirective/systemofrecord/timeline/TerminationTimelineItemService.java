package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.offshoresafetydirective.authentication.InvalidAuthenticationException;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTermination;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationFileController;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.termination.AppointmentTerminationService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
class TerminationTimelineItemService {

  static final RequestPurpose TERMINATED_BY_USER_PURPOSE = new RequestPurpose("User who terminated the appointment");

  private final EnergyPortalUserService energyPortalUserService;
  private final UserDetailService userDetailService;
  private final AppointmentTerminationService appointmentTerminationService;
  private final FileService fileService;
  private final TeamQueryService teamQueryService;

  @Autowired
  TerminationTimelineItemService(EnergyPortalUserService energyPortalUserService,
                                 AppointmentTerminationService appointmentTerminationService,
                                 UserDetailService userDetailService,
                                 FileService fileService, TeamQueryService teamQueryService) {
    this.energyPortalUserService = energyPortalUserService;
    this.appointmentTerminationService = appointmentTerminationService;
    this.userDetailService = userDetailService;
    this.fileService = fileService;
    this.teamQueryService = teamQueryService;
  }

  public List<AssetTimelineItemView> getTimelineItemViews(List<Appointment> appointments) {

    var terminations = appointmentTerminationService.getTerminations(appointments);
    var users = getUsers(terminations);

    return terminations.stream()
        .map(termination -> {
          var terminatedByUserName = Optional.ofNullable(users.get(termination.getTerminatedByWuaId()))
              .map(EnergyPortalUserDto::displayName)
              .orElse("Unknown");

          var terminationFiles = fileService.findAll(
                  termination.getId().toString(),
                  FileUsageType.TERMINATION.getUsageType(),
                  FileDocumentType.TERMINATION.name()
              )
              .stream()
              .map(uploadedFile -> new FileSummaryView(
                  UploadedFileView.from(uploadedFile),
                  ReverseRouter.route(on(AppointmentTerminationFileController.class)
                      .download(
                          termination.getId(),
                          uploadedFile.getId()
                      ))))
              .sorted(Comparator.comparing(view -> view.uploadedFileView().fileName(), String::compareToIgnoreCase))
              .toList();

          return convertToTimelineItemView(termination, terminatedByUserName, terminationFiles);
        })
        .toList();

  }

  private AssetTimelineItemView convertToTimelineItemView(AppointmentTermination termination,
                                                          String terminatedByUserName,
                                                          List<FileSummaryView> files) {

    var modelProperties = new AssetTimelineModelProperties()
        .addProperty("terminationDate", DateUtil.formatLongDate(termination.getTerminationDate()));

    if (isMemberOfRegulatorTeam()) {
      modelProperties.addProperty("reasonForTermination", termination.getReasonForTermination())
          .addProperty("terminatedBy", terminatedByUserName)
          .addProperty("terminationFiles", files);
    }

    return new AssetTimelineItemView(
        TimelineEventType.TERMINATION,
        "Termination of appointment",
        modelProperties,
        termination.getCreatedTimestamp(),
        termination.getTerminationDate()
    );
  }

  private Map<Long, EnergyPortalUserDto> getUsers(List<AppointmentTermination> terminations) {

    var wuaIds = terminations.stream()
        .map(AppointmentTermination::getTerminatedByWuaId)
        .map(WebUserAccountId::new)
        .collect(Collectors.toSet());

    return energyPortalUserService.findByWuaIds(wuaIds, TERMINATED_BY_USER_PURPOSE)
        .stream()
        .collect(Collectors.toMap(EnergyPortalUserDto::webUserAccountId, Function.identity()));
  }

  private boolean isMemberOfRegulatorTeam() {

    Optional<ServiceUserDetail> loggedInUser;

    try {
      loggedInUser = Optional.of(userDetailService.getUserDetail());
    } catch (InvalidAuthenticationException exception) {
      return false;
    }

    return teamQueryService.userHasAtLeastOneStaticRole(
        loggedInUser.get().wuaId(),
        TeamType.REGULATOR,
        new LinkedHashSet<>(TeamType.REGULATOR.getAllowedRoles())
    );
  }
}
