package com.smartone.ddm.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.smartone.ddm.busimodel.entity.DmBusiModelRelation;
import com.smartone.ddm.busimodel.entity.DmSqlBusiStructure;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceMapping;
import com.smartone.ddm.resource.entity.DmResourceParam;


@Component
public class IsBuiltInResource {
    
	@SuppressWarnings("rawtypes")
	private Map<String,Class> builtInResourceMap = new HashMap<String,Class>();
	
	@Bean
	public void myBean() {
		builtInResourceReg("DM_RESOURCE",DmResource.class);
		builtInResourceReg("DM_CFG_BUSI_MODEL_RES_RELATIONS",DmBusiModelRelation.class);
		builtInResourceReg("DM_RESOURCE_PARAMS",DmResourceParam.class);
		builtInResourceReg("DM_RESOURCE_MAPPING",DmResourceMapping.class);
		builtInResourceReg("DM_SQL_BUSI_STRUCTURE",DmSqlBusiStructure.class);
	}
	
	public RegResource builtInResourceReg(String name, Class clz) {
		builtInResourceMap.put(name,clz);
		return new RegResource(name,clz,true);
	}
	
	public RegResource builtInResourceCheck(String name) {
		RegResource regResource = new RegResource(name);
        if(builtInResourceMap.get(name)!=null) {
        	regResource.setBuilt(true);
        	regResource.setClz(regResource.getClz());
        	return regResource;
        }else {
        	regResource.setBuilt(false);
        	return regResource;
        }
	}
}
