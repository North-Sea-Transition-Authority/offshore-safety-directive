package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class RelatedInformationFormTestUtil {

  RelatedInformationFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Boolean relatedToAnyFields = true;

    private List<String> fields = new ArrayList<>();

    private String fieldSelector;

    private Boolean relatedToAnyLicenceApplications = true;

    private String relatedLicenceApplications = "LICENCE/123";

    private Boolean relatedToAnyWellApplications = true;

    private String relatedWellApplications = "WELL/123";

    private boolean fieldsListInitialised = false;

    Builder withRelatedToAnyFields(Boolean isRelatedToAnyFields) {
      this.relatedToAnyFields = isRelatedToAnyFields;
      return this;
    }

    Builder withRelatedToAnyFields(String isRelatedToAnyFields) {
      this.relatedToAnyFields = BooleanUtils.toBooleanObject(isRelatedToAnyFields);
      return this;
    }

    Builder withField(int fieldId) {
      this.fields.add(String.valueOf(fieldId));
      fieldsListInitialised = true;
      return this;
    }

    Builder withFields(List<String> fields) {
      this.fields = fields;
      fieldsListInitialised = true;
      return this;
    }

    Builder withRelatedToLicenceApplications(Boolean isRelatedToLicenceApplications) {
      this.relatedToAnyLicenceApplications = isRelatedToLicenceApplications;
      return this;
    }

    Builder withRelatedToLicenceApplications(String isRelatedToLicenceApplications) {
      this.relatedToAnyLicenceApplications = BooleanUtils.toBooleanObject(isRelatedToLicenceApplications);
      return this;
    }


    Builder withRelatedLicenceApplications(String relatedLicenceApplications) {
      this.relatedLicenceApplications = relatedLicenceApplications;
      return this;
    }

    Builder withRelatedToWellApplications(Boolean isRelatedToWellApplications) {
      this.relatedToAnyWellApplications = isRelatedToWellApplications;
      return this;
    }

    Builder withRelatedToWellApplications(String isRelatedToWellApplications) {
      this.relatedToAnyWellApplications = BooleanUtils.toBooleanObject(isRelatedToWellApplications);
      return this;
    }

    Builder withRelatedWellApplications(String relatedWellApplications) {
      this.relatedWellApplications = relatedWellApplications;
      return this;
    }

    RelatedInformationForm build() {
      var form = new RelatedInformationForm();
      form.setRelatedToAnyFields(String.valueOf(relatedToAnyFields));

      if (!fieldsListInitialised) {
        fields.add("100");
      }

      form.setFields(fields.stream().map(Objects::toString).toList());
      form.setRelatedToAnyLicenceApplications(Objects.toString(relatedToAnyLicenceApplications, null));
      form.setRelatedLicenceApplications(relatedLicenceApplications);
      form.setRelatedToAnyWellApplications(Objects.toString(relatedToAnyWellApplications, null));
      form.setRelatedWellApplications(relatedWellApplications);
      return form;
    }
  }

}
