package fr.gouv.bo.controller;

import com.google.gson.Gson;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.Guarantor;
import fr.dossierfacile.common.entity.Message;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.User;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.enums.DocumentStatus;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.enums.MessageStatus;
import fr.dossierfacile.common.enums.PartnerCallBackType;
import fr.dossierfacile.common.enums.TenantFileStatus;
import fr.gouv.bo.dto.CustomMessage;
import fr.gouv.bo.dto.EmailDTO;
import fr.gouv.bo.dto.GuarantorItem;
import fr.gouv.bo.dto.ItemDetail;
import fr.gouv.bo.dto.MessageDTO;
import fr.gouv.bo.dto.MessageItem;
import fr.gouv.bo.dto.MessageItems;
import fr.gouv.bo.dto.PartnerDTO;
import fr.gouv.bo.service.ApartmentSharingService;
import fr.gouv.bo.service.DocumentService;
import fr.gouv.bo.service.GuarantorService;
import fr.gouv.bo.service.MessageService;
import fr.gouv.bo.service.PartnerCallBackService;
import fr.gouv.bo.service.TenantService;
import fr.gouv.bo.service.TenantUserApiService;
import fr.gouv.bo.service.UserApiService;
import fr.gouv.bo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@Controller
@RequestMapping(value = "/bo/tenant")
@Slf4j
public class BOTenantController {

    private static final String EMAIL = "email";
    private static final String TENANT = "tenant";
    private static final String NEW_MESSAGE = "newMessage";
    private static final String PARTNER_LIST = "partnerList";
    private static final String REDIRECT_BO = "redirect:/bo";
    private static final String CUSTOM_MESSAGE = "customMessage";
    private static final String REDIRECT_ERROR = "redirect:/error";

    private final TenantService tenantService;
    private final MessageService messageService;
    private final GuarantorService guarantorService;
    private final DocumentService documentService;
    private final UserApiService userApiService;
    private final TenantUserApiService tenantUserApiService;
    private final PartnerCallBackService partnerCallBackService;
    private final UserService userService;
    private final ApartmentSharingService apartmentSharingService;

    @Value("${bo.message-tenant.location}")
    String locationMessageTenant;
    @Value("${bo.message-guarantor.location}")
    String locationMessageGuarantor;

    @GetMapping("/deleteCoTenant/{id}")
    public String deleteCoTenant(@PathVariable Long id) {
        Tenant create = tenantService.findTenantCreate(tenantService.findTenantById(id).getApartmentSharing().getId());
        if (create.getId().equals(id)) {
            Tenant mainTenant = userService.deleteAndReplaceTenantCreate(create);
            return "redirect:/bo/colocation/" + mainTenant.getApartmentSharing().getId() + "#tenant" + mainTenant.getId();
        }

        userService.deleteCoTenant(create, id);
        return "redirect:/bo/colocation/" + create.getApartmentSharing().getId() + "#tenant" + create.getId();
    }

    @GetMapping("/deleteApartmentSharing/{id}")
    public String deleteApartmentSharing(@PathVariable("id") Long id) {
        Tenant create = tenantService.findTenantById(id);
        userService.deleteApartmentSharing(create);
        return REDIRECT_BO;
    }

    @GetMapping("/partner/{id}")
    public String addNewPartnerInfo(@PathVariable("id") Long id, PartnerDTO partnerDTO) {

        Tenant tenant = tenantService.find(id);

        UserApi result = userApiService.findById(partnerDTO.getPartner());
        tenantUserApiService.getTenantUserApi(tenant, partnerDTO.getPartner(), partnerDTO.getInternalPartnerId());
        partnerCallBackService.sendCallBack(tenant, result, tenant.getStatus() == TenantFileStatus.VALIDATED ?
                PartnerCallBackType.VERIFIED_ACCOUNT :
                PartnerCallBackType.CREATED_ACCOUNT);

        return "redirect:/bo/colocation/" + tenant.getApartmentSharing().getId();
    }

