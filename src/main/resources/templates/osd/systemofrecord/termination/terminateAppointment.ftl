<#include '../../layout/layout.ftl'>
<#import '_appointmentDetailSummary.ftl' as _appointmentDetailSummary/>

<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="assetName" type="String" -->
<#-- @ftlvariable name="phases" type="java.util.List<uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAppointmentPhase>" -->

<@defaultPage
    pageHeading="Are you sure you want to terminate this appointment for ${assetName}?"
    pageSize=PageSize.TWO_THIRDS_COLUMN
    errorItems=[]
    backLinkWithBrowserBack=true
    showNavigationItems=(loggedInUser?has_content)
    allowSearchEngineIndexing=false
>
    <@_appointmentDetailSummary.appointmentDetailSummary
        appointedOperator="${appointedOperator}"
        responsibleFromDate="${responsibleFromDate}"
        phases=phases
        createdBy="${createdBy}"
    />

</@defaultPage>