<#import '../../../fds/objects/layouts/generic.ftl' as fdsGeneric>
<#import '../../../fds/components/button/button.ftl' as fdsButton>

<#macro _fileDisplay file>
    <@fdsButton.link
      linkText=file.uploadedFileView().fileName()
      linkUrl=fdsGeneric.springUrl(file.downloadUrl())
      linkClass="govuk-link govuk-!-font-size-19"
      linkScreenReaderText="Download"
      ariaDescribedBy="${file.uploadedFileView().fileId()}-description"
    />
    <span> - ${file.uploadedFileView().fileSize()}</span>
    <p
      id="${file.uploadedFileView().fileId()}-description"
      class="govuk-body">
        ${file.uploadedFileView().fileDescription()}
    </p>
</#macro>

<#macro fileSummary fileSummaryViews>
    <#if fileSummaryViews?size gt 1>
      <ul class="govuk-list">
          <#list fileSummaryViews as file>
            <li class="govuk-!-margin-top-2">
                <@_fileDisplay file/>
            </li>
          </#list>
      </ul>
    <#else>
        <#list fileSummaryViews as file>
          <div class="govuk-body">
              <@_fileDisplay file/>
          </div>
        </#list>
    </#if>
</#macro>