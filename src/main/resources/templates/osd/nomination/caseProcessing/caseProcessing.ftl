<#include '../../layout/layout.ftl'>
<#import '_caseProcessingHeader.ftl' as _caseProcessingHeader/>
<#import '../summary/nominationSummary.ftl' as nominationSummary/>
<#import '../../macros/slideOutActionDropdownItem.ftl' as slideOutActionDropdownItem/>
<#import '_qaChecksSlideout.ftl' as _qaChecksSlideout/>
<#import '_decisionSlideout.ftl' as _decisionSlideout/>
<#import '_withdrawSlideout.ftl' as _withdrawSlideout/>
<#import '_confirmAppointmentSlideout.ftl' as _confirmAppointmentSlideout/>
<#import '_generalCaseNoteSlideout.ftl' as _generalCaseNoteSlideout/>
<#import '_portalReferenceSlideout.ftl' as _systemReferenceSlideout/>
<#import '../events/eventList.ftl' as eventList/>
<#import '_activePortalReferences.ftl' as _activePortalReferences>
<#import '_sendForConsultationSlideout.ftl' as _sendForConsultationSlideout>

<#-- @ftlvariable name="headerInformation" type="uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeader" -->
<#-- @ftlvariable name="summaryView" type="uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="caseEvents" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventView>" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.fds.ErrorItem>" -->
<#-- @ftlvariable name="activePortalReferencesView" type="uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences.ActivePortalReferencesView" -->
<#-- @ftlvariable name="confirmNominationFiles" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView>" -->
<#-- @ftlvariable name="decisionFiles" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView>" -->
<#-- @ftlvariable name="existingCaseNoteFiles" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.file.UploadedFileView>" -->
<#-- @ftlvariable name="managementActions" type="java.util.Map<uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionGroup, java.util.List<uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingAction>>" -->

<#assign pageTitle = headerInformation.nominationReference().reference() />

<#assign heading>
    ${pageTitle}
  <span class="govuk-caption-xl">
      ${headerInformation.applicantOrganisationUnitView().name().name()}
  </span>
</#assign>

<#macro _slideoutButton slideoutPanelId buttonText isInDropdown>
    <#if isInDropdown>
        <@slideOutActionDropdownItem.slideOutActionDropdownItem actionText=buttonText slideOutPanelId=slideoutPanelId/>
    <#else>
        <@fdsSlideOutPanel.slideOutPanelButton buttonPanelId=slideoutPanelId buttonText=buttonText buttonClass="govuk-button govuk-button--secondary"/>
    </#if>
</#macro>

