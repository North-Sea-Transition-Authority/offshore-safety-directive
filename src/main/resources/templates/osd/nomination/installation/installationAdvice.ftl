<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm
   actionUrl=springUrl(actionUrl)
  >
    <@fdsRadio.radioGroup
      path="form.includeInstallationsInNomination"
      labelText="Is this nomination in relation to installation operatorship?"
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