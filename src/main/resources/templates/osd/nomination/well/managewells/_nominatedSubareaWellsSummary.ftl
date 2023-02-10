<#import '../../../../fds/components/details/details.ftl' as fdsDetails>
<#import '../../../../fds/components/insetText/insetText.ftl' as fdsInsetText>

<#-- @ftlvariable name="nominatedSubareaWellsView" type="uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsView" -->

<#macro nominatedSubareaWellsSummary nominatedSubareaWellsView>
  <h2 class="govuk-heading-m">Wells this nomination is for</h2>

  <#if nominatedSubareaWellsView.nominatedSubareaWellbores()?has_content>
    <table class="govuk-table">
      <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th scope="col" class="govuk-table__header">Wellbore</th>
        <th scope="col" class="govuk-table__header">Mechanical status</th>
        <th scope="col" class="govuk-table__header">WONS licences</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
        <#list nominatedSubareaWellsView.nominatedSubareaWellbores() as nominatedSubareaWellbore>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">
              ${nominatedSubareaWellbore.name()}
            </td>
            <td class="govuk-table__cell">
              <#if nominatedSubareaWellbore.mechanicalStatus()?has_content>
                ${nominatedSubareaWellbore.mechanicalStatus().displayName()}
              </#if>
            </td>
            <td class="govuk-table__cell">
              <#if nominatedSubareaWellbore.relatedLicences()?has_content>
                <#list nominatedSubareaWellbore.relatedLicences() as licence>
                  ${licence.licenceReference().value()}<#sep>, </#sep>
                </#list>
              </#if>
            </td>
          </tr>
        </#list>
      </tbody>
    </table>
  <#else>
    <@fdsInsetText.insetText>
      No wells are currently included on this nomination
    </@fdsInsetText.insetText>
  </#if>

  <@fdsDetails.summaryDetails summaryTitle="How are the included wells determined?">
    <p class="govuk-body">
      The origin or total depth location for a well is within the spatial area for a subareas on the nomination and
      it has not been manually excluded using the excluded wells section above.
    </p>
    <p class="govuk-body">
      The well origin and total depth location is recorded in WONS. If wells are missing, or included when they
      should not be, then check the information in WONS is correct.
    </p>
  </@fdsDetails.summaryDetails>
</#macro>