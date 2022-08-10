<#import '../../fds/utilities/utilities.ftl' as fdsUtil>
<#import '/spring.ftl' as spring>

<#function getFirstFormFieldWithError formFieldList formFieldWhenNoErrors=formFieldList[0]>
  <#list formFieldList as formField>
    <@spring.bind formField />
    <#assign hasError=fdsUtil.hasSpringStatusErrors() && spring.status.errorMessages[0]?has_content>
    <#if hasError>
      <#return formField />
    </#if>
  </#list>
  <#return formFieldWhenNoErrors />
</#function>