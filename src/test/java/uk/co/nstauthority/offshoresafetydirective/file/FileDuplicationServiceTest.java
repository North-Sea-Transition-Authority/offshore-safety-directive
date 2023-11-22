package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class FileDuplicationServiceTest {

  @Mock
  private FileService fileService;

  @InjectMocks
  private FileDuplicationService fileDuplicationService;

  @Test
  void duplicateFiles() {
    var sourceNominationDetailId = UUID.randomUUID();
    var targetNominationDetailId = UUID.randomUUID();
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(sourceNominationDetailId)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(targetNominationDetailId)
        .build();

    var uploadedFileA = UploadedFileTestUtil.newBuilder().build();
    var uploadedFileB = UploadedFileTestUtil.newBuilder().build();

    when(fileService.findAll(
        sourceNominationDetailId.toString(),
        FileUsageType.NOMINATION_DETAIL.getUsageType()
    ))
        .thenReturn(List.of(uploadedFileA, uploadedFileB));

    fileDuplicationService.duplicateFiles(sourceNominationDetail, targetNominationDetail);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageFunctionCaptorForFileA =
        ArgumentCaptor.forClass(Function.class);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageFunctionCaptorForFileB =
        ArgumentCaptor.forClass(Function.class);

    verify(fileService).copy(eq(uploadedFileA), fileUsageFunctionCaptorForFileA.capture());
    verify(fileService).copy(eq(uploadedFileB), fileUsageFunctionCaptorForFileB.capture());

    assertThat(fileUsageFunctionCaptorForFileA.getValue().apply(FileUsage.newBuilder()))
        .extracting(
            FileUsage::usageId,
            FileUsage::usageType,
            FileUsage::documentType
        )
        .containsExactly(
            targetNominationDetail.getId().toString(),
            uploadedFileA.getUsageType(),
            uploadedFileA.getDocumentType()
        );

    assertThat(fileUsageFunctionCaptorForFileB.getValue().apply(FileUsage.newBuilder()))
        .extracting(
            FileUsage::usageId,
            FileUsage::usageType,
            FileUsage::documentType
        )
        .containsExactly(
            targetNominationDetail.getId().toString(),
            uploadedFileB.getUsageType(),
            uploadedFileB.getDocumentType()
        );

  }
}