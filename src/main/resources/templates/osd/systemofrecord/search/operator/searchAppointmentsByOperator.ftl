<#include '../../layout.ftl'>
<#import '../_searchSystemOfRecord.ftl' as sorSearch>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="searchForm" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordSearchForm" -->
<#-- @ftlvariable name="appointedOperatorRestUrl" type="String" -->
<#-- @ftlvariable name="filteredAppointedOperator" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="appointments" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.AppointmentSearchItemDto>" -->
<#-- @ftlvariable name="hasAddedFilter" type="Boolean" -->

<@systemOfRecordPage
  pageTitle="View appointments by operator"
  pageSize=PageSize.FULL_PAGE
  backLinkUrl=springUrl(backLinkUrl)
>
  <#assign searchFilterContent>
    <@fdsSearch.searchFilterList filterButtonItemText="appointments">
      <@fdsSearch.searchFilterItem itemName="Appointed operator" expanded=true>
        <@fdsSearchSelector.searchSelectorRest
          path="searchForm.appointedOperatorId"
          restUrl=springUrl(appointedOperatorRestUrl)
          labelText=""
          selectorMinInputLength=3
          inputClass="govuk-!-width-three-quarters govuk-!-margin-bottom-0"
          formGroupClass="govuk-!-margin-bottom-0"
          preselectedItems=filteredAppointedOperator
        />
      </@fdsSearch.searchFilterItem>
    </@fdsSearch.searchFilterList>
  </#assign>

  <@sorSearch.searchSystemOfRecord
    appointments=appointments
    searchFilterContent=searchFilterContent
    hasAddedFilter=hasAddedFilter
  />
</@systemOfRecordPage>