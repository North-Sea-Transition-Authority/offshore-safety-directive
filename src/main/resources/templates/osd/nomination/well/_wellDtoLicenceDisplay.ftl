<#macro wellDtoLicenceDisplay originDto={} totalDepthDto={}>
    <#assign bothDtosProvided = originDto?has_content && totalDepthDto?has_content/>
    <#if bothDtosProvided>
      <#if originDto.licenceReference().value() == totalDepthDto.licenceReference().value()>
        <span>Origin and total depth licence: ${originDto.licenceReference().value()}</span>
      <#else>
        <ul class="govuk-list govuk-!-margin-bottom-0">
            <#if originDto?has_content>
              <li>Origin licence: ${originDto.licenceReference().value()}</li>
            </#if>
            <#if totalDepthDto?has_content>
              <li>Total depth licence: ${totalDepthDto.licenceReference().value()}</li>
            </#if>
        </ul>
      </#if>
    <#else>
      <#if originDto?has_content>
        <span>Origin licence: ${originDto.licenceReference().value()}</span>
      <#elseif totalDepthDto?has_content>
        <span>Total depth licence: originDto.licenceReference().value()</span>
      </#if>
    </#if>
</#macro>