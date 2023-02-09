<#include '../../layout/layout.ftl'>
<#import '_caseProcessingHeader.ftl' as _caseProcessingHeader/>
<#import '../summary/nominationSummary.ftl' as nominationSummary/>
<#import '../../macros/slideOutActionDropdownItem.ftl' as slideOutActionDropdownItem/>
<#import '_qaChecksSlideout.ftl' as _qaChecksSlideout/>
<#import '_decisionSlideout.ftl' as _decisionSlideout/>
<#import '_withdrawSlideout.ftl' as _withdrawSlideout/>
<#import '_confirmAppointmentSlideout.ftl' as _confirmAppointmentSlideout/>
<#import '_generalCaseNoteSlideout.ftl' as _generalCaseNoteSlideout/>
<#import '../events/eventList.ftl' as eventList/>

<#-- @ftlvariable name="headerInformation" type="uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeader" -->
<#-- @ftlvariable name="summaryView" type="uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="qaChecksSubmitUrl" type="java.lang.String" -->
<#-- @ftlvariable name="hasDropdownActions" type="java.lang.Boolean" -->
<#-- @ftlvariable name="caseProcessingAction_QA" type="java.lang.String" -->
<#-- @ftlvariable name="decisionSubmitUrl" type="java.lang.String" -->
<#-- @ftlvariable name="caseProcessingAction_DECISION" type="java.lang.String" -->
<#-- @ftlvariable name="nominationDecisions" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecision>" -->
<#-- @ftlvariable name="withdrawSubmitUrl" type="java.lang.String" -->
<#-- @ftlvariable name="caseProcessingAction_WITHDRAW" type="java.lang.String" -->
<#-- @ftlvariable name="caseEvents" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventView>" -->

<#assign pageTitle = headerInformation.nominationReference().reference() />

<#assign heading>
  ${pageTitle}
  <span class="govuk-caption-xl">
      ${headerInformation.applicantOrganisationUnitView().name().name()}
  </span>
</#assign>

<@defaultPage
    htmlTitle=pageTitle
    pageHeading=heading
    pageSize=PageSize.FULL_WIDTH
    backLinkUrl=springUrl(backLinkUrl!"")
    breadcrumbsList=breadcrumbsList
    errorItems=[]
>

    <@_caseProcessingHeader.caseProcessingHeader headerInformation/>

    <#assign qaChecksSlideoutPanelId = "qa-checks"/>
    <#assign qaChecksSlideoutText = "Complete QA checks"/>

    <#assign decisionSlideoutPanelId = "decision"/>
    <#assign decisionSlideoutText = "Record decision"/>

    <#assign withdrawSlideoutPanelId = "withdraw"/>
    <#assign withdrawSlideoutText = "Withdraw nomination"/>

    <#assign confirmAppointmentSlideoutPanelId = "confirm-appointment"/>
    <#assign confirmAppointmentSlideoutText = "Confirm appointment"/>

    <#assign generalCaseNoteSlideoutPanelId = "case-note"/>
    <#assign generalCaseNoteSlideoutText = "Add a case note"/>

    <#if hasDropdownActions>
        <@fdsAction.buttonGroup>
            <@fdsActionDropdown.actionDropdown dropdownButtonText="Update nomination">
                <#if qaChecksSubmitUrl?has_content>
                    <@slideOutActionDropdownItem.slideOutActionDropdownItem actionText=qaChecksSlideoutText slideOutPanelId=qaChecksSlideoutPanelId/>
                </#if>
                <#if nominationDecisionAttributes?has_content>
                    <@slideOutActionDropdownItem.slideOutActionDropdownItem actionText=decisionSlideoutText slideOutPanelId=decisionSlideoutPanelId/>
                </#if>
                <#if withdrawSubmitUrl?has_content>
                    <@slideOutActionDropdownItem.slideOutActionDropdownItem actionText=withdrawSlideoutText slideOutPanelId=withdrawSlideoutPanelId/>
                </#if>
                <#if confirmAppointmentAttributes?has_content>
                    <@slideOutActionDropdownItem.slideOutActionDropdownItem actionText=confirmAppointmentSlideoutText slideOutPanelId=confirmAppointmentSlideoutPanelId/>
                </#if>
                <#if generalCaseNoteAttributes?has_content>
                    <@slideOutActionDropdownItem.slideOutActionDropdownItem actionText=generalCaseNoteSlideoutText slideOutPanelId=generalCaseNoteSlideoutPanelId/>
                </#if>
            </@fdsActionDropdown.actionDropdown>
        </@fdsAction.buttonGroup>
    </#if>

    <#if qaChecksSubmitUrl?has_content>
        <@_qaChecksSlideout.qaChecksSlideout
            panelId=qaChecksSlideoutPanelId
            headingText=qaChecksSlideoutText
            postUrl=qaChecksSubmitUrl
            postParam=caseProcessingAction_QA
        />
    </#if>

    <#if nominationDecisionAttributes?has_content>
        <@_decisionSlideout.decisionSlideout
            panelId=decisionSlideoutPanelId
            headingText=decisionSlideoutText
            errorList=errorList![]
            nominationDecisionAttributes=nominationDecisionAttributes
        />
    </#if>

    <#if withdrawSubmitUrl?has_content>
        <@_withdrawSlideout.withdrawSlideout
            panelId=withdrawSlideoutPanelId
            headingText=withdrawSlideoutText
            postUrl=withdrawSubmitUrl
            errorList=errorList![]
            postParam=caseProcessingAction_WITHDRAW
        />
    </#if>

    <#if confirmAppointmentAttributes?has_content>
        <@_confirmAppointmentSlideout.confirmAppointmentSlideout
        panelId=confirmAppointmentSlideoutPanelId
        headingText=confirmAppointmentSlideoutText
        errorList=errorList![]
        attributes=confirmAppointmentAttributes
        />
    </#if>

    <#if generalCaseNoteAttributes?has_content>
        <@_generalCaseNoteSlideout.generalCaseNoteSlideout
            panelId=generalCaseNoteSlideoutPanelId
            headingText=generalCaseNoteSlideoutText
            errorList=errorList![]
            attributes=generalCaseNoteAttributes
        />
    </#if>

    <@fdsTabs.tabs tabsHeading="Nomination tabs">
        <@fdsTabs.tabList>
            <@fdsTabs.tab tabLabel="Nomination form" tabAnchor="nomination-form-tab"/>
            <@fdsTabs.tab tabLabel="Case events" tabAnchor="case-events-tab"/>
        </@fdsTabs.tabList>

        <@fdsTabs.tabContent tabAnchor="nomination-form-tab">
            <h2 class="govuk-heading-l">Nomination form</h2>
            <@nominationSummary.nominationSummary summaryView=summaryView/>
        </@fdsTabs.tabContent>

        <@fdsTabs.tabContent tabAnchor="case-events-tab">
            <@eventList.eventList caseEvents/>
        </@fdsTabs.tabContent>

    </@fdsTabs.tabs>

</@defaultPage>