<#include "../../../fds/layout.ftl"/>

<#macro withdrawSlideout panelId headingText postUrl errorList postParam>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <#-- TODO OSDOP-343 - Change errorList attribute to be slideout specific -->
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextarea.textarea path="withdrawNominationForm.reason.inputValue" labelText="Reason for withdrawal"/>
            <@fdsAction.button buttonText="Withdraw nomination" buttonName=postParam/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>