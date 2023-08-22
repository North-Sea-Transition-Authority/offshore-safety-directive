<#include '../../layout/layout.ftl'>
<#import '_appointmentDetailSummary.ftl' as _appointmentDetailSummary/>

<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="phases" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="timelineUrl" type="java.lang.String" -->
<#-- @ftlvariable name="fileUploadTemplate" type="uk.co.nstauthority.offshoresafetydirective.file.FileUploadTemplate" -->
<#-- @ftlvariable name="uploadedFiles" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView>" -->

<@defaultPage
    pageHeading="Are you sure you want to terminate this appointment for ${assetName}?"
    pageSize=PageSize.TWO_THIRDS_COLUMN
    errorItems=errorList
    backLinkWithBrowserBack=true
    showNavigationItems=(loggedInUser?has_content)
    allowSearchEngineIndexing=false
>
  <@fdsForm.htmlForm
    actionUrl=springUrl(submitUrl)
  >
    <@_appointmentDetailSummary.appointmentDetailSummary
        appointedOperator="${appointedOperator}"
        responsibleFromDate="${responsibleFromDate}"
        phases=phases
        createdBy="${createdBy}"
    />
    <@fdsTextarea.textarea
        path="form.reason.inputValue"
        labelText="Why is this appointment being terminated?"
    />
    <@fdsDateInput.dateInput
        dayPath="form.terminationDate.dayInput.inputValue"
        monthPath="form.terminationDate.monthInput.inputValue"
        yearPath="form.terminationDate.yearInput.inputValue"
        labelText="What is the date this appointment was terminated?"
        formId="terminationDate"
    />
    <@fdsFieldset.fieldset
        legendHeading="Supporting documents"
        legendHeadingClass="govuk-fieldset__legend--s"
        legendHeadingSize="h2"
        hintText="The termination notice should be included as part of any documents uploaded"
    >
      <@fdsFileUpload.fileUpload
          id="termination-file-upload"
          path="form.terminationDocuments"
          uploadUrl=fileUploadTemplate.uploadUrl()
          deleteUrl=fileUploadTemplate.deleteUrl()
          downloadUrl=fileUploadTemplate.downloadUrl()
          allowedExtensions=fileUploadTemplate.allowedExtensions()
          maxAllowedSize=fileUploadTemplate.maxAllowedSize()
          showAllowedExtensions=true
          multiFile=true
          existingFiles=uploadedFiles
      />
    </@fdsFieldset.fieldset>
    <@fdsWarning.warning>
        Terminating this appointment will result in no operator being appointed for ${assetName}
    </@fdsWarning.warning>
    <@fdsAction.submitButtons
        primaryButtonText="Terminate appointment"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(timelineUrl)
        primaryButtonClass="govuk-button govuk-button--warning"
    />
  </@fdsForm.htmlForm>
</@defaultPage>