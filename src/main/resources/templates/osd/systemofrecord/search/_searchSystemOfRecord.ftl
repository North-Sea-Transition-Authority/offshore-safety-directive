<#import '../../../fds/patterns/search/search.ftl' as fdsSearch>
<#import '../../../fds/components/insetText/insetText.ftl' as fdsInsetText>
<#import '../../../fds/components/resultList/resultList.ftl' as fdsResultList>
<#import '../../util/url.ftl' as url>

<#macro searchSystemOfRecord appointments hasAddedFilter=false searchFilterContent="">
  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter oneThirdWidth=true>
      ${searchFilterContent}
    </@fdsSearch.searchFilter>
    <@fdsSearch.searchPageContent twoThirdsWidth=true>
      <#if hasAddedFilter>
        <#if appointments?has_content>
          <@_appointments appointments=appointments/>
        <#else>
          <@_noMatchingAppointments/>
        </#if>
      <#else>
        <@_noFiltersAdded/>
      </#if>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</#macro>

<#macro _appointments appointments>
  <@fdsResultList.resultList
    resultCount=appointments?size
    resultCountSuffix="appointment"
  >
    <#list appointments as appointment>
      <@fdsResultList.resultListItem
        linkHeadingUrl=url.springUrl(appointment.timelineUrl())
        linkHeadingText=_assetName(appointment)
      >
        <@fdsResultList.resultListDataItem>
          <@fdsResultList.resultListDataValue key="Appointed operator" value=_appointedOperatorName(appointment)/>
          <@fdsResultList.resultListDataValue key="Appointment date" value=appointment.displayableAppointmentDate()!""/>
          <@fdsResultList.resultListDataValue key="Type of appointment" value=_appointmentType(appointment)/>
        </@fdsResultList.resultListDataItem>
      </@fdsResultList.resultListItem>
    </#list>
  </@fdsResultList.resultList>
</#macro>

<#macro _noMatchingAppointments>
  <h3 class="govuk-heading-s">There are no matching appointments</h3>
  <p class="govuk-body">Improve your results by:</p>
  <ul class="govuk-list govuk-list--bullet">
    <li>removing filters</li>
    <li>searching for something less specific</li>
  </ul>
</#macro>

<#macro _noFiltersAdded>
  <@fdsInsetText.insetText>
    To search, add some filters and click the 'Filter appointments' button.
  </@fdsInsetText.insetText>
</#macro>

<#function _assetName appointment>
  <#assign assetName>
    <#if appointment.assetName()?has_content>
      ${appointment.assetName().value()}
    </#if>
  </#assign>
  <#return assetName!""/>
</#function>

<#function _appointmentType appointment>
  <#assign appointmentType>
    <#if appointment.appointmentType()?has_content>
      ${appointment.appointmentType().displayName()}
    </#if>
  </#assign>
  <#return appointmentType!""/>
</#function>

<#function _appointedOperatorName appointment>
  <#assign appointedOperatorName>
    <#if appointment.appointedOperatorName()?has_content>
      ${appointment.appointedOperatorName().value()}
    </#if>
  </#assign>
  <#return appointedOperatorName!""/>
</#function>