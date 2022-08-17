<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="alreadyAddedSubareas" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaAddToListView>" -->
<#-- @ftlvariable name="blockSubareaRestUrl" type="String" -->

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
errorItems=errorList
pageSize=PageSize.TWO_THIRDS_COLUMN
backLinkUrl=springUrl(backLinkUrl)
>
  <p class="govuk-body">
    This will apply the nominated operator to all wells that at the time of submission exist within the licence block
    subareas listed below. You will be able to review this list and exclude wells from the nomination if required.
  </p>
  <@fdsForm.htmlForm
    actionUrl=springUrl(actionUrl)
  >
    <@fdsAddToList.addToList
      pathForList="form.subareas"
      pathForSelector="form.subareasSelect"
      alreadyAdded=alreadyAddedSubareas
      title=""
      itemName="Licence block subarea"
      noItemText="No licence block subarea added"
      invalidItemText="This licence block subarea is invalid"
      addToListId="block-subarea-table"
      selectorLabelText="Select a licence block subarea to add to this nomination"
      restUrl=springUrl(blockSubareaRestUrl)
    />
    <@fdsRadio.radioGroup
      path="form.validForFutureWellsInSubarea"
      labelText="Is this nomination for future wells drilled in the selected subareas?"
    >
      <@fdsRadio.radioYes path="form.validForFutureWellsInSubarea"/>
      <@fdsRadio.radioNo path="form.validForFutureWellsInSubarea"/>
    </@fdsRadio.radioGroup>
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