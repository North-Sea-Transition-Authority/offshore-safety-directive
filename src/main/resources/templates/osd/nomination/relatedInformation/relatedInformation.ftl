<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<k.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="actionUrl" type="java.lang.String" -->
<#-- @ftlvariable name="fieldRestUrl" type="java.lang.String" -->
<#-- @ftlvariable name="approvalsEmailAddress" type="java.lang.String" -->
<#-- @ftlvariable name="preselectedFields" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldAddToListItem>" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsList=breadcrumbsList
>

  <@fdsForm.htmlForm actionUrl=springUrl(actionUrl)>

    <@fdsRadio.radioGroup path="form.relatedToAnyFields" labelText="Is the proposed nomination related to any fields?" hiddenContent=true>
      <@fdsRadio.radioYes path="form.relatedToAnyFields">
        <@fdsAddToList.addToList pathForList="form.fields" pathForSelector="form.fieldSelector" itemName="Field" restUrl=springUrl(fieldRestUrl) alreadyAdded=preselectedFields/>
        <#assign detailsText>
          If the field you want to associate with your nomination is not listed then contact <@mailTo.mailToLink mailToEmailAddress=approvalsEmailAddress/> for advice providing them with the details of the field.
        </#assign>
        <@fdsDetails.details detailsTitle="The field I want to add is not listed" detailsText=detailsText/>
      </@fdsRadio.radioYes>
      <@fdsRadio.radioNo path="form.relatedToAnyFields"/>
    </@fdsRadio.radioGroup>

    <@fdsRadio.radioGroup
      path="form.relatedToAnyLicenceApplications"
      labelText="Are there any PEARS applications that relate to this nomination?"
      hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.relatedToAnyLicenceApplications">
        <@fdsTextarea.textarea
          path="form.relatedLicenceApplications"
          labelText="Provide the regulator reference for each related PEARS application"
          hintText="If the application has not been submitted then provide the applicant reference in place of the regulator reference"
          rows="2"
        />
      </@fdsRadio.radioYes>
      <@fdsRadio.radioNo path="form.relatedToAnyLicenceApplications"/>
    </@fdsRadio.radioGroup>

    <@fdsRadio.radioGroup
      path="form.relatedToAnyWellApplications"
      labelText="Are there any WONS applications that relate to this nomination?"
      hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.relatedToAnyWellApplications">
        <@fdsTextarea.textarea
          path="form.relatedWellApplications"
          labelText="Provide the application reference for each related WONS application"
          rows="2"
        />
      </@fdsRadio.radioYes>
      <@fdsRadio.radioNo path="form.relatedToAnyWellApplications"/>
    </@fdsRadio.radioGroup>

    <@fdsAction.button buttonText="Save and continue"/>

  </@fdsForm.htmlForm>

</@defaultPage>