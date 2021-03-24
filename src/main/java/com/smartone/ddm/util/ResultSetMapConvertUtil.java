package com.smartone.ddm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.douglei.orm.sql.statement.util.NameConvertUtil;

import ch.qos.logback.core.pattern.ConverterUtil;

/**
 * ResultSet Map转换工具类
 * @author wangShuFang
 */
public class ResultSetMapConvertUtil {
	
	/**
	 * 将resultset map的key, 由列名转换成属性名
	 * @param originResultsetMap
	 * @return
	 */
	private static Map<String, Object> resultsetMapKey2PropertyName_(Map<String, Object> originResultsetMap) {
		Map<String, Object> targetMap = new HashMap<String, Object>(originResultsetMap.size());
		Set<String> keys = originResultsetMap.keySet();
		for (String key : keys) {
			targetMap.put(NameConvertUtil.column2Property(key), originResultsetMap.get(key));
		}
		return targetMap;
	}
	
	/**
	 * 将resultset map的key, 由列名转换成属性名
	 * @param originResultsetMap
	 * @return
	 */
	public static Map<String, Object> resultsetMapKey2PropertyName(Map<String, Object> resultsetMap) {
		if(resultsetMap != null && resultsetMap.size() > 0) {
			return resultsetMapKey2PropertyName_(resultsetMap);
		}
		return null;
	}
	
	/**
	 * 将resultset list<map>中map的key, 由列名转换成属性名
	 * @param originListMap
	 * @return
	 */
	public static List<Map<String, Object>> resultsetListMapKey2PropertyName(List<Map<String, Object>> resultsetListMap){
		if(resultsetListMap!= null && resultsetListMap.size()>0) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(resultsetListMap.size());
			for (Map<String, Object> resultsetMap : resultsetListMap) {
				list.add(resultsetMapKey2PropertyName(resultsetMap));
			}
			return list;
		}
		return null;
	}
	
}
