package fr.dossierfacile.common.service.interfaces;

import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.enums.LogType;
import fr.dossierfacile.common.model.EditionType;

public interface LogService {

    void saveLog(LogType logType, Long tenantId);

    void saveLogWithTenantData(LogType logType, Tenant tenant);

    void saveDocumentEditedLog(Document document, Tenant editor, EditionType editionType);

    void savePartnerAccessRevocationLog(Tenant tenant, UserApi userApi);

}
