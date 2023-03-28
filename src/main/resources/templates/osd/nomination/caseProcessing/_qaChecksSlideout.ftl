<#include "../../../fds/layout.ftl"/>

<#macro qaChecksSlideout panelId headingText postUrl postParam>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextarea.textarea path="qaChecksForm.comment.inputValue" labelText="QA comments"/>
            <@fdsAction.button buttonText="Complete QA checks" buttonName=postParam/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>