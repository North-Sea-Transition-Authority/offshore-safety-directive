<#include "../../../fds/layout.ftl"/>

<#macro generalCaseNoteSlideout panelId headingText errorList attributes>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <#-- TODO OSDOP-343 - Change errorList attribute to be slideout specific -->
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(attributes.submitUrl())>
            <@fdsTextInput.textInput path="generalCaseNoteForm.caseNoteSubject.inputValue" labelText="Case note subject"/>
            <@fdsTextarea.textarea path="generalCaseNoteForm.caseNoteText.inputValue" labelText="Case note text"/>
            <@fdsAction.button buttonName=attributes.postParam() buttonText="Add case note"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>