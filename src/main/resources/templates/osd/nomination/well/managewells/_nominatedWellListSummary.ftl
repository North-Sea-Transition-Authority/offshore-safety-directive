<#import '../../../../fds/components/details/details.ftl' as fdsDetails>
<#import '../../../../fds/components/insetText/insetText.ftl' as fdsInsetText>
<#import '../_wellDtoLicenceDisplay.ftl' as _wellDtoLicenceDisplay>
<#import '../_listWellbores.ftl' as _listWellbores>
<#import '_wonsContactGuidance.ftl' as _wonsContactGuidance>

<#-- @ftlvariable name="nominatedWellsView" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto>" -->

<#macro nominatedWellListSummary wellSelectionType nominatedWellsView>
  <h2 class="govuk-heading-m">Wells this nomination is for</h2>

  <#if nominatedWellsView?has_content>
    <@_listWellbores.listWellbores nominatedWellsView/>
  <#else>
    <@fdsInsetText.insetText>
      No wells are currently included on this nomination
    </@fdsInsetText.insetText>
  </#if>

  <#if wellSelectionType?has_content && wellSelectionType == "LICENCE_BLOCK_SUBAREA">
    <@fdsDetails.summaryDetails summaryTitle="How are the included wells determined?">
      <p class="govuk-body">
        A well is included if its origin or total depth (TD) location for a well is within the spatial area for a
        subareas on the nomination, and it has not been manually excluded using the excluded wells section above.
      </p>
      <p class="govuk-body">
        The well origin and total depth location is recorded in WONS. If wells are missing, or included when they
        should not be, then check the information in WONS is correct.
      </p>
    </@fdsDetails.summaryDetails>
  </#if>
  <#if wellSelectionType?has_content && wellSelectionType == "SPECIFIC_WELLS">
    <@_wonsContactGuidance.wonsContactGuidance/>
  </#if>
</#macro>