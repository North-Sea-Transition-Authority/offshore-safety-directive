<#include '../../layout.ftl'>
<#import '../_searchSystemOfRecord.ftl' as sorSearch>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="appointments" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.AppointmentSearchItemDto>" -->
<#-- @ftlvariable name="hasAddedFilter" type="Boolean" -->
<#-- @ftlvariable name="searchForm" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordSearchForm" -->
<#-- @ftlvariable name="wellboreRestUrl" type="String" -->
<#-- @ftlvariable name="filteredWellbore" type="java.util.Map<Integer, String>" -->
<#-- @ftlvariable name="licenceRestUrl" type="String" -->
<#-- @ftlvariable name="filteredLicence" type="java.util.Map<Integer, String>" -->

<@systemOfRecordPage
  pageTitle="View appointments for wells"
  pageSize=PageSize.FULL_PAGE
  backLinkUrl=springUrl(backLinkUrl)
>
  <#assign searchFilterContent>
    <@fdsSearch.searchFilterList filterButtonItemText="appointments">
      <@fdsSearch.searchFilterItem itemName="Well registration number" expanded=true>
        <@fdsSearchSelector.searchSelectorRest
          path="searchForm.wellboreId"
          restUrl=springUrl(wellboreRestUrl)
          labelText=""
          selectorMinInputLength=3
          inputClass="govuk-!-width-three-quarters govuk-!-margin-bottom-0"
          formGroupClass="govuk-!-margin-bottom-0"
          preselectedItems=filteredWellbore
        />
      </@fdsSearch.searchFilterItem>
      <@fdsSearch.searchFilterItem itemName="Licence well pursuant to" expanded=true>
        <@fdsSearchSelector.searchSelectorRest
          path="searchForm.licenceId"
          restUrl=springUrl(licenceRestUrl)
          labelText=""
          selectorMinInputLength=1
          inputClass="govuk-!-width-three-quarters govuk-!-margin-bottom-0"
          formGroupClass="govuk-!-margin-bottom-0"
          preselectedItems=filteredLicence
        />
      </@fdsSearch.searchFilterItem>
    </@fdsSearch.searchFilterList>
  </#assign>
  <@sorSearch.searchSystemOfRecord
    appointments=appointments
    hasAddedFilter=hasAddedFilter
    searchFilterContent=searchFilterContent
  />
</@systemOfRecordPage>