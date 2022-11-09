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

    <@fdsAction.button buttonText="Save and continue"/>

  </@fdsForm.htmlForm>

</@defaultPage>