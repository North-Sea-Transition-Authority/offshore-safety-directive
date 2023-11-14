package uk.co.nstauthority.offshoresafetydirective.fds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.CreateIndustryTeamFormTestUtil;

class FormErrorSummaryServiceTest {

  private final FormErrorSummaryService formErrorSummaryService = new FormErrorSummaryService();

  @Test
  void getErrorItems_withEmptyBindingResults() {
    List<ErrorItem> errorItemList = formErrorSummaryService.getErrorItems(ReverseRouter.emptyBindingResult());
    assertThat(errorItemList).isEmpty();
  }

  @Test
  void getErrorItems_withBindingResults() {
    var bindingResult = new BeanPropertyBindingResult(CreateIndustryTeamFormTestUtil.builder().build(), "form");
    bindingResult.addError(new FieldError("Error", "ErrorField", "Error message"));

    List<ErrorItem> errorItemList = formErrorSummaryService.getErrorItems(bindingResult);
    assertThat(errorItemList)
        .extracting(ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(tuple("ErrorField", "Error message"));
  }
}