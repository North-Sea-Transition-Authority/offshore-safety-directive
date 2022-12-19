package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.time.Clock;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;

@Service
public class CaseEventService {

  private final CaseEventRepository caseEventRepository;
  private final UserDetailService userDetailService;
  private final Clock clock;

  @Autowired
  public CaseEventService(CaseEventRepository caseEventRepository,
                          UserDetailService userDetailService,
                          Clock clock) {
    this.caseEventRepository = caseEventRepository;
    this.userDetailService = userDetailService;
    this.clock = clock;
  }

  @Transactional
  public void createCompletedQaChecksEvent(NominationDetail nominationDetail, @Nullable String comment) {
    var caseEvent = new CaseEvent();
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);
    caseEvent.setCaseEventType(CaseEventType.QA_CHECKS);
    caseEvent.setComment(comment);
    caseEvent.setCreatedBy(userDetailService.getUserDetail().wuaId());
    caseEvent.setCreatedInstant(clock.instant());
    caseEvent.setNomination(nominationDetail.getNomination());
    caseEvent.setNominationVersion(nominationDetailDto.version());
    caseEventRepository.save(caseEvent);
  }
}
