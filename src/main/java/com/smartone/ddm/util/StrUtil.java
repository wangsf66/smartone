package com.smartone.ddm.util;

public class StrUtil {
	
	//将字符串拼接为sql
	public static String stringToSql(String ids) {
		String id[] = ids.split(",");
		String sql = "";
		for (int i = 0; i < id.length; i++) {
			if (i == id.length - 1) {
				sql += "'" + id[i] + "'";
			} else {
				sql += "'" + id[i] + "' ,";
			}
		}
		return sql;
	}
} 
