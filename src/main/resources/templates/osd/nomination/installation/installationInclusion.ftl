<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#assign pageTitle = "Is this nomination in relation to installation operatorship?">

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=""
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm
   actionUrl=springUrl(actionUrl)
  >
    <@fdsRadio.radioGroup
      path="form.includeInstallationsInNomination"
      labelText=pageTitle
      fieldsetHeadingSize="h1"
      fieldsetHeadingClass="govuk-fieldset__legend--l"
    >
      <@fdsRadio.radioYes
        path="form.includeInstallationsInNomination"
      />
      <@fdsRadio.radioNo
        path="form.includeInstallationsInNomination"
      />
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>