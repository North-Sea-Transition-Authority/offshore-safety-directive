<#include '../../util/url.ftl'>
<#include "../../../fds/layout.ftl"/>

<#macro requestUpdateSlideout panelId headingText postUrl postParam errorList>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextarea.textarea
                path="nominationRequestUpdateForm.reason.inputValue"
                labelText="What information needs to be updated?"
                hintText="This information will be provided to the applicant"
            />
            <@fdsAction.button buttonText="Request update" buttonName=postParam/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>