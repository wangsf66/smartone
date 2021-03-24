package com.smartone.ddm.resource.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.SessionFactoryContainer;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.douglei.orm.mapping.handler.MappingHandleException;
import com.douglei.orm.mapping.handler.MappingHandler;
import com.douglei.orm.mapping.handler.entity.AddOrCoverMappingEntity;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.DmResourceMapping;
import com.smartone.ddm.resource.entity.ResourceAdapters;
import com.smartone.ddm.util.ProcedurePrefixUtil;
import com.smartone.ddm.util.ProcedureSqlType;
import com.smartone.ddm.util.ResourceTypeUtil;
import com.smartone.ex.mapping.ExMappingTypeConstants;

@TransactionComponent
public class ResourceMappingService extends BasicService {
	private static final Logger logger = LoggerFactory.getLogger(ResourceMappingService.class);

	@Transaction
	public void load(List<ResourceAdapters> resourceList) {
		logger.info("在线接收发布的资源的个数: {}", resourceList.size());
		for (ResourceAdapters resource : resourceList) {
			 montageList(resource);
		}
	}	

	private void montageList(ResourceAdapters resource) {
		DmResourceMapping dmResourceMapping = null;
		DmResourceMapping createDmResourceMapping = null;
		SessionContext.getSqlSession()
				.executeUpdate("delete from DM_RESOURCE_MAPPING where RESOURCE_ID='" + resource.getResourceId() + "'");
		MappingHandler handler = SessionFactoryContainer.getSingleton().get().getMappingHandler();
		try {
			if (resource.getSqlMap() != null) {
				String procedureXml = null;
				String createprocedureXml = null;
				for (Map.Entry<ProcedureSqlType, Object> entry : resource.getSqlMap().entrySet()) {
					if (entry.getKey().equals(ProcedureSqlType.CREATE)) {
						createprocedureXml = (String) entry.getValue();
					} else if (entry.getKey().equals(ProcedureSqlType.CALL)) {
						procedureXml = (String) entry.getValue();
					}
				}
				handler.execute(new AddOrCoverMappingEntity(procedureXml, MappingTypeNameConstants.SQL).enableProperty(),
						new AddOrCoverMappingEntity(createprocedureXml, MappingTypeNameConstants.PROCEDURE).enableProperty());
				dmResourceMapping = new DmResourceMapping(resource.getResourceId(),ProcedurePrefixUtil.CALL+resource.getName(),procedureXml.replaceAll("oldName=\"([^\"]*)\"", ""),ResourceTypeUtil.RESOURCE_TYPE_SQL);
				SessionContext.getTableSession().save(setBasicPropertyValues(dmResourceMapping,true));
				createDmResourceMapping = new DmResourceMapping(resource.getResourceId(),resource.getName(),createprocedureXml.replaceAll("oldName=\"([^\"]*)\"", ""),ResourceTypeUtil.RESOURCE_TYPE_PROCEDURE);
				SessionContext.getTableSession().save(setBasicPropertyValues(createDmResourceMapping,true));
			} else {
				if (resource.getType().equals(MappingTypeNameConstants.TABLE)) {
					handler.execute(new AddOrCoverMappingEntity(resource.getContent(), MappingTypeNameConstants.TABLE).enableProperty());
					dmResourceMapping = new DmResourceMapping(resource.getResourceId(),resource.getName(),resource.getContent().replaceAll("oldName=\"([^\"]*)\"", ""),ResourceTypeUtil.RESOURCE_TYPE_TABLE);
				} else if (resource.getType().equals(MappingTypeNameConstants.SQL)) {
					handler.execute(new AddOrCoverMappingEntity(resource.getContent(), MappingTypeNameConstants.SQL).enableProperty());
					dmResourceMapping = new DmResourceMapping(resource.getResourceId(),resource.getName(),resource.getContent().replaceAll("oldName=\"([^\"]*)\"", ""),ResourceTypeUtil.RESOURCE_TYPE_SQL);
				} else if (resource.getType().equals(ExMappingTypeConstants.BUSIMODEL)) {
					handler.execute(new AddOrCoverMappingEntity(resource.getContent(),ExMappingTypeConstants.BUSIMODEL).enableProperty());
					dmResourceMapping = new DmResourceMapping(resource.getResourceId(),resource.getName(),resource.getContent().replaceAll("oldName=\"([^\"]*)\"", ""),ResourceTypeUtil.RESOURCE_TYPE_BUSIMODEL);
				}
				SessionContext.getTableSession().save(setBasicPropertyValues(dmResourceMapping,true));
			}
			ResponseContext.addData(resource.getResourceId());
		} catch (MappingHandleException e) {
			Exception exc = (Exception)e.getCause();
			if(exc.getMessage()!=null) {
				ResponseContext.addValidationFull(resource, "id",exc.getMessage(),null);
			}else {
				ResponseContext.addError(resource,e);
			}
			SessionContext.executeRollback();
		}
	}


	@Transaction
	public void loadResourceData(Map<String, List<Map<String, Object>>> map) {
		for(Map.Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
			if(entry.getKey().equals("SMT_SETTING_FUNC")||
					entry.getKey().equals("SMT_SETTING_LAYOUT")||
					entry.getKey().equals("SMT_SETTING_PAGE")) {
				SessionContext.getSqlSession().executeUpdate("delete from "+entry.getKey()+"_BASE");
				SessionContext.getTableSession().save(entry.getKey()+"_BASE",entry.getValue());
			}else {
				SessionContext.getSqlSession().executeUpdate("delete from "+entry.getKey());
				SessionContext.getTableSession().save(entry.getKey(),entry.getValue());
			}
			for(Map<String, Object> objectMap:entry.getValue()) {
				ResponseContext.addData(objectMap.get("ID"));
			}
        }
	}
}
