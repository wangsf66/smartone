package com.smartone.ddm.excelPOI.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.ibs.code.service.BasicService;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.excelPOI.entity.ApplictionType;
/**
 * 
 * @author wangShuFang
 */
@TransactionComponent
public class SysFileService extends BasicService{
	@Transaction
	public void uploadFile(List<Map<String, Object>> assemblyObjects) {
		tableSessionSave("SYS_FILE",assemblyObjects);
    }
	
	//下载
	@Transaction
	public void download(String refDataId,HttpServletResponse response) {
		List<Map<String, Object>> rootlist =  SessionContext.getSqlSession().query("SELECT * FROM SYS_FILE where REF_DATA_ID ='"+refDataId+"'");
		downloadFile(rootlist,response);
		//更新文件下载次数
		SessionContext.getSqlSession().executeUpdate("update SYS_FILE set DOWNLOAD_COUNT = DOWNLOAD_COUNT+1 where ID in("+pjIds(rootlist)+")");
	}
	
	
	//预览
	@Transaction
	public void downloadOnly(String id, HttpServletResponse response,String type) {
		Map<String, Object> fileMap= (Map<String, Object>)SessionContext.getSqlSession().uniqueQuery("SELECT * FROM SYS_FILE where ID='"+id+"'");
		downloadFile(fileMap,response,type);
		//更新文件下载次数
		SessionContext.getSqlSession().executeUpdate("update SYS_FILE set DOWNLOAD_COUNT = DOWNLOAD_COUNT+1 where where ID='"+id+"'");
		
	}
	
	public void downloadFile(Map<String, Object> fileMap,HttpServletResponse response,String type) {
		File file = null;
		OutputStream out = null;
		InputStream input = null;
		byte[] b;
		int len;
		ApplictionType applictionType = new ApplictionType();
	    String suffix = ((String)fileMap.get("SUFFIX")).substring(1);
	    for(String key : applictionType.getTypeMap().keySet()) {
	    	if(key.equals(suffix)) {
	    		response.setHeader("Content-Type", applictionType.getTypeMap().get(key)+";charset=utf-8");
	    	}
	    }
		try {
			if(type!=null&&"download".equals(type)) {
				response.setHeader("Content-Disposition", "attachment;filename=" +turnStrEncoding((String)fileMap.get("ACT_NAME")+fileMap.get("SUFFIX"),"utf-8","iso8859-1"));	
			}
			file = new File((String) fileMap.get("SAVE_PATH"));
			input = new FileInputStream(file);
			b = new byte[1024];
			out = response.getOutputStream(); 
			while((len = input.read(b)) > 0){
				out.write(b, 0, len);
			}
			input.close();
			out.close();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public static String pjIds(List<Map<String, Object>> rootlist) {
		StringBuilder stringIds = new StringBuilder();
		for(int i =0;i<rootlist.size();i++) {
			if(i==rootlist.size()-1) {
				stringIds.append("'"+rootlist.get(i).get("ID")+"'");
			}else {
				stringIds.append("'"+rootlist.get(i).get("ID")+"',");
			}
		}
		return stringIds.toString();
	}
	
	public void downloadFile(List<Map<String, Object>> rootlist,HttpServletResponse response) {
		File file = null;
		OutputStream out = null;
		InputStream input = null;
		ZipOutputStream zos = null;
		byte[] b;
		int len;
		response.setHeader("Content-Type", "application/octet-stream;charset=utf-8");
		try {
			if(rootlist.size()==1) {
				response.setHeader("Content-Disposition", "attachment;filename=" +turnStrEncoding((String)rootlist.get(0).get("ACT_NAME")+rootlist.get(0).get("SUFFIX"),"utf-8","iso8859-1"));
				file = new File((String) rootlist.get(0).get("SAVE_PATH"));
				input = new FileInputStream(file);
				b = new byte[1024];
				out = response.getOutputStream(); 
				while((len = input.read(b)) > 0){
					out.write(b, 0, len);
				}
				input.close();
			}else {
				response.addHeader("Content-Disposition", "attachment;filename="+getFileCode()+".zip");
				out = response.getOutputStream();
				zos = new ZipOutputStream(out);
				for(Map<String, Object> map:rootlist) {
					//if("service".equals(map.get("SAVE_TYPE"))) {
					    zos.putNextEntry(new ZipEntry((String) map.get("ACT_NAME")+(String)map.get("SUFFIX")));
						file = new File((String) map.get("SAVE_PATH"));
						input = new FileInputStream(file);
						b = new byte[1024];
						while((len = input.read(b)) > 0){
							zos.write(b, 0, len);
						}
						zos.closeEntry();
						input.close();
					}
				zos.flush();
				zos.close();
			}
			out.close();
        }catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public static String getFileCode() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public static String turnStrEncoding(String str, String fromEncoding, String toEncoding){
		try {
			return new String(str.getBytes(fromEncoding), toEncoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Transaction
	public void deleteByRefDataId(String refDataId,String fileUploudServerAddress) {
		List<Map<String, Object>> rootlist =  SessionContext.getSqlSession().query("SELECT * FROM SYS_FILE where REF_DATA_ID ='"+refDataId+"'");// TODO Auto-generated method stub
		if(rootlist!=null&&rootlist.size()>0) {
			for(Map<String, Object> fileMap:rootlist) {
				deleteFile(fileMap,fileUploudServerAddress);
			}
			SessionContext.getSqlSession().executeUpdate("delete SYS_FILE where ID in("+pjIds(rootlist)+")");
			ResponseContext.addData(refDataId);
		}else {
			ResponseContext.addValidationFull(refDataId, "id", "该资源下没有附件", "smartone.excelPOI.file.delete");
		}
	}
	
	@Transaction
	public void delete(String id,String  fileUploudServerAddress) {
		Map<String, Object> fileMap= (Map<String, Object>)SessionContext.getSqlSession().uniqueQuery("SELECT * FROM SYS_FILE where ID='"+id+"'");
		if(fileMap!=null) {
			deleteFile(fileMap,fileUploudServerAddress);
			SessionContext.getSqlSession().executeUpdate("delete SYS_FILE where ID= '"+id+"'");
			ResponseContext.addData(id);
		}
	}
	
	private void deleteFile(Map<String, Object> fileMap,String fileUploudServerAddress) {
		File dir = new File(fileUploudServerAddress+"\\"+fileMap.get("CODE")+fileMap.get("SUFFIX"));
		if(dir.exists()){
			dir.delete();
		}
	}
}
