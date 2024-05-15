<#import '../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList/>
<#import '../../../fds/components/insetText/insetText.ftl' as fdsInsetText/>
<#import '../well/_wellDtoLicenceDisplay.ftl' as _wellDtoLicenceDisplay>
<#import '../well/_listWellbores.ftl' as _listWellbores>
<#import '../../../fds/components/details/details.ftl' as fdsDetails>
<#import '../well/_wonsContactGuidance.ftl' as _wonsContactGuidance>

<#macro wellSummary wellSummaryView>
  <#if wellSummaryView.wellSelectionType?has_content>
    <#if wellSummaryView.wellSelectionType == "NO_WELLS">
      <@_relationToWellOperatorSummary wellSummaryView=wellSummaryView/>
    <#elseif wellSummaryView.wellSelectionType == "SPECIFIC_WELLS">
      <@_specificWellsSummary wellSummaryView=wellSummaryView/>
    <#elseif wellSummaryView.wellSelectionType == "LICENCE_BLOCK_SUBAREA">
      <@_subareaWellsSummary wellSummaryView=wellSummaryView/>
    </#if>
  <#else>
    <@_relationToWellOperatorSummary wellSummaryView=wellSummaryView/>
  </#if>
</#macro>

<#macro _relationToWellOperatorSummary wellSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=wellSummaryView.summarySectionDetail.summarySectionId().id()
    headingText=wellSummaryView.summarySectionDetail.summarySectionName().name()
    summaryListErrorMessage=(wellSummaryView.summarySectionError.errorMessage())!""
  >
    <@_relationToWellOperatorshipSummaryListRow wellSummaryView=wellSummaryView/>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro _specificWellsSummary wellSummaryView>

  <#local specificWellSummaryView = wellSummaryView.specificWellSummaryView />

  <@_summaryListCardWrapper wellSummaryView=wellSummaryView wellSelectionType="SPECIFIC_WELLS">
    <@_relationToWellOperatorshipSummaryListRow wellSummaryView=wellSummaryView/>
    <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all well activity phases?">
      <#if specificWellSummaryView.isNominationForAllWellPhases?has_content>
        ${specificWellSummaryView.isNominationForAllWellPhases?then('Yes', 'No')}
      </#if>
    </@fdsSummaryList.summaryListRowNoAction>
    <#if specificWellSummaryView.isNominationForAllWellPhases?has_content && !specificWellSummaryView.isNominationForAllWellPhases>
      <@fdsSummaryList.summaryListRowNoAction keyText="Which well activity phases is this nomination for?">
        <#if specificWellSummaryView.wellPhases?has_content>
          <ol class="govuk-list">
            <#list specificWellSummaryView.wellPhases as phase>
              <li class="govuk-list__item">${phase.screenDisplayText}</li>
            </#list>
          </ol>
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
    </#if>
  </@_summaryListCardWrapper>
</#macro>

