package uk.co.nstauthority.offshoresafetydirective.nomination.duplication;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.file.FileDuplicationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominationDuplicationService {

  private final List<DuplicatableNominationService> duplicatableNominationServices;
  private final FileDuplicationService fileDuplicationService;

  @Autowired
  public NominationDuplicationService(List<DuplicatableNominationService> duplicatableNominationServices,
                                      FileDuplicationService fileDuplicationService) {
    this.duplicatableNominationServices = duplicatableNominationServices;
    this.fileDuplicationService = fileDuplicationService;
  }

  @Transactional
  public void duplicateNominationDetailSections(NominationDetail sourceNominationDetail,
                                                NominationDetail targetNominationDetail) {
    duplicatableNominationServices.forEach(service ->
        service.duplicate(sourceNominationDetail, targetNominationDetail));

    fileDuplicationService.duplicateFiles(sourceNominationDetail, targetNominationDetail);
  }

}
