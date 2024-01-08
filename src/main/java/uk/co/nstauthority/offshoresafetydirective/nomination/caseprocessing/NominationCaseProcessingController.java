package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.primitives.Ints;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.CanViewNominationPostSubmission;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;

@Controller
@RequestMapping("/nomination/{nominationId}/review")
@CanViewNominationPostSubmission
public class NominationCaseProcessingController {

  public static final String VERSION_FORM_NAME = "nominationVersionForm";

  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationCaseProcessingController(
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      NominationDetailService nominationDetailService) {
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView renderCaseProcessing(@PathVariable("nominationId") NominationId nominationId,
                                           @Nullable @RequestParam(value = "version", required = false) String version) {

    var parsedVersion = Optional.ofNullable(version)
        .map(Ints::tryParse)
        .orElse(null);

    NominationDetail nominationDetail = getApplicableNominationDetail(nominationId, parsedVersion);

    var foundVersion = NominationDetailDto.fromNominationDetail(nominationDetail).version();

    var form = new CaseProcessingVersionForm();
    form.setNominationDetailVersion(Objects.toString(foundVersion, null));

    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withCaseProcessingVersionForm(form)
        .build();

    return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        nominationDetail,
        modelAndViewDto
    );
  }

  private NominationDetail getApplicableNominationDetail(NominationId nominationId, @Nullable Integer version) {

    var nominationDetailVersion = Optional.ofNullable(version);
    return nominationDetailVersion
        .map(versionNumber -> getVersionedPostSubmissionNominationDetail(nominationId, versionNumber))
        .orElseGet(() -> getLatestPostSubmissionNominationDetail(nominationId));
  }

  @PostMapping
  public ModelAndView changeCaseProcessingVersion(@PathVariable("nominationId") NominationId nominationId,
                                                  @Nullable @ModelAttribute(VERSION_FORM_NAME) CaseProcessingVersionForm form) {

    var versionToRedirect = Optional.ofNullable(Objects.requireNonNull(form).getNominationDetailVersion())
        .map(Objects::toString)
        .orElse(null);
    return ReverseRouter.redirect(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(nominationId, versionToRedirect));
  }

  private NominationDetail getVersionedPostSubmissionNominationDetail(NominationId nominationId, Integer version) {
    return nominationDetailService.getVersionedNominationDetailWithStatuses(
        nominationId,
        version,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ).orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "No NominationDetail in a post submission status found for Nomination [%s] with version [%d]".formatted(
            nominationId,
            version
        )
    ));
  }

  private NominationDetail getLatestPostSubmissionNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ).orElseThrow(() -> new OsdEntityNotFoundException(
        "No NominationDetail found with Nomination ID [%s] in a post submission status".formatted(
            nominationId.id()
        )
    ));
  }

}
