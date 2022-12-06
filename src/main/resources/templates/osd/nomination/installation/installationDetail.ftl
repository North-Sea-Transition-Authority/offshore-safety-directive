<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="installationPhases" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="alreadyAddedInstallations" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationAddToListView>" -->
<#-- @ftlvariable name="installationsRestUrl" type="String" -->

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
      pathForList="form.installations"
      pathForSelector="form.installationsSelect"
      alreadyAdded=alreadyAddedInstallations
      title=""
      itemName="Installation"
      noItemText="No installations added"
      invalidItemText="This installation is not a valid selection and must be removed"
      addToListId="installation-table"
      selectorLabelText="Select an installation to add to this nomination"
      restUrl=springUrl(installationsRestUrl)
      selectorMinInputLength=3
    />
    <@fdsRadio.radioGroup
      path="form.forAllInstallationPhases"
      labelText="Is this nomination for all installation phases?"
      hintText="Installation phases include development or decommissioning"
      hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.forAllInstallationPhases"/>
      <@fdsRadio.radioNo path="form.forAllInstallationPhases">
        <@fdsCheckbox.checkboxGroup
          path="form.developmentDesignPhase"
          fieldsetHeadingText="Which installations phases is this nomination for?"
          nestingPath="form.forAllInstallationPhases"
        >
          <@fdsCheckbox.checkboxItem
            path="form.developmentDesignPhase"
            labelText=installationPhases["DEVELOPMENT_DESIGN"]
          />
          <@fdsCheckbox.checkboxItem
            path="form.developmentConstructionPhase"
            labelText=installationPhases["DEVELOPMENT_CONSTRUCTION"]
          />
          <@fdsCheckbox.checkboxItem
            path="form.developmentInstallationPhase"
            labelText=installationPhases["DEVELOPMENT_INSTALLATION"]
          />
          <@fdsCheckbox.checkboxItem
            path="form.developmentCommissioningPhase"
            labelText=installationPhases["DEVELOPMENT_COMMISSIONING"]
          />
          <@fdsCheckbox.checkboxItem
            path="form.developmentProductionPhase"
            labelText=installationPhases["DEVELOPMENT_PRODUCTION"]
          />
          <@fdsCheckbox.checkboxItem
            path="form.decommissioningPhase"
            labelText=installationPhases["DECOMMISSIONING"]
          />
        </@fdsCheckbox.checkboxGroup>
      </@fdsRadio.radioNo>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>