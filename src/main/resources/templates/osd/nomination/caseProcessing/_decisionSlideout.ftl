<#include "../../../fds/layout.ftl"/>

<#macro decisionSlideout panelId headingText postUrl postParam nominationDecisions>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsRadio.radioGroup path="nominationDecisionForm.nominationDecision" labelText="Nomination decision">
                <#assign isFirstNominationDecision = true/>
                <#list nominationDecisions as decision>
                    <@fdsRadio.radioItem path="nominationDecisionForm.nominationDecision" itemMap={decision: decision.displayText} isFirstItem=isFirstNominationDecision/>
                    <#assign isFirstNominationDecision = false/>
              </#list>
            </@fdsRadio.radioGroup>
            <@fdsTextarea.textarea path="nominationDecisionForm.comments.inputValue" labelText="Decision comments"/>
            <@fdsDateInput.dateInput
                formId="nominationDecisionForm.decisionDate"
                dayPath="nominationDecisionForm.decisionDate.dayInput.inputValue"
                monthPath="nominationDecisionForm.decisionDate.monthInput.inputValue"
                yearPath="nominationDecisionForm.decisionDate.yearInput.inputValue"
                labelText="Decision date"
            />
            <@fdsAction.button buttonName=postParam buttonText="Submit decision"/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>