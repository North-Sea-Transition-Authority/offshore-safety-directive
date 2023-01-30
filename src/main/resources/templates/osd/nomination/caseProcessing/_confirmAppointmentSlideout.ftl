<#include "../../../fds/layout.ftl"/>

<#macro confirmAppointmentSlideout panelId headingText errorList attributes>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <#-- TODO OSDOP-343 - Change errorList attribute to be slideout specific -->
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(attributes.submitUrl())>
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
            <@fdsAction.button buttonName=attributes.postParam() buttonText="Confirm appointment"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>