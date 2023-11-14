<#include "../../../fds/layout.ftl"/>

<#macro generalCaseNoteSlideout panelId headingText postUrl postParam fileUploadTemplate errorList uploadedFiles>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextInput.textInput path="generalCaseNoteForm.caseNoteSubject.inputValue" labelText="Case note subject"/>
            <@fdsTextarea.textarea path="generalCaseNoteForm.caseNoteText.inputValue" labelText="Case note text"/>
            <@fdsFieldset.fieldset legendHeading="Case note documents" legendHeadingClass="govuk-fieldset__legend--s" legendHeadingSize="h2" optionalLabel=true>
                <@fdsFileUpload.fileUpload
                    id="case-note-file-upload"
                    path="generalCaseNoteForm.caseNoteFiles"
                    uploadUrl=fileUploadTemplate.uploadUrl()
                    deleteUrl=fileUploadTemplate.deleteUrl()
                    downloadUrl=fileUploadTemplate.downloadUrl()
                    allowedExtensions=fileUploadTemplate.allowedExtensions()
                    maxAllowedSize=fileUploadTemplate.maxAllowedSize()
                    showAllowedExtensions=true
                    existingFiles=uploadedFiles
                    formName="generalCaseNoteForm"
                />
            </@fdsFieldset.fieldset>
            <@fdsAction.button buttonName=postParam buttonText="Add case note"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>