<#include "../../../fds/layout.ftl"/>

<#macro withdrawSlideout panelId headingText postUrl postParam>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextarea.textarea path="withdrawNominationForm.reason.inputValue" labelText="Reason for withdrawal"/>
            <@fdsAction.button buttonText="Withdraw nomination" buttonName=postParam/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>