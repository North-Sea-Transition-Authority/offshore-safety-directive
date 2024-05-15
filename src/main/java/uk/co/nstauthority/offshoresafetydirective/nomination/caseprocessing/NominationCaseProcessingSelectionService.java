package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;

@Service
class NominationCaseProcessingSelectionService {

  private final NominationDetailService nominationDetailService;

  NominationCaseProcessingSelectionService(NominationDetailService nominationDetailService) {
    this.nominationDetailService = nominationDetailService;
  }

  public Map<String, String> getSelectionOptions(Nomination nomination) {

    var sortedResults = nominationDetailService.getPostSubmissionNominationDetailDtos(nomination)
        .stream()
        .sorted(Comparator.comparing(NominationDetailDto::version))
        .filter(dto -> Objects.nonNull(dto.version()))
        .toList();

    LinkedHashMap<String, String> selectionMap = new LinkedHashMap<>();

    for (var selectionIndex = sortedResults.size() - 1; selectionIndex >= 0; selectionIndex--) {
      selectionMap.put(
          sortedResults.get(selectionIndex).version().toString(),
          "(%s) Submitted: %s".formatted(
              selectionIndex + 1,
              DateUtil.formatShortDate(sortedResults.get(selectionIndex).submittedInstant())
          )
      );
    }

    return selectionMap;
  }
}
