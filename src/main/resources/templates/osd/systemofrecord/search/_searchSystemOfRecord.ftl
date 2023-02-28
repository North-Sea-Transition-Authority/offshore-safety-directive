<#import '../../../fds/patterns/search/search.ftl' as fdsSearch>
<#import '../../../fds/components/resultList/resultList.ftl' as fdsResultList>

<#macro searchSystemOfRecord resultCount appointments searchFilterContent="">
  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter>${searchFilterContent}</@fdsSearch.searchFilter>
    <@fdsSearch.searchPageContent>
      <@fdsResultList.resultList
        resultCount=resultCount
        resultCountSuffix="appointment"
      >
        <#list appointments as appointment>
          <@fdsResultList.resultListItem linkHeadingUrl="#" linkHeadingText=_assetName(appointment)>
            <@fdsResultList.resultListDataItem>
              <@fdsResultList.resultListDataValue key="Appointed operator" value=_appointedOperatorName(appointment)/>
              <@fdsResultList.resultListDataValue key="Appointment date" value=appointment.displayableAppointmentDate()!""/>
              <@fdsResultList.resultListDataValue key="Type of appointment" value=_appointmentType(appointment)/>
            </@fdsResultList.resultListDataItem>
          </@fdsResultList.resultListItem>
        </#list>
      </@fdsResultList.resultList>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
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