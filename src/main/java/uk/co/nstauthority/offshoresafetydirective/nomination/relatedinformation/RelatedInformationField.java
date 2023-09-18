package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "related_information_fields")
class RelatedInformationField {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "related_information_id")
  private RelatedInformation relatedInformation;

  private Integer fieldId;
  private String fieldName;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public RelatedInformation getRelatedInformation() {
    return relatedInformation;
  }

  public void setRelatedInformation(
      RelatedInformation relatedInformation) {
    this.relatedInformation = relatedInformation;
  }

  public Integer getFieldId() {
    return fieldId;
  }

  public void setFieldId(Integer fieldId) {
    this.fieldId = fieldId;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
}
