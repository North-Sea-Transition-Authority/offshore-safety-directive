<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="portalOrganisationsRestUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="preselectedItems" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->

<#assign pageTitle = "Applicant details" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl!"")
  breadcrumbsList=breadcrumbsList
>
  <@fdsForm.htmlForm
    actionUrl=springUrl(actionUrl)
  >
    <@fdsSearchSelector.searchSelectorRest
      path="form.portalOrganisationId"
      preselectedItems=preselectedItems
      labelText="What organisation is making this nomination?"
      selectorMinInputLength=2
      restUrl=springUrl(portalOrganisationsRestUrl)
    />
    <@fdsDetails.summaryDetails summaryTitle="The organisation making the nomination is not listed">
      <p class="govuk-body">
        If the organisation making the nomination is not shown in the list then you must contact them to provide you with access to make nominations on their behalf.
      </p>
    </@fdsDetails.summaryDetails>
    <@fdsTextInput.textInput
      path="form.applicantReference"
      labelText="Applicant reference"
      optionalLabel=true
    />
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>