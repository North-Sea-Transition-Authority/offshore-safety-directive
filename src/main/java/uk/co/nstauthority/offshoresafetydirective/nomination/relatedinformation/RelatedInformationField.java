package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "related_information_fields")
class RelatedInformationField {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "related_information_id")
  private RelatedInformation relatedInformation;

  private Integer fieldId;
  private String fieldName;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
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
