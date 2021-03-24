package com.smartone.ddm.excelPOI.entity;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author wangShuFang
 */
public class ApplictionType {
    private Map<String,String> typeMap = new HashMap<String,String>();

	public ApplictionType() {
		typeMap.put("doc", "application/msword");
		typeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		typeMap.put("xls", "application/vnd.ms-excel");
		typeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		typeMap.put("pdf", "application/pdf");
		typeMap.put("gif", "image/gif");
		typeMap.put("jpeg", "image/jpeg");
		typeMap.put("jpg", "image/jpeg");
		typeMap.put("png", "image/png");
		typeMap.put("mpeg", "video/mpeg");
		typeMap.put("mpg", "video/mpeg");
		typeMap.put("bmp", "image/bmp");
		typeMap.put("bin", "application/octet-stream");
		typeMap.put("exe", "application/octet-stream");
		typeMap.put("so", "application/octet-stream");
		typeMap.put("dll", "application/octet-stream");
	}

	public Map<String, String> getTypeMap() {
		return typeMap;
	}

	public void setTypeMap(Map<String, String> typeMap) {
		this.typeMap = typeMap;
	}
	
}
