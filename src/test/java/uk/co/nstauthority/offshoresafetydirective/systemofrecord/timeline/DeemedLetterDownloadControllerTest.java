package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = DeemedLetterDownloadController.class)
class DeemedLetterDownloadControllerTest extends AbstractControllerTest {

  @Test
  void getAsUploadedFileView() throws IOException {

    var expectedResource = new ClassPathResource(
        DeemedLetterDownloadController.FILE_PATH.toString(),
        DeemedLetterDownloadController.class.getClassLoader()
    );

    var expectedSize = FileUploadLibraryUtils.formatSize(expectedResource.getInputStream().available());

    var uploadedFileView = DeemedLetterDownloadController.getAsUploadedFileView();
    assertThat(uploadedFileView)
        .extracting(
            UploadedFileView::fileId,
            UploadedFileView::fileName,
            UploadedFileView::fileSize,
            UploadedFileView::fileDescription,
            UploadedFileView::fileUploadedTime
        ).containsExactly(
            "note-concerning-deemed-appointments",
            "General note on deemed appointments.pdf",
            expectedSize,
            null,
            null
        );
  }

  @SecurityTest
  void download_whenUnauthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DeemedLetterDownloadController.class).download())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void download_whenAuthenticated_thenOk() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(DeemedLetterDownloadController.class).download()))
            .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isOk());
  }

  @Test
  void download() throws Exception {
    var expectedResource = new ClassPathResource(
        DeemedLetterDownloadController.FILE_PATH.toString(),
        DeemedLetterDownloadController.class.getClassLoader()
    );

    mockMvc.perform(get(ReverseRouter.route(on(DeemedLetterDownloadController.class).download()))
            .with(user(ServiceUserDetailTestUtil.Builder().build())))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(content().bytes(expectedResource.getInputStream().readAllBytes()))
        .andExpect(header().string(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"%s\"".formatted("General note on deemed appointments.pdf")
        ));
  }
}