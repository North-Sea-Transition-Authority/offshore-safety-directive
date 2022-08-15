<#import '_installationInclusionSummary.ftl' as installationInclusionSummary>
<#import '_nominatedInstallationDetailSummary.ftl' as nominatedInstallationDetailSummary>

<#-- @ftlvariable name="installationInclusionView" type="uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionView" -->

<#macro installationNominationSummary
  installationInclusionView
  nominatedInstallationDetailView
  installationInclusionChangeUrl=""
  nominatedInstallationDetailChangeUrl=""
>
  <@installationInclusionSummary.installationInclusionSummary
  installationInclusionView=installationInclusionView
    changeUrl=installationInclusionChangeUrl
  />
  <#if installationInclusionView.includeInstallationsInNomination?has_content && installationInclusionView.includeInstallationsInNomination>
    <@nominatedInstallationDetailSummary.nominatedInstallationDetailSummary
      nominatedInstallationDetailView=nominatedInstallationDetailView
      changeUrl=nominatedInstallationDetailChangeUrl
    />
  </#if>
</#macro>