<#import '../../../fds/components/slideOutPanel/slideOutPanel.ftl' as fdsSlideOutPanel>
<#import '../../../fds/components/form/htmlForm.ftl' as fdsForm>
<#import '../../../fds/components/button/button.ftl' as fdsAction>
<#import '../../../fds/components/textarea/textarea.ftl' as fdsTextarea>
<#include '../../util/url.ftl'>

<#macro requestUpdateSlideout panelId headingText postUrl postParam>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
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