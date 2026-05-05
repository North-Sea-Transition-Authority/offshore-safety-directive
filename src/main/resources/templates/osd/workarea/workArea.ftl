<#include '../layout/layout.ftl'>
<#import '_nominationWorkAreaItem.ftl' as _nominationWorkAreaItem>

<#-- @ftlvariable name="startNominationUrl" type="String" -->
<#-- @ftlvariable name="workAreaItems" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaItem>" -->

<#assign pageTitle = "Work area" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_PAGE
>
  <#if startNominationUrl?has_content>
    <@fdsAction.link
      linkText="Create nomination"
      linkUrl=springUrl(startNominationUrl)
      linkClass="govuk-button govuk-button--secondary"
    />
  </#if>

  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter oneThirdWidth=true>
      <@fdsSearch.searchFilterList
        clearFilterUrl=springUrl(clearFiltersUrl)
        clearFilterText="Reset"
        filterButtonItemText="nominations"
      >
        <@fdsSearch.searchCheckboxes path="form.nominationStatuses" checkboxes=statusMap/>
      </@fdsSearch.searchFilterList>
    </@fdsSearch.searchFilter>

    <@fdsSearch.searchPageContent twoThirdsWidth=true>
        <@fdsResultList.resultList resultCount=workAreaItems?size resultCountSuffix="nomination">
            <#list workAreaItems as workAreaItem>
                <#if workAreaItem.type() == "NOMINATION">
                    <@_nominationWorkAreaItem.nominationWorkAreaItem workAreaItem=workAreaItem/>
                </#if>
            </#list>
        </@fdsResultList.resultList>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</@defaultPage>