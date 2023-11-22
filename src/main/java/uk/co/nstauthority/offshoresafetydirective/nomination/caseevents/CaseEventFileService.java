package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

@Service
public class CaseEventFileService {

  private final FileService fileService;
  private final UserDetailService userDetailService;

  @Autowired
  public CaseEventFileService(FileService fileService,
                              UserDetailService userDetailService) {
    this.fileService = fileService;
    this.userDetailService = userDetailService;
  }

  @Transactional
  public void linkFilesToCaseEvent(CaseEvent caseEvent, Collection<UploadedFileForm> uploadedFileForms,
                                   FileDocumentType fileDocumentType) {

    Map<UUID, UploadedFileForm> fileIdAndFileFormMap = uploadedFileForms.stream()
        .collect(Collectors.toMap(UploadedFileForm::getFileId, Function.identity()));

    var user = userDetailService.getUserDetail();

    var files = fileService.findAll(fileIdAndFileFormMap.keySet());

    var filesToUpdate = files.stream()
        .filter(uploadedFile -> Objects.equals(uploadedFile.getUploadedBy(), user.wuaId().toString()))
        .toList();

    if (filesToUpdate.size() != files.size() || files.isEmpty()) {

      var filesAsString = uploadedFileForms.stream()
          .map(UploadedFileForm::getFileId)
          .map(UUID::toString)
          .collect(Collectors.joining(","));

      throw new IllegalStateException(
          "Not all files [%s] are allowed to be linked to the case event [%s] by user [%d]".formatted(
              filesAsString,
              caseEvent.getUuid(),
              user.wuaId()
          ));
    }

    for (UploadedFile file : files) {
      fileService.updateUsageAndDescription(
          file,
          builder -> builder
              .withUsageId(caseEvent.getUuid().toString())
              .withUsageType(FileUsageType.CASE_EVENT.getUsageType())
              .withDocumentType(fileDocumentType.getDocumentType())
              .build(),
          fileIdAndFileFormMap.get(file.getId()).getFileDescription()
      );
    }
  }

  public Map<CaseEvent, List<FileSummaryView>> getFileViewMapFromCaseEvents(Collection<CaseEvent> caseEvents) {
    Map<String, CaseEvent> caseEventIdMap = caseEvents.stream()
        .collect(Collectors.toMap(
            caseEvent -> caseEvent.getUuid().toString(),
            Function.identity()
        ));

    var caseEventIds = caseEvents.stream()
        .map(caseEvent -> caseEvent.getUuid().toString())
        .toList();

    var files = fileService.findAllByUsageIdsWithUsageType(
        caseEventIds,
        FileUsageType.CASE_EVENT.getUsageType()
    );

    Map<String, List<UploadedFile>> caseEventReferenceAndLinkedFileMap = files.stream()
        .collect(Collectors.groupingBy(UploadedFile::getUsageId));

    return caseEventReferenceAndLinkedFileMap.entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> caseEventIdMap.get(entry.getKey()),
            entry -> entry.getValue()
                .stream()
                .map(uploadedFile -> createFileSummaryView(
                    caseEventIdMap.get(entry.getKey()),
                    uploadedFile
                ))
                .sorted(Comparator.comparing(summaryView -> summaryView.uploadedFileView().fileName().toLowerCase()))
                .toList()
        ));

  }

  private FileSummaryView createFileSummaryView(
      CaseEvent caseEvent,
      UploadedFile file
  ) {
    var uploadedFileView = new UploadedFileView(
        file.getId().toString(),
        file.getName(),
        String.valueOf(file.getContentLength()),
        file.getDescription(),
        file.getUploadedAt()
    );
    return new FileSummaryView(
        uploadedFileView,
        ReverseRouter.route(on(CaseEventFileDownloadController.class).download(
            new NominationId(caseEvent.getNomination().getId()),
            new CaseEventId(caseEvent.getUuid()),
            new UploadedFileId(file.getId())
        ))
    );
  }

}
