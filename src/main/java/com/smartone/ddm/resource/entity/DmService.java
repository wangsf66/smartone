package com.smartone.ddm.resource.entity;

import java.util.Comparator;

import com.ibs.code.entity.BasicEntity;

public class DmService extends BasicEntity implements Comparator<DmService>{

	private String name;
	private String pid;
	private String ip;
	private String port;
	private String remark;
	private String descName;
	private String account;
	private String password;
	private String datebase;
	
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getDatebase() {
		return datebase;
	}
	public void setDatebase(String datebase) {
		this.datebase = datebase;
	}

	private int weight;
	
	
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getDescName() {
		return descName;
	}
	public void setDescName(String descName) {
		this.descName = descName;
	}
	@Override
	public int compare(DmService parentService, DmService service) {
		if(parentService.getWeight()<service.getWeight()) {
			return 1;
		}else {
			return 0;
		}
	}
}
