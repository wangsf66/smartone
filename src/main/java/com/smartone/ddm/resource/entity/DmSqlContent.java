package com.smartone.ddm.resource.entity;
/**
 * 
 * @author wangShuFang
 */
public class DmSqlContent {
	private Integer id;
	private String resourceId;
	private String name = "";
	private String content;
	private String dialet;
	private Integer type;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getDialet() {
		return dialet;
	}
	public void setDialet(String dialet) {
		this.dialet = dialet;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public DmSqlContent( String resourceId, String content, String dialet, Integer type) {
		super();
		this.resourceId = resourceId;
		this.content = content;
		this.dialet = dialet;
		this.type = type;
	}
	public DmSqlContent() {
		super();
		// TODO Auto-generated constructor stub
	}
}
