<#import '_wellDtoLicenceDisplay.ftl' as _wellDtoLicenceDisplay>

<#macro listWellbores wellbores>
  <table class="govuk-table">
    <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th scope="col" class="govuk-table__header">Wellbore</th>
        <th scope="col" class="govuk-table__header">Mechanical status</th>
        <th scope="col" class="govuk-table__header">WONS licences</th>
      </tr>
    </thead>
    <tbody class="govuk-table__body">
      <#list wellbores as wellbore>
        <tr class="govuk-table__row">
          <td class="govuk-table__cell">
            ${wellbore.name()}
            <#if !wellbore.isOnPortal()>
              <div>
                <strong class="govuk-tag govuk-tag--blue">Does not exist</strong>
              </div>
            </#if>
          </td>
          <td class="govuk-table__cell">
            <#if wellbore.mechanicalStatus()?has_content>
              ${wellbore.mechanicalStatus().displayName()}
            </#if>
          </td>
          <td class="govuk-table__cell">
            <#if (wellbore.originLicenceDto())?has_content || (wellbore.totalDepthLicenceDto())?has_content>
              <@_wellDtoLicenceDisplay.wellDtoLicenceDisplay
                originDto=wellbore.originLicenceDto()
                totalDepthDto=wellbore.totalDepthLicenceDto()
              />
            </#if>
          </td>
        </tr>
      </#list>
    </tbody>
  </table>
</#macro>