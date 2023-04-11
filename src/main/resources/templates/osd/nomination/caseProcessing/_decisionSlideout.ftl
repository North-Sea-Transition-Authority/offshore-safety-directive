<#include "../../../fds/layout.ftl"/>

<#macro decisionSlideout panelId headingText postUrl postParam fileUploadTemplate decisionOptions errorList uploadedFiles>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <#-- TODO OSDOP-343 - Change errorList attribute to be slideout specific -->
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsRadio.radioGroup path="form.nominationDecision" labelText="What decision was made on this nomination?">
                <#assign isFirstNominationDecision = true/>
                <#list decisionOptions as decision>
                    <@fdsRadio.radioItem path="form.nominationDecision" itemMap={decision: decision.displayText} isFirstItem=isFirstNominationDecision/>
                    <#assign isFirstNominationDecision = false/>
              </#list>
            </@fdsRadio.radioGroup>
            <@fdsDateInput.dateInput
                formId="form.decisionDate"
                dayPath="form.decisionDate.dayInput.inputValue"
                monthPath="form.decisionDate.monthInput.inputValue"
                yearPath="form.decisionDate.yearInput.inputValue"
                labelText="Date decision was made"
            />
            <@fdsTextarea.textarea path="form.comments.inputValue" labelText="Decision comments"/>
            <@fdsFieldset.fieldset legendHeading="Decision document" legendHeadingClass="govuk-fieldset__legend--s" legendHeadingSize="h2">
                <@fdsFileUpload.fileUpload
                    id="decision-file-upload"
                    path="form.decisionFiles"
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
            <@fdsAction.button buttonName=postParam buttonText="Submit decision"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>