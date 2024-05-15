package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import com.amazonaws.util.StringInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.core.FileUploadRequest;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = UnlinkedFileController.class)
class UnlinkedFileControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private FileService fileService;

  @SecurityTest
  void upload_whenNotAuthenticated_verifyRedirectedToLogin() throws Exception {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);
    mockMvc.perform(multipart(ReverseRouter.route(on(UnlinkedFileController.class)
        .upload(null, FileDocumentType.CASE_NOTE.name())))
        .file(mockMultipartFile)
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void delete_whenNotAuthenticated_verifyUnauthenticated() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
        .delete(UUID.randomUUID().toString())))
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_whenNotAuthenticated_verifyRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
        .download(UUID.randomUUID().toString()))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void upload_verifyCalls() throws Exception {

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", (byte[]) null);

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    mockMvc.perform(multipart(ReverseRouter.route(on(UnlinkedFileController.class)
        .upload(null, FileDocumentType.CASE_NOTE.name())))
        .file(mockMultipartFile)
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Function<FileUploadRequest.Builder, FileUploadRequest>> actionCaptor =
        ArgumentCaptor.forClass(Function.class);

    verify(fileService).upload(actionCaptor.capture());

    var builder = FileUploadRequest.newBuilder()
        .withBucket("bucket")
        .withMaximumSize(DataSize.ofBytes(100))
        .withFileExtensions(Set.of("extension"));

    assertThat(actionCaptor.getValue().apply(builder))
        .extracting(FileUploadRequest::fileSource)
        .isEqualTo(FileSource.fromMultipartFile(mockMultipartFile));
  }

  @Test
  void delete_whenNoFileFound_thenNotFound() throws Exception {

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    when(fileService.find(fileUuid))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
        .delete(fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenInvalidUuid_thenNotFound() throws Exception {

    var fileId = "invalid uuid";

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
        .delete(fileId)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenUserDidNotUploadFile_thenNotFound() throws Exception {

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
        .delete(fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_whenFileHasUsages_thenNotFound() throws Exception {

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(UUID.randomUUID().toString())
        .withUsageType(UUID.randomUUID().toString())
        .withDocumentType(UUID.randomUUID().toString())
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
        .delete(fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

  @Test
  void delete_verifyCalls() throws Exception {

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    when(fileService.delete(uploadedFile))
        .thenReturn(FileDeleteResponse.success(fileUuid));

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
        .delete(fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(fileService).delete(uploadedFile);
  }

  @Test
  void download_verifyCalls() throws Exception {

    var fileUuid = UUID.randomUUID();

    var file = UploadedFileTestUtil.builder()
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(USER.wuaId().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    var streamContent = "abc";
    var inputStreamResource = new InputStreamResource(new StringInputStream(streamContent), "stream description");
    var response = ResponseEntity.ok(inputStreamResource);

    when(fileService.download(file))
        .thenAnswer(invocation -> response);

    var result = mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
        .download(fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    assertThat(result).isEqualTo(streamContent);
  }

  @Test
  void download_whenNoFileFound_thenNotFound() throws Exception {

    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    when(fileService.find(fileUuid))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
        .download(fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_whenInvalidUuid_thenNotFound() throws Exception {

    var fileId = "invalid uuid";

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
        .download(fileId)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_whenUserDidNotUploadFile_thenNotFound() throws Exception {
    var fileUuid = UUID.randomUUID();

    SamlAuthenticationUtil.Builder()
        .withUser(USER)
        .setSecurityContext();

    when(userDetailService.getUserDetail())
        .thenReturn(USER);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
        .download(fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_whenFileIdNotFound() throws Exception {
    var fileUuid = UUID.randomUUID();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(
            on(UnlinkedFileController.class).download(fileUuid.toString())))
            .with(user(USER)))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void download_whenFileUsageIdIsNotLinkedToNomination() throws Exception {
    var fileUuid = UUID.randomUUID();

    var file = UploadedFileTestUtil.builder()
        .withUsageId(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(get(ReverseRouter.route(on(UnlinkedFileController.class)
        .download(fileUuid.toString())))
        .with(user(USER)))
        .andExpect(status().isNotFound());

    verify(fileService, never()).download(any());
  }

  @Test
  void delete_whenFileHasDifferentUploader_thenNotFound() throws Exception {
    var fileUuid = UUID.randomUUID();

    var file = UploadedFileTestUtil.builder()
        .withUsageId(null)
        .withUsageType(null)
        .withDocumentType(null)
        .withUploadedBy(UUID.randomUUID().toString())
        .build();

    when(fileService.find(fileUuid))
        .thenReturn(Optional.of(file));

    mockMvc.perform(post(ReverseRouter.route(on(UnlinkedFileController.class)
        .delete(fileUuid.toString())))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isNotFound());

    verify(fileService, never()).delete(any());
  }

}