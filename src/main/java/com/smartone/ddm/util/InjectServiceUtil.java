package com.smartone.ddm.util;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smartone.ddm.resource.service.DmResourceMappingService;

@Component
public class InjectServiceUtil {
	@Autowired
    private DmResourceMappingService dmResourceMappingService;
    @Autowired
	private IsBuiltInResource isBuiltInResource;
    
    @PostConstruct
    public void init(){
        InjectServiceUtil.getInstance().dmResourceMappingService = this.dmResourceMappingService;
        InjectServiceUtil.getInstance().isBuiltInResource = this.isBuiltInResource;
    }
 
    /**
     *  实现单例 start
     */
    private static class SingletonHolder {
        private static final InjectServiceUtil INSTANCE = new InjectServiceUtil();
    }
    private InjectServiceUtil (){}
    public static final InjectServiceUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }
    /**
     *  实现单例 end
     */
	public DmResourceMappingService getDmResourceMappingService() {
		return dmResourceMappingService;
	}
	
	public IsBuiltInResource getIsBuiltInResource() {
		return isBuiltInResource;
	}
	public void setIsBuiltInResource(IsBuiltInResource isBuiltInResource) {
		this.isBuiltInResource = isBuiltInResource;
	}
	
}