<#macro _subareaWellsSummary wellSummaryView>

  <#local subareaWellSummaryView = wellSummaryView.subareaWellSummaryView />
  <#local excludedWellSummaryView = wellSummaryView.excludedWellSummaryView />

  <@_summaryListCardWrapper wellSummaryView=wellSummaryView wellSelectionType="LICENCE_BLOCK_SUBAREA">
    <@_relationToWellOperatorshipSummaryListRow wellSummaryView=wellSummaryView/>

    <@fdsSummaryList.summaryListRowNoAction keyText="Licence block subareas">
      <#if subareaWellSummaryView.licenceBlockSubareas?has_content>
        <ol class="govuk-list">
          <#list subareaWellSummaryView.licenceBlockSubareas as subarea>
            <li class="govuk-list__item govuk-!-margin-top-0">
              <#if subarea.isExtant()>
                ${subarea.displayName()}
              <#else>
                  ${subarea.subareaName().value()}
                <div>
                  <strong class="govuk-tag govuk-tag--blue">No longer exists</strong>
                </div>
              </#if>
            </li>
          </#list>
        </ol>
      </#if>
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction
      keyText="Will this nomination cover future wells that may be drilled in the selected subareas?"
    >
      <#if subareaWellSummaryView.validForFutureWellsInSubarea?has_content>
        ${subareaWellSummaryView.validForFutureWellsInSubarea?then('Yes', 'No')}
      </#if>
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Is this nomination for all well activity phases?">
      <#if subareaWellSummaryView.forAllWellPhases?has_content>
        ${subareaWellSummaryView.forAllWellPhases?then('Yes', 'No')}
      </#if>
    </@fdsSummaryList.summaryListRowNoAction>
    <#if subareaWellSummaryView.forAllWellPhases?has_content && !subareaWellSummaryView.forAllWellPhases>
      <@fdsSummaryList.summaryListRowNoAction keyText="Which well activity phases is this nomination for?">
        <#if subareaWellSummaryView.wellPhases?has_content>
          <ol class="govuk-list">
            <#list subareaWellSummaryView.wellPhases as phase>
              <li class="govuk-list__item">${phase.screenDisplayText}</li>
            </#list>
          </ol>
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
    </#if>
    <@fdsSummaryList.summaryListRowNoAction keyText="Are any wells to be excluded from this nomination?">
      <#if excludedWellSummaryView.hasWellsToExclude()?has_content>
        ${excludedWellSummaryView.hasWellsToExclude()?then('Yes', 'No')}
      </#if>
    </@fdsSummaryList.summaryListRowNoAction>
    <#if excludedWellSummaryView.hasWellsToExclude()?has_content && excludedWellSummaryView.hasWellsToExclude()>
      <@fdsSummaryList.summaryListRowNoAction keyText="Excluded wells">
        <#if excludedWellSummaryView.hasWellsToExclude()?has_content>
          <ol class="govuk-list">
            <#list excludedWellSummaryView.excludedWells() as excludedWellRegistrationNumber>
              <li class="govuk-list__item">${excludedWellRegistrationNumber.value()}</li>
            </#list>
          </ol>
        </#if>
      </@fdsSummaryList.summaryListRowNoAction>
    </#if>
  </@_summaryListCardWrapper>
</#macro>

<#macro _summaryListCardWrapper wellSummaryView wellSelectionType>
  <#assign summaryListErrorMessage=(wellSummaryView.summarySectionError.errorMessage())!""/>
  <#assign summaryListId=wellSummaryView.summarySectionDetail.summarySectionId().id()/>

  <#assign tableContent>
    <#if wellSelectionType=="SPECIFIC_WELLS" && wellSummaryView.specificWellSummaryView.wells?has_content>
      <@_listWellbores.listWellbores wellSummaryView.specificWellSummaryView.wells/>
      <@_wonsContactGuidance.wonsContactGuidance detailsClass="govuk-!-margin-bottom-0"/>
    </#if>

    <#if wellSelectionType=="LICENCE_BLOCK_SUBAREA" && wellSummaryView.subareaWellSummaryView.licenceBlockSubareas?has_content>
      <#if wellSummaryView.subareaWellsIncludedOnNomination?has_content>
        <@_listWellbores.listWellbores wellSummaryView.subareaWellsIncludedOnNomination/>
      <#else>
        <@fdsInsetText.insetText>
          None of the subareas included in this nomination contain any wells,
          or all the wells within them have been excluded
        </@fdsInsetText.insetText>
      </#if>
      <@_wonsContactGuidance.wonsContactGuidance detailsClass="govuk-!-margin-bottom-0"/>
    </#if>
  </#assign>
  <div class="fds-summary-list-card<#if summaryListErrorMessage?has_content> fds-summary-list-card--error</#if>" id="${summaryListId}">
    <div class="fds-summary-list-card__heading-wrapper">
      <h2 class="fds-summary-list-card__heading">Wells</h2>
    </div>
    <div class="fds-summary-list-card__content">
      <#if summaryListErrorMessage?has_content>
        <p class="govuk-error-message fds-summary-list__error-message">
          <span class="govuk-visually-hidden">Error:</span> ${summaryListErrorMessage}<br/>
        </p>
      </#if>
      <dl class="govuk-summary-list">
        <#nested>
      </dl>
      <h2 class="govuk-heading-m govuk-!-margin-top-2">Wells this nomination is for</h2>
      ${tableContent}
    </div>
  </div>
</#macro>

<#macro _relationToWellOperatorshipSummaryListRow wellSummaryView>
  <@fdsSummaryList.summaryListRowNoAction keyText="In relation to well operatorship">
    <#if wellSummaryView.wellSelectionType?has_content>
      ${wellSummaryView.wellSelectionType.screenDisplayText}
    </#if>
  </@fdsSummaryList.summaryListRowNoAction>
</#macro>