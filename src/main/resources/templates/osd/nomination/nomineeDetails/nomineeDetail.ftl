<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="portalOrganisationsRestUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<k.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsList=breadcrumbsList
>
  <@fdsForm.htmlForm
    actionUrl=springUrl(actionUrl)
  >
    <@fdsSearchSelector.searchSelectorRest
      path="form.nominatedOrganisationId"
      preselectedItems=preselectedItems
      labelText="Who is the proposed well or installation operator?"
      selectorMinInputLength=2
      restUrl=springUrl(portalOrganisationsRestUrl)
    />
    <@fdsDetails.summaryDetails summaryTitle="The well or installation operator I want to nominate is not shown in the list">
      <p class="govuk-body">
        If the organisation you want to nominate is not shown in the list then you must contact the
        <@mailTo.mailToLink linkText=customerBranding.mnemonic() mailToEmailAddress=customerBranding.businessEmailAddress()/>.
      </p>
    </@fdsDetails.summaryDetails>
    <@fdsTextarea.textarea
      path="form.reasonForNomination"
      labelText="Reason for the nomination"
    />
    <@fdsDateInput.dateInput
      dayPath="form.plannedStartDay"
      monthPath="form.plannedStartMonth"
      yearPath="form.plannedStartYear"
      labelText="On what date is the appointment planned to take effect?"
      defaultHint=false
      hintText="If you are not sure of the planned date, provide an estimated date"
      formId="planned-start-date"
    />
    <@fdsCheckbox.checkboxGroup
      path="form.operatorHasAuthority"
      fieldsetHeadingText="Nominee declarations"
    >
      <@fdsCheckbox.checkboxItem
        path="form.operatorHasAuthority"
        labelText="The licensee(s) will provide the proposed operator with the necessary authority to deliver their safety and environmental duties and responsibilities"
      />
      <@fdsCheckbox.checkboxItem
        path="form.licenseeAcknowledgeOperatorRequirements"
        labelText="The licensee(s) will take all reasonable steps to ensure that the appointed operator meets those requirements, by implementing arrangements for the monitoring, audit and review of the proposed operator’s performance"
      />
      <@fdsCheckbox.checkboxItem
        path="form.operatorHasCapacity"
        labelText="The proposed operator has the capacity to meet the requirements relevant to those duties and responsibilities"
      />
    </@fdsCheckbox.checkboxGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>