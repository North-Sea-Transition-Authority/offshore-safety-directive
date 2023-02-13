<#import '../../../fds/components/slideOutPanel/slideOutPanel.ftl' as fdsSlideOutPanel>
<#import '../../../fds/components/textarea/textarea.ftl' as fdsTextarea>
<#import '../../../fds/components/form/htmlForm.ftl' as fdsForm>
<#import '../../../fds/components/button/button.ftl' as fdsAction>
<#import '../../../fds/objects/layouts/generic.ftl' as fdsGeneric>

<#macro pearsReferenceSlideout panelId headingText attributes>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsForm.htmlForm actionUrl=fdsGeneric.springUrl(attributes.submitUrl())>
            <@fdsTextarea.textarea path="pearsPortalReferenceForm.references.inputValue" labelText="Related PEARS application references" optionalLabel=true/>
            <@fdsAction.button buttonName=attributes.postParam() buttonText="Update references"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>