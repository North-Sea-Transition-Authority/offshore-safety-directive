<#include "../../../fds/layout.ftl"/>

<#macro qaChecksSlideout panelId headingText postUrl>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextarea.textarea path="qaChecksForm.comment" labelText="QA comments" optionalLabel=true/>
            <@fdsAction.button buttonText="Complete QA checks"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>