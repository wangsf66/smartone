package com.smartone.ddm.resource.entity;

import com.ibs.code.entity.BasicEntity;

/**
 * 
 * @author wangShuFang
 */

public class DmResourcePrepose extends BasicEntity{
    
    private String resourceId;
    private String preposeResourceId;
    
    @Override
    public boolean equals(Object o) {
    	DmResourcePrepose resourcePrepose = (DmResourcePrepose)o;
		if(preposeResourceId.equals(resourcePrepose.getResourceId())) {
			return true;
		}
		return false;
	}
   
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getPreposeResourceId() {
		return preposeResourceId;
	}
	public void setPreposeResourceId(String preposeResourceId) {
		this.preposeResourceId = preposeResourceId;
	}
}
