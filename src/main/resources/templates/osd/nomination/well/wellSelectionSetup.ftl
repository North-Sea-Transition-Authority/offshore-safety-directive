<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="wellSetupAnswers" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<#assign pageTitle = "Is this nomination in relation to well operatorship?">

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
    <@fdsRadio.radio
      path="form.wellSelectionType"
      radioItems=wellSelectionTypes
      fieldsetHeadingSize="h1"
      fieldsetHeadingClass="govuk-fieldset__legend--l"
      labelText=pageTitle
    />
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>