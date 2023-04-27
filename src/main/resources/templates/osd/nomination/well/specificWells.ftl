<#include '../../layout/layout.ftl'>
<#import '_wellActivityPhaseGuidance.ftl' as activityPhase>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="alreadyAddedWells" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellAddToListView>" -->
<#-- @ftlvariable name="wellsRestUrl" type="String" -->
<#-- @ftlvariable name="wellPhases" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="accidentRegulatorBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.AccidentRegulatorConfigurationProperties" -->

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
    <@fdsAddToList.addToList
      pathForList="form.wells"
      pathForSelector="form.wellsSelect"
      alreadyAdded=alreadyAddedWells
      title=""
      itemName="Well"
      noItemText="No wells added"
      invalidItemText="This well is invalid"
      addToListId="well-table"
      selectorLabelText="Select a well to add to this nomination"
      restUrl=springUrl(wellsRestUrl)
      selectorMinInputLength=3
    />
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
            labelText=wellPhases["EXPLORATION_AND_APPRAISAL"]
          />
          <@fdsCheckbox.checkboxItem
            path="form.developmentPhase"
            labelText=wellPhases["DEVELOPMENT"]
          />
          <@fdsCheckbox.checkboxItem
            path="form.decommissioningPhase"
            labelText=wellPhases["DECOMMISSIONING"]
          />
        </@fdsCheckbox.checkboxGroup>
      </@fdsRadio.radioNo>
    </@fdsRadio.radioGroup>
    <@activityPhase.wellActivityPhaseGuidance accidentRegulatorBranding=accidentRegulatorBranding/>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>