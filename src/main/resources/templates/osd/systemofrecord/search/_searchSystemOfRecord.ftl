<#import '../../../fds/patterns/search/search.ftl' as fdsSearch>
<#import '../../../fds/components/resultList/resultList.ftl' as fdsResultList>

<#macro searchSystemOfRecord resultCount>
  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter>
      <#nested/>
    </@fdsSearch.searchFilter>
    <@fdsSearch.searchPageContent>
      <@fdsResultList.resultList resultCount=resultCount resultCountSuffix="appointments"/>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</#macro>