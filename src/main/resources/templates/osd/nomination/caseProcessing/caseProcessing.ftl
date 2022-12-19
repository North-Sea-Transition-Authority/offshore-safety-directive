<#include '../../layout/layout.ftl'>
<#import '_caseProcessingHeader.ftl' as _caseProcessingHeader/>
<#import '../summary/nominationSummary.ftl' as nominationSummary/>
<#import '../../macros/slideOutActionDropdownItem.ftl' as slideOutActionDropdownItem/>
<#import '_qaChecksSlideout.ftl' as _qaChecksSlideout/>

<#-- @ftlvariable name="headerInformation" type="uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingHeader" -->
<#-- @ftlvariable name="summaryView" type="uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView" -->
<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="breadcrumbsList" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="qaChecksSubmitUrl" type="java.lang.String" -->
<#-- @ftlvariable name="canManageNomination" type="java.lang.Boolean" -->

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
>

    <@_caseProcessingHeader.caseProcessingHeader headerInformation/>

    <#assign qaChecksSlideoutPanelId = "complete-qa-checks"/>
    <#assign qaChecksSlideoutText = "Complete QA checks"/>

    <#if canManageNomination>
        <@fdsAction.buttonGroup>
            <@fdsActionDropdown.actionDropdown dropdownButtonText="Update nomination">
                <#if qaChecksSubmitUrl?has_content>
                    <@slideOutActionDropdownItem.slideOutActionDropdownItem actionText=qaChecksSlideoutText slideOutPanelId=qaChecksSlideoutPanelId/>
                </#if>
            </@fdsActionDropdown.actionDropdown>
        </@fdsAction.buttonGroup>
    </#if>

    <#if qaChecksSubmitUrl?has_content>
        <@_qaChecksSlideout.qaChecksSlideout panelId=qaChecksSlideoutPanelId headingText=qaChecksSlideoutText postUrl=qaChecksSubmitUrl/>
    </#if>

    <@fdsTabs.tabs tabsHeading="Nomination tabs">
        <@fdsTabs.tabList>
            <@fdsTabs.tab tabLabel="Nomination form" tabAnchor="nomination-form-tab"/>
        </@fdsTabs.tabList>

        <@fdsTabs.tabContent tabAnchor="nomination-form-tab">
            <h2 class="govuk-heading-l">Nomination form</h2>
            <@nominationSummary.nominationSummary summaryView=summaryView/>
        </@fdsTabs.tabContent>

    </@fdsTabs.tabs>

</@defaultPage>