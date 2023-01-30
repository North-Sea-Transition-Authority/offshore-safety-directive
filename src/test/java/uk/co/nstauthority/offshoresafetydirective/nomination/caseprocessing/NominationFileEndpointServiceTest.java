package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.util.StringInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.co.nstauthority.offshoresafetydirective.file.FileDeleteResult;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadResult;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.NominationFileService;

@ExtendWith(MockitoExtension.class)
class NominationFileEndpointServiceTest {

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private NominationFileService nominationFileService;

  @InjectMocks
  private NominationFileEndpointService nominationFileEndpointService;

  @Test
  void handleUpload_verifyCall() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail);
    var multipartFile = mock(MultipartFile.class);
    var virtualFolder = VirtualFolder.CONFIRM_APPOINTMENTS;
    var statuses = EnumSet.of(NominationStatus.AWAITING_CONFIRMATION);
    var allowedExtensions = Set.of(".txt");
    var fileId = UUID.randomUUID();
    var fileName = "file name";
    var expectedResult = FileUploadResult.valid(fileId.toString(), fileName, multipartFile);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationFileService.processFileUpload(nominationDetail, virtualFolder, multipartFile, allowedExtensions))
        .thenReturn(expectedResult);

    var result = nominationFileEndpointService.handleUpload(nominationId, multipartFile, virtualFolder, statuses,
        allowedExtensions);

    assertThat(result).contains(expectedResult);
  }

  @Test
  void handleUpload_whenNoLatestNomination_verifyEmptyOptional() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail);
    var multipartFile = mock(MultipartFile.class);
    var virtualFolder = VirtualFolder.CONFIRM_APPOINTMENTS;
    var statuses = EnumSet.of(NominationStatus.AWAITING_CONFIRMATION);
    var allowedExtensions = Set.of(".txt");

    when(nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses))
        .thenReturn(Optional.empty());

    var result = nominationFileEndpointService.handleUpload(nominationId, multipartFile, virtualFolder, statuses,
        allowedExtensions);
    assertThat(result).isEmpty();
  }

  @Test
  void handleDelete_verifyCall() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail);
    var statuses = EnumSet.of(NominationStatus.AWAITING_CONFIRMATION);
    var fileId = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileId);
    var expectedResult = FileDeleteResult.success(fileId.toString());

    when(nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationFileService.deleteFile(nominationDetail, uploadedFileId))
        .thenReturn(expectedResult);

    var result = nominationFileEndpointService.handleDelete(nominationId, uploadedFileId, statuses);

    assertThat(result).contains(expectedResult);
  }

  @Test
  void handleDelete_whenNoLatestNomination_verifyEmptyOptional() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail);
    var statuses = EnumSet.of(NominationStatus.AWAITING_CONFIRMATION);
    var uploadedFileId = new UploadedFileId(UUID.randomUUID());

    when(nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses))
        .thenReturn(Optional.empty());

    var result = nominationFileEndpointService.handleDelete(nominationId, uploadedFileId, statuses);
    assertThat(result).isEmpty();
  }

  @Test
  void handleDownload_verifyCall() throws UnsupportedEncodingException {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail);
    var statuses = EnumSet.of(NominationStatus.AWAITING_CONFIRMATION);
    var fileId = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileId);

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var expectedResult = ResponseEntity.ok(inputStreamResource);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses))
        .thenReturn(Optional.of(nominationDetail));

    when(nominationFileService.handleDownload(nominationDetail, uploadedFileId))
        .thenReturn(expectedResult);

    var result = nominationFileEndpointService.handleDownload(nominationId, uploadedFileId, statuses);

    assertThat(result).contains(expectedResult);
  }

  @Test
  void handleDownload_whenNoLatestNomination_verifyEmptyOptional() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationId = new NominationId(nominationDetail);
    var statuses = EnumSet.of(NominationStatus.AWAITING_CONFIRMATION);
    var fileId = UUID.randomUUID();
    var uploadedFileId = new UploadedFileId(fileId);

    when(nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, statuses))
        .thenReturn(Optional.empty());

    var result = nominationFileEndpointService.handleDownload(nominationId, uploadedFileId, statuses);
    assertThat(result).isEmpty();
  }

  private String getStatusString(Collection<NominationStatus> statuses) {
    return statuses.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

}