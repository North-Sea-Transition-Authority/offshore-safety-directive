<#include '../layout/layout.ftl'/>

<#macro mailToLink mailToEmailAddress linkText="" linkClass="govuk-link" linkScreenReaderText="" subjectText="">
  <@fdsAction.link
    linkText=linkText?has_content?then(linkText, mailToEmailAddress)
    linkUrl="mailto:${mailToEmailAddress}?subject=${subjectText}"
    linkClass=linkClass
    linkScreenReaderText=linkScreenReaderText
  />
</#macro>