<#import '../../../fds/components/slideOutPanel/slideOutPanel.ftl' as fdsSlideOutPanel>
<#import '../../../fds/components/form/htmlForm.ftl' as fdsForm>
<#import '../../../fds/components/button/button.ftl' as fdsAction>
<#include '../../util/url.ftl'>
<#import '../../../fds/components/error/error.ftl' as fdsError>
<#import '../../../fds/components/textarea/textarea.ftl' as fdsTextarea>
<#import '../../../fds/components/fileUpload/fileUpload.ftl' as fdsFileUpload>
<#import '../../../fds/components/fieldset/fieldset.ftl' as fdsFieldset>

<#macro consultationResponseSlideout panelId headingText postUrl postParam errorList fileUploadTemplate uploadedFiles>
    <@fdsSlideOutPanel.slideOutPanel panelId=panelId headingText=headingText>
        <@fdsError.errorSummary errorItems=errorList![]/>
        <@fdsForm.htmlForm actionUrl=springUrl(postUrl)>
            <@fdsTextarea.textarea
                path="nominationConsultationResponseForm.response.inputValue"
                labelText="Consultation response"
            />
            <@fdsFieldset.fieldset legendHeading="Consultation response documents" legendHeadingClass="govuk-fieldset__legend--s" legendHeadingSize="h2" optionalLabel=true>
                <@fdsFileUpload.fileUpload
                    id="consultation-response-file-upload"
                    path="nominationConsultationResponseForm.consultationResponseFiles"
                    formName="nominationConsultationResponseForm"
                    uploadUrl=fileUploadTemplate.uploadUrl()
                    deleteUrl=fileUploadTemplate.deleteUrl()
                    downloadUrl=fileUploadTemplate.downloadUrl()
                    allowedExtensions=fileUploadTemplate.allowedExtensions()
                    maxAllowedSize=fileUploadTemplate.maxAllowedSize()
                    showAllowedExtensions=true
                    existingFiles=uploadedFiles
                    multiFile=true
                />
            </@fdsFieldset.fieldset>
            <@fdsAction.button buttonText="Add consultation response" buttonName=postParam/>
        </@fdsForm.htmlForm>
    </@fdsSlideOutPanel.slideOutPanel>
</#macro>