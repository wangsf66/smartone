package com.smartone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 
 * @author DougLei
 */
public class UpdateResourceMappingApp {
	public static void main(String[] args) throws Exception{
		Connection conn = DriverManager.getConnection("jdbc:sqlserver://192.168.1.111:1433;DatabaseName=SMT_DEV", "sa", "123_abc");
		PreparedStatement pst = conn.prepareStatement("update DM_RESOURCE_MAPPING set mapping_content=? where resource_id=?");
		
		String content;
		ResultSet rs = conn.createStatement().executeQuery("select resource_id, mapping_content from DM_RESOURCE_MAPPING");
		while(rs.next()) {
			content = rs.getString(2).replaceAll("dataType=\"integer\"", "dataType=\"number\"")
						   .replaceAll("dataType=\"byte\"", "dataType=\"number\"")
						   .replaceAll("dataType=\"Integer\"", "dataType=\"number\"")
						   .replaceAll("dataType=\"short\"", "dataType=\"number\"")
						   .replaceAll("dataType=\"long\"", "dataType=\"number\"")
						   .replaceAll("dataType=\"double\"", "dataType=\"number\"")
						   .replaceAll("dataType=\"float\"", "dataType=\"number\"")
						   .replaceAll("descriptionName=", "description=")
						   .replaceAll("dataType=\"date\"", "dataType=\"datetime\"");
			
			System.out.println(content); // 先全部输出, 看看有没有没有修改到的地方
			
//			pst.setString(1, content);
//			pst.setString(2, rs.getString(1));
//			pst.executeUpdate();
		}
		
		conn.commit();
	}
}
