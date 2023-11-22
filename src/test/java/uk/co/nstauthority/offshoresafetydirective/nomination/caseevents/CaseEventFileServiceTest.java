package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.file.FileDocumentType;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.FileUsageType;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseEventFileServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private FileService fileService;

  @Mock
  private UserDetailService userDetailService;

  @InjectMocks
  private CaseEventFileService caseEventFileService;

  @Test
  void getFileViewMapFromCaseEvents_whenMultipleLinkedFiles_thenAssertResult() {
    var caseEvent = CaseEventTestUtil.builder().build();
    var firstFile = UploadedFileTestUtil.builder()
        .withName("File a")
        .withUsageId(caseEvent.getUuid().toString())
        .build();
    var secondFile = UploadedFileTestUtil.builder()
        .withName("File B") // Ensure sort is not case-sensitive
        .withUsageId(caseEvent.getUuid().toString())
        .build();
    var thirdFile = UploadedFileTestUtil.builder()
        .withName("File c")
        .withUsageId(caseEvent.getUuid().toString())
        .build();

    when(fileService.findAllByUsageIdsWithUsageType(
        List.of(caseEvent.getUuid().toString()),
        FileUsageType.CASE_EVENT.getUsageType()
    ))
        .thenReturn(List.of(firstFile, thirdFile, secondFile));

    var result = caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent));

    assertThat(result)
        .containsOnlyKeys(caseEvent)
        .extractingByKey(caseEvent)
        .asInstanceOf(InstanceOfAssertFactories.list(FileSummaryView.class))
        .extracting(
            fileSummaryView -> fileSummaryView.uploadedFileView().fileName()
        )
        .containsExactly(
            firstFile.getName(),
            secondFile.getName(),
            thirdFile.getName()
        );
  }

  @Test
  void getFileViewMapFromCaseEvents_whenNoLinkedFiles_thenAssertResult() {
    var caseEvent = CaseEventTestUtil.builder().build();

    when(fileService.findAllByUsageIdsWithUsageType(
        List.of(caseEvent.getUuid().toString()),
        FileUsageType.CASE_EVENT.getUsageType()
    ))
        .thenReturn(List.of());

    var result = caseEventFileService.getFileViewMapFromCaseEvents(List.of(caseEvent));

    assertThat(result).isEmpty();
  }

  @Test
  void linkFilesToCaseEvent_whenValidFile_thenVerifyFileIsLinked() {

    var caseEvent = CaseEventTestUtil.builder().build();

    var formDescription = "description";
    var formFileId = UUID.randomUUID();

    var uploadedFileForm = new UploadedFileForm();
    uploadedFileForm.setFileDescription(formDescription);
    uploadedFileForm.setFileId(formFileId);

    var file = UploadedFileTestUtil.builder()
        .withId(formFileId)
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.findAll(Set.of(formFileId)))
        .thenReturn(List.of(file));

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    caseEventFileService.linkFilesToCaseEvent(
        caseEvent,
        List.of(uploadedFileForm),
        FileDocumentType.CASE_NOTE
    );

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageCaptor = ArgumentCaptor.forClass(Function.class);
    verify(fileService).updateUsageAndDescription(eq(file), fileUsageCaptor.capture(), eq(formDescription));

    fileUsageCaptor.getAllValues()
        .forEach(builderFileUsageFunction -> {
          var builder = FileUsage.newBuilder();
          assertThat(builderFileUsageFunction.apply(builder))
              .extracting(
                  FileUsage::usageId,
                  FileUsage::usageType,
                  FileUsage::documentType
              )
              .containsExactly(
                  caseEvent.getUuid().toString(),
                  FileUsageType.CASE_EVENT.getUsageType(),
                  FileDocumentType.CASE_NOTE.name()
              );
        });
  }

  @Test
  void linkFilesToCaseEvent_whenFileWasUploadedByAnotherUser_thenError() {

    var caseEvent = CaseEventTestUtil.builder().build();

    var firstFormDescription = "first description";
    var secondFormDescription = "second description";
    var firstFormFileId = UUID.randomUUID();
    var secondFormFileId = UUID.randomUUID();

    var firstUploadedFileForm = new UploadedFileForm();
    firstUploadedFileForm.setFileDescription(firstFormDescription);
    firstUploadedFileForm.setFileId(firstFormFileId);

    var secondUploadedFileForm = new UploadedFileForm();
    secondUploadedFileForm.setFileDescription(secondFormDescription);
    secondUploadedFileForm.setFileId(secondFormFileId);

    var firstFile = UploadedFileTestUtil.builder()
        .withId(firstFormFileId)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    var secondFile = UploadedFileTestUtil.builder()
        .withId(secondFormFileId)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    when(fileService.findAll(Set.of(firstFormFileId, secondFormFileId)))
        .thenReturn(List.of(firstFile, secondFile));

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    assertThatThrownBy(() -> caseEventFileService.linkFilesToCaseEvent(
        caseEvent,
        List.of(firstUploadedFileForm, secondUploadedFileForm),
        FileDocumentType.CASE_NOTE
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Not all files [%s] are allowed to be linked to the case event [%s] by user [%d]".formatted(
            "%s,%s".formatted(firstFormFileId, secondFormFileId),
            caseEvent.getUuid(),
            USER.wuaId()
        ));

  }

  @Test
  void linkFilesToCaseEvent_whenNoMatchingFiles_thenError() {

    var caseEvent = CaseEventTestUtil.builder().build();

    var firstFormDescription = "first description";
    var secondFormDescription = "second description";
    var firstFormFileId = UUID.randomUUID();
    var secondFormFileId = UUID.randomUUID();

    var firstUploadedFileForm = new UploadedFileForm();
    firstUploadedFileForm.setFileDescription(firstFormDescription);
    firstUploadedFileForm.setFileId(firstFormFileId);

    var secondUploadedFileForm = new UploadedFileForm();
    secondUploadedFileForm.setFileDescription(secondFormDescription);
    secondUploadedFileForm.setFileId(secondFormFileId);

    when(fileService.findAll(Set.of(firstFormFileId, secondFormFileId)))
        .thenReturn(List.of());

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    assertThatThrownBy(() -> caseEventFileService.linkFilesToCaseEvent(
        caseEvent,
        List.of(firstUploadedFileForm, secondUploadedFileForm),
        FileDocumentType.CASE_NOTE
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Not all files [%s] are allowed to be linked to the case event [%s] by user [%d]".formatted(
            "%s,%s".formatted(firstFormFileId, secondFormFileId),
            caseEvent.getUuid(),
            USER.wuaId()
        ));

  }

}