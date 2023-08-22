<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="installationPhases" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="alreadyAddedInstallations" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationAddToListView>" -->
<#-- @ftlvariable name="installationsRestUrl" type="String" -->
<#-- @ftlvariable name="alreadyAddedLicences" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationAddToListView>" -->
<#-- @ftlvariable name="licencesRestUrl" type="String" -->
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
    <@fdsFieldset.fieldset
      legendHeadingSize="h2"
      legendHeadingClass="govuk-fieldset__legend--m"
      legendHeading="Licences relevant to this nomination"
    >
      <@fdsAddToList.addToList
        pathForList="form.licences"
        pathForSelector="form.licencesSelect"
        alreadyAdded=alreadyAddedLicences
        title=""
        itemName="Licence"
        noItemText="No licences added"
        invalidItemText="This licene is not a valid selection and must be removed"
        addToListId="licence-table"
        selectorLabelText="Select a licence relevant to the installations on this nomination"
        restUrl=springUrl(licencesRestUrl)
        selectorMinInputLength=2
        selectorInputClass="govuk-!-width-one-third"
      />
    </@fdsFieldset.fieldset>
    <@fdsRadio.radioGroup
      path="form.forAllInstallationPhases"
      labelText="Is this nomination for all installation activity phases?"
      hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.forAllInstallationPhases"/>
      <@fdsRadio.radioNo path="form.forAllInstallationPhases">
        <@fdsCheckbox.checkboxGroup
          path="form.developmentDesignPhase"
          fieldsetHeadingText="Which installation activity phases is this nomination for?"
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
    <@fdsDetails.summaryDetails summaryTitle="What are the installation activity phases?">
      <p class="govuk-body">
        Installation activity phases include development and decommissioning, as defined in the
        <@fdsAction.link
          linkText="${accidentRegulatorBranding.name()}'s Appendix C guidance"
          linkUrl=accidentRegulatorBranding.consultationGuidanceUrl()
          openInNewTab=true
        />
      </p>
    </@fdsDetails.summaryDetails>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>