package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationPortalReferenceController {

  public static final String PEARS_FORM_NAME = "pearsPortalReferenceForm";
  public static final String WONS_FORM_NAME = "wonsPortalReferenceForm";

  private final NominationPortalReferencePersistenceService nominationPortalReferencePersistenceService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationPortalReferenceController(
      NominationPortalReferencePersistenceService nominationPortalReferencePersistenceService,
      NominationDetailService nominationDetailService
  ) {
    this.nominationPortalReferencePersistenceService = nominationPortalReferencePersistenceService;
    this.nominationDetailService = nominationDetailService;
  }

  @PostMapping(params = CaseProcessingAction.PEARS_REFERENCES)
  public ModelAndView updatePearsReferences(@PathVariable("nominationId") NominationId nominationId,
                                            @RequestParam("pears-references") Boolean slideoutOpen,
                                            // Used for ReverseRouter to call correct route
                                            @Nullable
                                            @RequestParam(CaseProcessingAction.PEARS_REFERENCES) String postButtonName,
                                            @Nullable @ModelAttribute(PEARS_FORM_NAME) PearsPortalReferenceForm form,
                                            @Nullable BindingResult bindingResult,
                                            @Nullable RedirectAttributes redirectAttributes) {

    return processAndReturn(PortalReferenceType.PEARS, nominationId, form, redirectAttributes);
  }

  @PostMapping(params = CaseProcessingAction.WONS_REFERENCES)
  public ModelAndView updateWonsReferences(@PathVariable("nominationId") NominationId nominationId,
                                           @RequestParam("wons-references") Boolean slideoutOpen,
                                           // Used for ReverseRouter to call correct route
                                           @Nullable
                                           @RequestParam(CaseProcessingAction.WONS_REFERENCES) String postButtonName,
                                           @Nullable @ModelAttribute(WONS_FORM_NAME) WonsPortalReferenceForm form,
                                           @Nullable BindingResult bindingResult,
                                           @Nullable RedirectAttributes redirectAttributes) {

    return processAndReturn(PortalReferenceType.WONS, nominationId, form, redirectAttributes);
  }

  private ModelAndView processAndReturn(PortalReferenceType portalReferenceType, NominationId nominationId,
                                        NominationPortalReferenceForm form,
                                        @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(nominationId, EnumSet.of(
            NominationStatus.SUBMITTED))
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find NominationDetail with status [%s] for Nomination [%s]".formatted(
                NominationStatus.SUBMITTED.name(),
                nominationId.id()
            )
        ));

    nominationPortalReferencePersistenceService.updatePortalReferences(nominationDetail.getNomination(),
        portalReferenceType, Objects.requireNonNull(form).getReferences().getInputValue());

    if (redirectAttributes != null) {
      var notificationBanner = NotificationBanner.builder()
          .withBannerType(NotificationBannerType.SUCCESS)
          .withTitle("Updated references")
          .withHeading(
              "%s references for %s have been updated"
                  .formatted(portalReferenceType.name(), nominationDetail.getNomination().getReference())
          )
          .build();

      NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    }

    return ReverseRouter.redirect(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId));
  }

}
