<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="pageTitle" type="String" -->
<#-- @ftlvariable name="workAreaLink" type="String" -->
<#-- @ftlvariable name="nominationReference" type="String" -->
<#-- @ftlvariable name="nominationManagementLink" type="String" -->
<#-- @ftlvariable name="feedbackUrl" type="String" -->

<#assign nominationSubmittedText="Nomination submitted"/>

<@defaultPage
  pageHeading=""
  htmlTitle=nominationSubmittedText
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(nominationManagementLink)
>
 <@fdsPanel.panel panelTitle=nominationSubmittedText panelText="Your reference number" panelRef=nominationReference/>
  <h2 class="govuk-heading-m">
    What happens next
  </h2>
  <p class="govuk-body">
    Your nomination has been sent to the licensing authority to review.
  </p>
  <p class="govuk-body">
    The appointment cannot take effect until the licensing authority has confirmed they have no objection or 3 months
    have elapsed and the licensing authority has not objected to the appointment.
  </p>
  <p class="govuk-body">
    The licensing authority will contact you if your nomination or supporting information requires any discussion or clarification.
  </p>
  <p class="govuk-body">
    <@fdsAction.link
      linkClass="govuk-link"
      linkText="What did you think of this service?"
      linkUrl=springUrl(feedbackUrl)/>
    (takes 30 seconds)
  </p>
  <p class="govuk-body">
    <@fdsAction.link linkText="Back to work area" linkUrl=springUrl(workAreaLink)/>
  </p>
</@defaultPage>