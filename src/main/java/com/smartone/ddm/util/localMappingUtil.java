package com.smartone.ddm.util;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author wangShuFang
 */
public class localMappingUtil {
      
	public final static Map<String,String> localMap = new HashMap<String,String>();
      
	static {
		localMap.put("DM_RESOURCE", "DM_RESOURCE");
		localMap.put("DM_RESOURCE_PARAMS", "DM_RESOURCE_PARAMS");
		localMap.put("DM_RESOURCE_MAPPING", "DM_RESOURCE_MAPPING");
		localMap.put("CFG_SEQ_INFO", "CFG_SEQ_INFO");
		localMap.put("CFG_PROP_CODE_RULE_DETAIL", "CFG_PROP_CODE_RULE_DETAIL");
		localMap.put("CFG_PROP_CODE_RULE", "CFG_PROP_CODE_RULE");
		localMap.put("CFG_CODE_DATA_DICTIONARY", "CFG_CODE_DATA_DICTIONARY");
		localMap.put("DM_CFG_BUSI_MODEL", "DM_CFG_BUSI_MODEL");
		localMap.put("DM_CFG_BUSI_MODEL_RES_RELATIONS", "DM_CFG_BUSI_MODEL_RES_RELATIONS");
		localMap.put("DM_SQL_BUSI_STRUCTURE", "DM_SQL_BUSI_STRUCTURE");
		localMap.put("OFFICE_SHEET", "OFFICE_SHEET");
	}
}
