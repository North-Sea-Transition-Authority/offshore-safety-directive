<#import '../../../fds/components/slideOutPanel/slideOutPanel.ftl' as fdsSlideOutPanel>
<#import '../../../fds/components/form/htmlForm.ftl' as fdsForm>
<#import '../../../fds/components/button/button.ftl' as fdsAction>
<#include '../../util/url.ftl'>
<#import '../../../fds/components/error/error.ftl' as fdsError>
<#import '../../../fds/components/textarea/textarea.ftl' as fdsTextarea>

<#macro consultationResponseSlideout panelId headingText postUrl postParam errorList>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
    <#-- TODO OSDOP-343 - Change errorList attribute to be slideout specific -->
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextarea.textarea
                path="nominationConsultationResponseForm.response.inputValue"
                labelText="Consultation response"
            />
            <@fdsAction.button buttonText="Send for consultation" buttonName=postParam/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>