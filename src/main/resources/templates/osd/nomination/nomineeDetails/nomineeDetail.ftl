<#include '../../layout/layout.ftl'>
<#import '../../util/getFirstFormFieldWithError.ftl' as checkBoxValidationUtil>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="actionUrl" type="String" -->
<#-- @ftlvariable name="portalOrganisationsRestUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<k.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="appendixDocumentFileUploadTemplate" type="uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate" -->
<#-- @ftlvariable name="uploadedFiles" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView>" -->

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
    <@fdsDetails.summaryDetails
      summaryTitle="The well or installation operator I want to nominate is not shown in the list"
    >
      <p class="govuk-body">
        If the operator you want to nominate is not shown in the list then you must contact
        <@mailTo.mailToLink
          linkText=customerBranding.businessEmailAddress()
          mailToEmailAddress=customerBranding.businessEmailAddress()
        />
      </p>
    </@fdsDetails.summaryDetails>
    <@fdsTextarea.textarea
      path="form.reasonForNomination"
      labelText="Why do you want to appoint this operator?"
      hintText="
        Specify the particular offshore oil and gas operations, activities, duties and responsibilities the
        proposed operator is being nominated for
      "
    />
    <@fdsDateInput.dateInput
      dayPath="form.plannedStartDay"
      monthPath="form.plannedStartMonth"
      yearPath="form.plannedStartYear"
      labelText="On what date is the appointment planned to take effect?"
      defaultHint=false
      hintText="
        If you are not sure of the planned date, provide an estimated date. The date should not be less than 3 months
        in the future. Where in exceptional circumstances a date earlier than this is proposed, you will need to
        provide a robust justification later in this process and prior to submission.
      "
      formId="planned-start-date"
    />

      <@fdsFieldset.fieldset
        legendHeading="Provide the Appendix C and any associated documents"
        legendHeadingClass="govuk-fieldset__legend--s"
        legendHeadingSize="h2"
      >
          <@fdsFileUpload.fileUpload
            id="appendix-c-documents"
            path="form.appendixDocuments"
            uploadUrl=appendixDocumentFileUploadTemplate.uploadUrl()
            deleteUrl=appendixDocumentFileUploadTemplate.deleteUrl()
            downloadUrl=appendixDocumentFileUploadTemplate.downloadUrl()
            maxAllowedSize=appendixDocumentFileUploadTemplate.maxAllowedSize()
            allowedExtensions=appendixDocumentFileUploadTemplate.allowedExtensions()
            existingFiles=uploadedFiles
          />
      </@fdsFieldset.fieldset>

      <!-- TODO OSDOP-184 - Add appendix C file guidance -->

<#--Check which is the first checkbox that is not selected and pass that as the path to the checkBoxGroupComponent to properly bind the error-->
    <#assign checkboxGroupPath=checkBoxValidationUtil.getFirstFormFieldWithError(["form.operatorHasAuthority", "form.licenseeAcknowledgeOperatorRequirements", "form.operatorHasCapacity"])/>
    <@fdsCheckbox.checkboxGroup
      path=checkboxGroupPath
      fieldsetHeadingText="Licensee declarations"
    >
      <@fdsCheckbox.checkboxItem
        path="form.operatorHasAuthority"
        labelText="
          The licensee(s) will provide the proposed operator with the necessary authority to deliver their
          safety and environmental duties and responsibilities
        "
      />
      <@fdsCheckbox.checkboxItem
        path="form.licenseeAcknowledgeOperatorRequirements"
        labelText="
          The licensee(s) will take all reasonable steps to ensure that the appointed operator meets those
          requirements, by implementing arrangements for the monitoring, audit and review of the proposed
          operator's performance
        "
      />
      <@fdsCheckbox.checkboxItem
        path="form.operatorHasCapacity"
        labelText="
          The proposed operator has the capacity to meet the requirements relevant to those duties and responsibilities
        "
      />
    </@fdsCheckbox.checkboxGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>