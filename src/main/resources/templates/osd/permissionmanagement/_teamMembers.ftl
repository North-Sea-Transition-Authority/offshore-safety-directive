<#import '../macros/mailTo.ftl' as mailTo>

<#macro teamMembers name members>

    <table class="govuk-table">
      <caption class="govuk-table__caption govuk-table__caption--m govuk-visually-hidden">Members of ${name}</caption>
      <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th scope="col" class="govuk-table__header">Name</th>
          <th scope="col" class="govuk-table__header">Contact details</th>
          <th scope="col" class="govuk-table__header">Roles</th>
        </tr>
      </thead>
      <tbody class="govuk-table__body">
        <#list members as member>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${member.displayName}</td>
            <td class="govuk-table__cell">
              <ul class="govuk-list govuk-!-margin-bottom-0">
                <#if member.contactEmail()?has_content>
                  <li>
                    <@mailTo.mailToLink mailToEmailAddress=member.contactEmail() />
                  </li>
                </#if>
                <#if member.contactNumber()?has_content>
                  <li>${member.contactNumber()}</li>
                </#if>
              </ul>
            </td>
            <td class="govuk-table__cell">
              <ul class="govuk-list govuk-!-margin-bottom-0">
                <#list member.teamRoles() as role>
                    <li>${role.displayText}</li>
                </#list>
              </ul>
            </td>
          </tr>
        </#list>
      </tbody>
    </table>

</#macro>