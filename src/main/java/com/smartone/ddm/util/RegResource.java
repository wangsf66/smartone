package com.smartone.ddm.util;

public class RegResource {
    private String name; 
    private Class clz;
    private boolean isBuilt = false;
    
	public boolean isBuilt() {
		return isBuilt;
	}
	public void setBuilt(boolean isBuilt) {
		this.isBuilt = isBuilt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class getClz() {
		return clz;
	}
	public void setClz(Class clz) {
		this.clz = clz;
	}
	
	public RegResource(String name, Class clz) {
		this.name = name;
		this.clz = clz;
	}
	
	public RegResource(String name, Class clz, boolean isBuilt) {
		this.name = name;
		this.clz = clz;
		this.isBuilt = isBuilt;
	}
	
	public RegResource(String name) {
		this.name = name;
	}
}
