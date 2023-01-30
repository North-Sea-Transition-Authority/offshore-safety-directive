package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;

@Service
public class CaseEventQueryService {

  private final CaseEventRepository caseEventRepository;

  @Autowired
  CaseEventQueryService(CaseEventRepository caseEventRepository) {
    this.caseEventRepository = caseEventRepository;
  }

  public Optional<LocalDate> getDecisionDateForNominationDetail(NominationDetail nominationDetail) {
    var dto = NominationDetailDto.fromNominationDetail(nominationDetail);
    return caseEventRepository.findFirstByCaseEventTypeInAndNominationAndNominationVersion(
            EnumSet.of(CaseEventType.NO_OBJECTION_DECISION, CaseEventType.OBJECTION_DECISION),
            nominationDetail.getNomination(),
            dto.version())
        .map(caseEvent -> LocalDate.ofInstant(caseEvent.getCreatedInstant(), ZoneId.systemDefault()));
  }
}
