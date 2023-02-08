<#import '/spring.ftl' as spring>
<#import '../../../../fds/components/checkboxes/checkboxes.ftl' as fdsCheckbox>
<#import '../../../../fds/utilities/utilities.ftl' as fdsUtil>

<#macro _wellsExcludeList wellbores excludedWellsFormPath>

  <@spring.bind excludedWellsFormPath/>

  <#assign selectedWellbores = [] />

  <#list spring.stringStatusValue?split(",") as wellbore>
    <#assign selectedWellbores = selectedWellbores + [wellbore] />
  </#list>

  <table class="govuk-table">
    <thead class="govuk-table__head">
    <tr class="govuk-table__row">
      <th scope="col" class="govuk-table__header">Exclude well?</th>
      <th scope="col" class="govuk-table__header">Mechanical status</th>
      <th scope="col" class="govuk-table__header">WONS licences</th>
    </tr>
    </thead>
    <tbody class="govuk-table__body">
      <#list wellbores as wellbore>

        <#-- ensure the first item in the list matches the form field name so we can bind errors correctly -->
        <#if wellbore?index == 0>
          <#local id=fdsUtil.sanitiseId("${fdsUtil.getSpringStatusExpression()}")/>
        <#else>
          <#local id=fdsUtil.sanitiseId("${fdsUtil.getSpringStatusExpression()}-${wellbore?index}")/>
        </#if>

        <#local name=fdsUtil.getSpringStatusExpression()>

        <#-- Convert to number to avoid issues with 1000 -> 1,000 if used as string -->
        <#local wellboreId = "${wellbore.wellboreId().id()?long?c}" />

        <#assign isSelected = selectedWellbores?seq_contains("${wellboreId}")>

        <tr class="govuk-table__row">
          <td class="govuk-table__cell">
            <#--
              Wrapping divs and labels set to 100% width so any text and gaps in table can be used to check the
              checkbox. This means we don't require custom JS to do this. Each table data value is wrapped in a label
              targetting the checkbox.
            -->
            <div class="govuk-checkboxes govuk-checkboxes--small govuk-!-width-full" data-module="govuk-checkboxes">
              <div class="govuk-checkboxes__item govuk-!-width-full">
                <input
                  class="govuk-checkboxes__input"
                  id="${id}"
                  name="${name}"
                  type="checkbox"
                  value="${wellboreId}"
                  <#if isSelected>checked</#if>
                />
                <label class="govuk-label govuk-checkboxes__label govuk-!-width-full" for="${id}">
                  <span class="govuk-visually-hidden">Wellbore with registration number </span>
                  ${wellbore.name()}
                </label>
              </div>
            </div>
          </td>
          <td class="govuk-table__cell">
            <#if wellbore.mechanicalStatus()?has_content>
              <label class="govuk-checkboxes__label govuk-!-padding-left-0 govuk-!-width-full" for="${id}">
                <span class="govuk-visually-hidden"> with mechanical status </span>
                ${wellbore.mechanicalStatus().displayName()}
              </label>
            </#if>
          </td>
          <td class="govuk-table__cell">
            <#if wellbore.relatedLicences()?has_content>
              <label class="govuk-checkboxes__label govuk-!-padding-left-0 govuk-!-width-full" for="${id}">
                <span class="govuk-visually-hidden"> on licences </span>
                <#list wellbore.relatedLicences() as licence>
                  ${licence.licenceReference().value()}<#sep>, </#sep>
                </#list>
              </label>
            </#if>
          </td>
        </tr>
      </#list>
    </tbody>
  </table>
</#macro>