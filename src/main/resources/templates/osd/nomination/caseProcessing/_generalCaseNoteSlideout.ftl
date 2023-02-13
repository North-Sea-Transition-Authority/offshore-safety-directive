<#include "../../../fds/layout.ftl"/>

<#macro generalCaseNoteSlideout panelId headingText errorList attributes uploadedFiles>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <#-- TODO OSDOP-343 - Change errorList attribute to be slideout specific -->
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(attributes.submitUrl())>
            <@fdsTextInput.textInput path="generalCaseNoteForm.caseNoteSubject.inputValue" labelText="Case note subject"/>
            <@fdsTextarea.textarea path="generalCaseNoteForm.caseNoteText.inputValue" labelText="Case note text"/>
            <@fdsFieldset.fieldset legendHeading="Case note documents" legendHeadingClass="govuk-fieldset__legend--s" legendHeadingSize="h2" optionalLabel=true>
                <@fdsFileUpload.fileUpload
                    id="case-note-file-upload"
                    path="generalCaseNoteForm.caseNoteFiles"
                    uploadUrl=attributes.fileUploadTemplate().uploadUrl()
                    deleteUrl=attributes.fileUploadTemplate().deleteUrl()
                    downloadUrl=attributes.fileUploadTemplate().downloadUrl()
                    allowedExtensions=attributes.fileUploadTemplate().allowedExtensions()
                    maxAllowedSize=attributes.fileUploadTemplate().maxAllowedSize()
                    showAllowedExtensions=true
                    existingFiles=uploadedFiles
                    formName="generalCaseNoteForm"
                />
            </@fdsFieldset.fieldset>
            <@fdsAction.button buttonName=attributes.postParam() buttonText="Add case note"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>