<@defaultPage
htmlTitle=pageTitle
pageHeading=heading
pageSize=PageSize.FULL_COLUMN
backLinkUrl=springUrl(backLinkUrl!"")
breadcrumbsList=breadcrumbsList
errorItems=[]
>

    <@_caseProcessingHeader.caseProcessingHeader headerInformation/>

    <#assign qaChecksSlideoutPanelId = "qa-checks"/>
    <#assign decisionSlideoutPanelId = "decision"/>
    <#assign withdrawSlideoutPanelId = "withdraw"/>
    <#assign confirmAppointmentSlideoutPanelId = "confirm-appointment"/>
    <#assign generalCaseNoteSlideoutPanelId = "case-note"/>
    <#assign pearsSystemReferenceSlideoutPanelId = "pears-references"/>
    <#assign wonsSystemReferenceSlideoutPanelId = "wons-references"/>
    <#assign sendForConsultationSlideoutPanelId = "send-for-consultation"/>

    <#macro _applyAction action isInDropdown>
        <#local actionKeyValue = action.item/>
        <#if actionKeyValue == "QA_CHECKS">
            <@_slideoutButton slideoutPanelId=qaChecksSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign qaChecksAction = action/>
        </#if>
        <#if actionKeyValue == "NOMINATION_DECISION">
            <@_slideoutButton slideoutPanelId=decisionSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign decisionAction = action/>
        </#if>
        <#if actionKeyValue == "WITHDRAW">
            <@_slideoutButton slideoutPanelId=withdrawSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign withdrawAction = action/>
        </#if>
        <#if actionKeyValue == "CONFIRM_APPOINTMENT">
            <@_slideoutButton slideoutPanelId=confirmAppointmentSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign confirmAppointmentAction = action/>
        </#if>
        <#if actionKeyValue == "GENERAL_CASE_NOTE">
            <@_slideoutButton slideoutPanelId=generalCaseNoteSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign generalCaseNoteAction = action/>
        </#if>
        <#if actionKeyValue == "PEARS_REFERENCE">
            <@_slideoutButton slideoutPanelId=pearsSystemReferenceSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign pearsReferenceAction = action/>
        </#if>
        <#if actionKeyValue == "WONS_REFERENCE">
            <@_slideoutButton slideoutPanelId=wonsSystemReferenceSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign wonsReferenceAction = action/>
        </#if>
        <#if actionKeyValue == "SEND_FOR_CONSULTATION">
            <@_slideoutButton slideoutPanelId=sendForConsultationSlideoutPanelId buttonText=actionKeyValue.actionText isInDropdown=isInDropdown/>
            <#assign sendForConsultationAction = action/>
        </#if>
    </#macro>

    <#if managementActions?has_content>
        <@fdsAction.buttonGroup>
            <#list managementActions as group, actions>
                <#if actions?size gt 1>
                    <@fdsActionDropdown.actionDropdown dropdownButtonText=group.displayText>
                        <#list actions as action>
                            <@_applyAction action=action isInDropdown=true/>
                        </#list>
                    </@fdsActionDropdown.actionDropdown>
                <#else>
                    <@_applyAction action=actions[0] isInDropdown=false/>
                </#if>
            </#list>
        </@fdsAction.buttonGroup>
    </#if>

    <#if qaChecksAction?has_content>
        <@_qaChecksSlideout.qaChecksSlideout
            panelId=qaChecksSlideoutPanelId
            headingText=qaChecksAction.item.actionText
            postUrl=qaChecksAction.submitUrl
            postParam=qaChecksAction.caseProcessingActionIdentifier.value()
        />
    </#if>

    <#if decisionAction?has_content>
        <@_decisionSlideout.decisionSlideout
            panelId=decisionSlideoutPanelId
            headingText=decisionAction.item.actionText
            postUrl=decisionAction.submitUrl
            postParam=decisionAction.caseProcessingActionIdentifier.value()
            fileUploadTemplate=decisionAction.modelProperties["fileUploadTemplate"]
            decisionOptions=decisionAction.modelProperties["decisionOptions"]
            errorList=errorList![]
            uploadedFiles=decisionFiles![]
        />
    </#if>

    <#if withdrawAction?has_content>
        <@_withdrawSlideout.withdrawSlideout
            panelId=withdrawSlideoutPanelId
            headingText=withdrawAction.item.actionText
            postUrl=withdrawAction.submitUrl
            errorList=errorList![]
            postParam=withdrawAction.caseProcessingActionIdentifier.value()
        />
    </#if>

    <#if confirmAppointmentAction?has_content>
        <@_confirmAppointmentSlideout.confirmAppointmentSlideout
            panelId=confirmAppointmentSlideoutPanelId
            headingText=confirmAppointmentAction.item.actionText
            postUrl=confirmAppointmentAction.submitUrl
            postParam=confirmAppointmentAction.caseProcessingActionIdentifier.value()
            fileUploadTemplate=confirmAppointmentAction.modelProperties["fileUploadTemplate"]
            errorList=errorList![]
            uploadedFiles=confirmNominationFiles![]
        />
    </#if>

    <#if generalCaseNoteAction?has_content>
        <@_generalCaseNoteSlideout.generalCaseNoteSlideout
            panelId=generalCaseNoteSlideoutPanelId
            headingText=generalCaseNoteAction.item.actionText
            postUrl=generalCaseNoteAction.submitUrl
            postParam=generalCaseNoteAction.caseProcessingActionIdentifier.value()
            fileUploadTemplate=generalCaseNoteAction.modelProperties["fileUploadTemplate"]
            errorList=errorList![]
            uploadedFiles=existingCaseNoteFiles![]
        />
    </#if>

    <#if pearsReferenceAction?has_content>
        <@_systemReferenceSlideout.pearsReferenceSlideout
            panelId=pearsSystemReferenceSlideoutPanelId
            headingText=pearsReferenceAction.item.actionText
            postUrl=pearsReferenceAction.submitUrl
            postParam=pearsReferenceAction.caseProcessingActionIdentifier.value()
        />
    </#if>

    <#if wonsReferenceAction?has_content>
        <@_systemReferenceSlideout.wonsReferenceSlideout
            panelId=wonsSystemReferenceSlideoutPanelId
            headingText=wonsReferenceAction.item.actionText
            postUrl=wonsReferenceAction.submitUrl
            postParam=wonsReferenceAction.caseProcessingActionIdentifier.value()
        />
    </#if>

    <#if sendForConsultationAction?has_content>
        <@_sendForConsultationSlideout.sendForConsultationSlideout
            panelId=sendForConsultationSlideoutPanelId
            headingText=sendForConsultationAction.item.actionText
            postUrl=sendForConsultationAction.submitUrl
            postParam=sendForConsultationAction.caseProcessingActionIdentifier.value()
        />
    </#if>

    <@fdsTabs.tabs tabsHeading="Nomination tabs">
        <@fdsTabs.tabList>
            <@fdsTabs.tab tabLabel="Nomination form" tabAnchor="nomination-form-tab"/>
            <@fdsTabs.tab tabLabel="Case events" tabAnchor="case-events-tab"/>
        </@fdsTabs.tabList>

        <@fdsTabs.tabContent tabAnchor="nomination-form-tab">
          <h2 class="govuk-heading-l">Nomination form</h2>
            <@_activePortalReferences.activePortalReferences activePortalReferencesView/>
            <@nominationSummary.nominationSummary summaryView=summaryView/>
        </@fdsTabs.tabContent>

        <@fdsTabs.tabContent tabAnchor="case-events-tab">
            <@eventList.eventList caseEvents/>
        </@fdsTabs.tabContent>

    </@fdsTabs.tabs>

</@defaultPage>