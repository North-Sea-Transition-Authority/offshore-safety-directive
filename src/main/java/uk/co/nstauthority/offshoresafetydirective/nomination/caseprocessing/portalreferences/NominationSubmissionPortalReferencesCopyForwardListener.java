package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.offshoresafetydirective.logging.LoggerUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationSubmittedEvent;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationDto;

@Component
class NominationSubmissionPortalReferencesCopyForwardListener {

  static final Set<PortalReferenceType> COPY_FORWARD_PORTAL_TYPES = EnumSet.of(
      PortalReferenceType.PEARS,
      PortalReferenceType.WONS
  );

  private final RelatedInformationAccessService relatedInformationAccessService;
  private final NominationPortalReferenceRepository nominationPortalReferenceRepository;
  private final NominationPortalReferencePersistenceService nominationPortalReferencePersistenceService;

  @Autowired
  NominationSubmissionPortalReferencesCopyForwardListener(
      RelatedInformationAccessService relatedInformationAccessService,
      NominationPortalReferenceRepository nominationPortalReferenceRepository,
      NominationPortalReferencePersistenceService nominationPortalReferencePersistenceService) {
    this.relatedInformationAccessService = relatedInformationAccessService;
    this.nominationPortalReferenceRepository = nominationPortalReferenceRepository;
    this.nominationPortalReferencePersistenceService = nominationPortalReferencePersistenceService;
  }

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleSubmission(NominationSubmittedEvent event) {

    var dto = NominationDetailDto.fromNominationDetail(event.getNominationDetail());

    if (!dto.version().equals(1)) {
      return;
    }

    var relatedInformationDto = relatedInformationAccessService.getRelatedInformationDto(event.getNominationDetail())
        .orElseThrow(() -> new IllegalStateException(
            "No related information found for nomination detail %s".formatted(dto.nominationDetailId().id())));

    var referencesToSave = new ArrayList<NominationPortalReference>();

    COPY_FORWARD_PORTAL_TYPES.forEach(portalReferenceType -> {

      LoggerUtil.info("Copy-forwarding %s references for nomination %s".formatted(
          portalReferenceType.name(),
          event.getNominationDetail().getNomination().getId())
      );

      var portalReference = nominationPortalReferencePersistenceService.createPortalReference(
          event.getNominationDetail().getNomination(),
          portalReferenceType
      );

      portalReference.setPortalReferences(getRefsForPortalReference(relatedInformationDto, portalReferenceType));
      referencesToSave.add(portalReference);
    });

    nominationPortalReferenceRepository.saveAll(referencesToSave);
  }

  private String getRefsForPortalReference(RelatedInformationDto relatedInformationDto,
                                           PortalReferenceType portalReferenceType) {
    return switch (portalReferenceType) {
      case PEARS -> relatedInformationDto.relatedToPearsApplications().applications();
      case WONS -> relatedInformationDto.relatedToWonsApplications().applications();
    };
  }

}
