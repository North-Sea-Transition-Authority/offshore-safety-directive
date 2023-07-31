<#include '../../../layout/layout.ftl'>
<#import '../_searchSystemOfRecord.ftl' as sorSearch>

<#-- @ftlvariable name="backLinkUrl" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="appointments" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.AppointmentSearchItemDto>" -->
<#-- @ftlvariable name="hasAddedFilter" type="Boolean" -->
<#-- @ftlvariable name="searchForm" type="uk.co.nstauthority.offshoresafetydirective.systemofrecord.search.SystemOfRecordSearchForm" -->
<#-- @ftlvariable name="subareaRestUrl" type="String" -->
<#-- @ftlvariable name="filteredSubarea" type="java.util.Map<String, String>" -->

<#assign pageTitle = "View appointments for forward area approvals" />

<@defaultPage
  pageHeading=pageTitle
  errorItems=[]
  pageSize=PageSize.FULL_PAGE
  backLinkUrl=springUrl(backLinkUrl)
  showNavigationItems=(loggedInUser?has_content)
  allowSearchEngineIndexing=false
>
  <#assign searchFilterContent>
    <@fdsSearch.searchFilterList filterButtonItemText="appointments">
      <@fdsSearch.searchFilterItem itemName="Subarea" expanded=searchForm.subareaId?has_content>
        <@fdsSearchSelector.searchSelectorRest
          path="searchForm.subareaId"
          restUrl=springUrl(subareaRestUrl)
          labelText=""
          selectorMinInputLength=3
          inputClass="govuk-!-width-three-quarters govuk-!-margin-bottom-0"
          formGroupClass="govuk-!-margin-bottom-0"
          preselectedItems=filteredSubarea
        />
      </@fdsSearch.searchFilterItem>
    </@fdsSearch.searchFilterList>
  </#assign>
  <@sorSearch.searchSystemOfRecord
    appointments=appointments
    hasAddedFilter=hasAddedFilter
    searchFilterContent=searchFilterContent
  />
</@defaultPage>