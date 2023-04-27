<#import '../../../fds/components/slideOutPanel/slideOutPanel.ftl' as fdsSlideOutPanel>
<#import '../../../fds/components/form/htmlForm.ftl' as fdsForm>
<#import '../../../fds/components/button/button.ftl' as fdsAction>
<#include '../../util/url.ftl'>

<#macro sendForConsultationSlideout panelId headingText postUrl postParam>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <p class="govuk-body">
              An email will be sent to all consultation recipients to request their advice on this nomination.
            </p>
            <@fdsAction.button buttonText="Send for consultation" buttonName=postParam/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>