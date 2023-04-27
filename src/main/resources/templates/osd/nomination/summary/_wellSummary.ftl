<#import '../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList/>
<#import '../../../fds/components/insetText/insetText.ftl' as fdsInsetText/>
<#import '../well/_wellDtoLicenceDisplay.ftl' as _wellDtoLicenceDisplay>
<#import '../well/_listWellbores.ftl' as _listWellbores>

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
  <@_summaryListCardWrapper wellSummaryView=wellSummaryView>
    <@_relationToWellOperatorshipSummaryListRow wellSummaryView=wellSummaryView/>
  </@_summaryListCardWrapper>
</#macro>

<#macro _specificWellsSummary wellSummaryView>

  <#local specificWellSummaryView = wellSummaryView.specificWellSummaryView />

  <@_summaryListCardWrapper wellSummaryView=wellSummaryView>
    <@_relationToWellOperatorshipSummaryListRow wellSummaryView=wellSummaryView/>
    <@fdsSummaryList.summaryListRowNoAction keyText="Wells">
      <#if specificWellSummaryView.wells?has_content>
        <ol class="govuk-list">
          <#list specificWellSummaryView.wells as well>
            <li class="govuk-list__item">${well.name()}</li>
          </#list>
        </ol>
      </#if>
    </@fdsSummaryList.summaryListRowNoAction>
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

  <@_summaryListCardWrapper wellSummaryView=wellSummaryView>
    <@_relationToWellOperatorshipSummaryListRow wellSummaryView=wellSummaryView/>

    <@fdsSummaryList.summaryListRowNoAction keyText="Licence block subareas">
      <#if subareaWellSummaryView.licenceBlockSubareas?has_content>
        <ol class="govuk-list">
          <#list subareaWellSummaryView.licenceBlockSubareas as subarea>
            <li class="govuk-list__item govuk-!-margin-top-0">${subarea.displayName()}</li>
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
  <#if subareaWellSummaryView.licenceBlockSubareas?has_content>
    <@fdsSummaryList.summaryListCard
      summaryListId="wells-included-in-nomination-summary"
      headingText="Wells in this nomination"
    >
      <#if wellSummaryView.subareaWellsIncludedOnNomination?has_content>
          <@_listWellbores.listWellbores wellSummaryView.subareaWellsIncludedOnNomination/>
      <#else>
        <@fdsInsetText.insetText>
          None of the subareas included in this nomination contain any wells,
          or all the wells within them have been excluded
        </@fdsInsetText.insetText>
      </#if>
    </@fdsSummaryList.summaryListCard>
  </#if>
</#macro>

<#macro _summaryListCardWrapper wellSummaryView>
  <@fdsSummaryList.summaryListCard
    summaryListId=wellSummaryView.summarySectionDetail.summarySectionId().id()
    headingText=wellSummaryView.summarySectionDetail.summarySectionName().name()
    summaryListErrorMessage=(wellSummaryView.summarySectionError.errorMessage())!""
  >
    <#nested/>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro _relationToWellOperatorshipSummaryListRow wellSummaryView>
  <@fdsSummaryList.summaryListRowNoAction keyText="In relation to well operatorship">
    <#if wellSummaryView.wellSelectionType?has_content>
      ${wellSummaryView.wellSelectionType.screenDisplayText}
    </#if>
  </@fdsSummaryList.summaryListRowNoAction>
</#macro>