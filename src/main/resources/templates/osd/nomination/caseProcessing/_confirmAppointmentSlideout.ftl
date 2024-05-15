<#include "../../../fds/layout.ftl"/>

<#macro confirmAppointmentSlideout panelId headingText postUrl postParam fileUploadTemplate errorList uploadedFiles>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsDateInput.dateInput
                formId="confirmAppointmentForm.appointmentDate"
                dayPath="confirmAppointmentForm.appointmentDate.dayInput.inputValue"
                monthPath="confirmAppointmentForm.appointmentDate.monthInput.inputValue"
                yearPath="confirmAppointmentForm.appointmentDate.yearInput.inputValue"
                labelText="Date appointment took effect"
            />
            <@fdsTextarea.textarea
                path="confirmAppointmentForm.comments.inputValue"
                labelText="Appointment comments"
                optionalLabel=true
            />

            <@fdsFieldset.fieldset
                legendHeading="Appointment files"
                legendHeadingClass="govuk-fieldset__legend--s"
                legendHeadingSize="h2"
                optionalLabel=true
            >
                <@fdsFileUpload.fileUpload
                    id="confirm-appointment-files"
                    path="confirmAppointmentForm.files"
                    formName="confirmAppointmentForm"
                    uploadUrl=fileUploadTemplate.uploadUrl()
                    deleteUrl=fileUploadTemplate.deleteUrl()
                    downloadUrl=fileUploadTemplate.downloadUrl()
                    allowedExtensions=fileUploadTemplate.allowedExtensions()
                    maxAllowedSize=fileUploadTemplate.maxAllowedSize()
                    showAllowedExtensions=true
                    existingFiles=uploadedFiles
                    multiFile=false
                />
            </@fdsFieldset.fieldset>
            <@fdsAction.button buttonName=postParam buttonText="Confirm appointment"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>