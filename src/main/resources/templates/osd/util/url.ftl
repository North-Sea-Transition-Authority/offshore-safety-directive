<#import '/spring.ftl' as spring>

<#function springUrl url>
  <#local springUrl>
    <@spring.url url/>
  </#local>
  <#return springUrl>
</#function>