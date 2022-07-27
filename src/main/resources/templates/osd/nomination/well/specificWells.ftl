<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<k.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->

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
      path="form.forAllWellPhases"
      labelText="Is this nomination for all well phases?"
      hintText="Well phases include exploration & appraisal, development or decommissioning"
      hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.forAllWellPhases"/>
      <@fdsRadio.radioNo path="form.forAllWellPhases">
        <@fdsCheckbox.checkboxGroup
          path="form.explorationAndAppraisalPhase"
          fieldsetHeadingText="Which well phases is this nomination for?"
          nestingPath="form.forAllWellPhases"
        >
          <@fdsCheckbox.checkboxItem
            path="form.explorationAndAppraisalPhase"
            labelText="Exploration & Appraisal"
          />
          <@fdsCheckbox.checkboxItem
            path="form.developmentPhase"
            labelText="Development"
          />
          <@fdsCheckbox.checkboxItem
            path="form.decommissioningPhase"
            labelText="Decommissioning"
          />
        </@fdsCheckbox.checkboxGroup>
      </@fdsRadio.radioNo>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>