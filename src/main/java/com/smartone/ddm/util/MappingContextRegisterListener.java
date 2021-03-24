package com.smartone.ddm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import com.douglei.orm.context.SessionFactoryContainer;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.douglei.orm.mapping.handler.MappingHandleException;
import com.douglei.orm.mapping.handler.entity.AddOrCoverMappingEntity;
import com.douglei.orm.mapping.handler.entity.MappingEntity;
import com.smartone.ex.mapping.ExMappingTypeConstants;
/**
 * 
 * @author wangShuFang
 */

public class MappingContextRegisterListener implements ApplicationListener<ApplicationStartedEvent>{
	
	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		List<Map<String, Object>> objectList = InjectServiceUtil.getInstance().getDmResourceMappingService().query();
		List<MappingEntity> list = new ArrayList<MappingEntity>();
		if(objectList.size()>0) {
			for (Map<String, Object> map : objectList) {
				if (Integer.parseInt(map.get("RESOURCE_TYPE").toString()) == 10) {
					list.add(new AddOrCoverMappingEntity(map.get("MAPPING_CONTENT").toString(), MappingTypeNameConstants.TABLE).enableProperty());
				} else if(Integer.parseInt(map.get("RESOURCE_TYPE").toString()) == 15){
					list.add(new AddOrCoverMappingEntity(map.get("MAPPING_CONTENT").toString(), MappingTypeNameConstants.PROCEDURE).enableProperty());
				} else if(Integer.parseInt(map.get("RESOURCE_TYPE").toString()) == 20){
					list.add(new AddOrCoverMappingEntity(map.get("MAPPING_CONTENT").toString(), MappingTypeNameConstants.SQL).enableProperty());
				} else if(Integer.parseInt(map.get("RESOURCE_TYPE").toString()) == 30){
					list.add(new AddOrCoverMappingEntity(map.get("MAPPING_CONTENT").toString(), ExMappingTypeConstants.BUSIMODEL).enableProperty());
				} 
			}
			try {
				SessionFactoryContainer.getSingleton().get().getMappingHandler().execute(list);
			} catch (MappingHandleException e) {
				e.printStackTrace();
			}
		}
	}
}
