<#include '../../layout/layout.ftl'>
<#import '_wellActivityPhaseGuidance.ftl' as activityPhase>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="alreadyAddedSubareas" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaAddToListView>" -->
<#-- @ftlvariable name="blockSubareaRestUrl" type="String" -->
<#-- @ftlvariable name="accidentRegulatorBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties" -->
<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties" -->

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
      selectorMinInputLength=2
    />
    <@fdsRadio.radioGroup
      path="form.validForFutureWellsInSubarea"
      labelText="
        In addition to any existing well stock, would you like this nomination to cover future wells that may
        be drilled in the selected subareas?
      "
    >
      <@fdsRadio.radioYes path="form.validForFutureWellsInSubarea"/>
      <@fdsRadio.radioNo path="form.validForFutureWellsInSubarea"/>
    </@fdsRadio.radioGroup>
    <@fdsDetails.summaryDetails summaryTitle="Why would I want this nomination to cover future wells?">
      <p class="govuk-body">
        This ${serviceBranding.mnemonic()} feature aims to reduce administrative burden and potential blockers. Selecting Yes to this question
        would help avoid having to submit separate ${serviceBranding.mnemonic()} nominations for future wells in the selected subareas,
        if the well operator and activity phases you specify now continue to be suitable for your well activities.
      </p>
    </@fdsDetails.summaryDetails>
    <@fdsRadio.radioGroup
      path="form.forAllWellPhases"
      labelText="Is this nomination for all well activity phases?"
      hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.forAllWellPhases"/>
      <@fdsRadio.radioNo path="form.forAllWellPhases">
        <@fdsCheckbox.checkboxGroup
          path="form.explorationAndAppraisalPhase"
          fieldsetHeadingText="Which well activity phases is this nomination for?"
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
    <@activityPhase.wellActivityPhaseGuidance accidentRegulatorBranding=accidentRegulatorBranding/>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>