    @GetMapping("/{id}/showResult")
    public String showResult(Model model, @PathVariable("id") Long id) {
        Tenant tenant = tenantService.find(id);
        model.addAttribute(TENANT, tenant);
        return "include/process-files-result:: process-files-result";
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<Void> validateTenantFile(@PathVariable("id") Long tenantId, Principal principal) {
        tenantService.validateTenantFile(principal, tenantId);
        return ok().build();
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> declineTenantFile(@PathVariable("id") Long tenantId, Principal principal) {
        tenantService.declineTenant(principal, tenantId);
        return ok().build();
    }

    @GetMapping("/{id}/customMessage")
    public String customEmailForm(@PathVariable("id") Long id, Model model) throws IOException {
        Tenant tenant = tenantService.find(id);
        if (tenant == null) {
            log.error("BOTenantController customEmailForm not found tenant with id {}", id);
            return REDIRECT_ERROR;
        }

        model.addAttribute("customMessage", getCustomMessage(tenant));
        model.addAttribute(TENANT, tenant);
        return "bo/tenant-custom-message-form";
    }

    @PostMapping("/{id}/customMessage")
    public String customEmail(@PathVariable("id") Long tenantId, CustomMessage customMessage, Principal principal) {
        return tenantService.customMessage(principal, tenantId, customMessage);
    }

    @GetMapping("/delete/document/{id}")
    public String deleteDocument(@PathVariable("id") Long id) {
        Tenant tenant = documentService.deleteDocument(id);
        apartmentSharingService.resetDossierPdfGenerated(tenant.getApartmentSharing());
        tenantService.updateTenantStatus(tenant);
        return "redirect:/bo/colocation/" + tenant.getApartmentSharing().getId() + "#tenant" + tenant.getId();
    }

    @GetMapping("/status/{id}")
    public String changeStatusOfDocument(@PathVariable("id") Long id, MessageDTO messageDTO) {
        Tenant tenant = documentService.changeStatusOfDocument(id, messageDTO);
        apartmentSharingService.resetDossierPdfGenerated(tenant.getApartmentSharing());
        tenantService.updateTenantStatus(tenant);
        return "redirect:/bo/colocation/" + tenant.getApartmentSharing().getId() + "#tenant" + tenant.getId();
    }

    @GetMapping("/{id}/processFile")
    public String processFileForm(Model model, @PathVariable("id") Long id) throws IOException {
        Tenant tenant = tenantService.find(id);

        if (tenant == null) {
            log.error("BOTenantController processFile not found tenant with id : {}", id);
            return REDIRECT_ERROR;
        }
        EmailDTO emailDTO = new EmailDTO();
        model.addAttribute(EMAIL, emailDTO);
        model.addAttribute(PARTNER_LIST, getPartnersListTenant(id));
        model.addAttribute(NEW_MESSAGE, findNewMessageFromTenant(id));
        model.addAttribute(TENANT, tenant);
        model.addAttribute(CUSTOM_MESSAGE, getCustomMessage(tenant));
        return "bo/process-file";
    }

    @GetMapping("/delete/guarantor/{guarantorId}")
    public String deleteGuarantor(@PathVariable("guarantorId") Long guarantorId) {
        Tenant tenant = guarantorService.deleteById(guarantorId);
        apartmentSharingService.resetDossierPdfGenerated(tenant.getApartmentSharing());
        tenantService.updateTenantStatus(tenant);
        return "redirect:/bo/colocation/" + tenant.getApartmentSharing().getId() + "#tenant" + tenant.getId();
    }

    private List<String> getPartnersListTenant(Long id) {
        return userApiService.getNamesOfPartnerByTenantId(id);
    }

    private Boolean findNewMessageFromTenant(Long id) {

        User tenant1 = tenantService.getUserById(id);
        List<Message> messages = messageService.findTenantMessages(tenant1);
        for (Message message : messages) {
            if (message.getMessageStatus().equals(MessageStatus.UNREAD) && message.getFromUser() != null)
                return true;
        }
        return false;
    }

    @PostMapping("/{id}/processFile")
    public String processFile(@PathVariable("id") Long id, CustomMessage customMessage, Principal principal) {
        tenantService.processFile(id, customMessage, principal);
        tenantService.updateOperatorDateTimeTenant(id);
        return tenantService.redirectToApplication(principal, null);
    }

    private List<ItemDetail> getItemDetailForSubcategoryOfDocument(DocumentSubCategory documentSubCategory, Boolean typeMessage) throws IOException {

        List<ItemDetail> itemDetails = new ArrayList<>();
        Gson gson = new Gson();
        String route;

        if (Boolean.TRUE.equals(typeMessage)) {
            route = locationMessageTenant;
        } else route = locationMessageGuarantor;

        ClassPathResource classPathResource = new ClassPathResource(route);

        byte[] bdata = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());

        String result = new String(bdata, StandardCharsets.UTF_8);

        MessageItems messageItems = gson.fromJson(result, MessageItems.class);

        List<String> checklist = messageItems.getCheckBoxValues().get(documentSubCategory.toString());
        checklist.forEach(check -> {
            ItemDetail itemDetail1 = ItemDetail.builder().check(false).message(check).build();
            itemDetails.add(itemDetail1);
        });

        return itemDetails;
    }

    private CustomMessage getCustomMessage(Tenant tenant) throws IOException {

        CustomMessage customMessage = new CustomMessage();

        List<Document> documents = tenant.getDocuments();
        documents.sort(Comparator.comparing(Document::getDocumentCategory));
        for (Document document : documents) {
            if (document.getDocumentStatus().equals(DocumentStatus.TO_PROCESS)) {
                customMessage.getMessageItems().add(MessageItem.builder()
                        .monthlySum(document.getMonthlySum())
                        .customTex(document.getCustomText())
                        .taxDocument(document.getTaxProcessResult())
                        .documentCategory(document.getDocumentCategory())
                        .documentSubCategory(document.getDocumentSubCategory())
                        .itemDetailList(getItemDetailForSubcategoryOfDocument(document.getDocumentSubCategory(), true))
                        .documentId(document.getId())
                        .documentName(document.getName())
                        .build());
            }
        }

        for (Guarantor guarantor : tenant.getGuarantors()) {
            GuarantorItem guarantorItem = GuarantorItem.builder()
                    .guarantorId(guarantor.getId())
                    .typeGuarantor(guarantor.getTypeGuarantor())
                    .firstName(guarantor.getFirstName())
                    .lastName(guarantor.getLastName())
                    .legalPersonName(guarantor.getLegalPersonName())
                    .build();

            documents = guarantor.getDocuments();
            documents.sort(Comparator.comparing(Document::getDocumentCategory));
            for (Document document : documents) {
                if (document.getDocumentStatus().equals(DocumentStatus.TO_PROCESS)) {
                    guarantorItem.getMessageItems().add(MessageItem.builder()
                            .monthlySum(document.getMonthlySum())
                            .customTex(document.getCustomText())
                            .taxDocument(document.getTaxProcessResult())
                            .documentCategory(document.getDocumentCategory())
                            .documentSubCategory(document.getDocumentSubCategory())
                            .itemDetailList(getItemDetailForSubcategoryOfDocument(document.getDocumentSubCategory(), false))
                            .documentId(document.getId())
                            .documentName(document.getName())
                            .build());
                }
            }
            customMessage.getGuarantorItems().add(guarantorItem);
        }
        return customMessage;
    }
}
