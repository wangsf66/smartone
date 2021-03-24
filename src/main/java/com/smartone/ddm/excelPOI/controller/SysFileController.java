package com.smartone.ddm.excelPOI.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import com.douglei.api.doc.annotation.ApiCatalog;
import com.douglei.tools.file.reader.PropertiesReader;
import com.ibs.code.controller.BasicController;
import com.ibs.components.filters.log.core.filter.LogContext;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.excelPOI.service.SysFileService;
/**
 * 
 * @author wangShuFang
 */
@ApiCatalog(name="附件上传")
@RestController
@RequestMapping("/file")
public class SysFileController extends BasicController {
	
    public static String fileUploudKeySeparator = null;
	
	public static Integer sizes = null;
	
	public static String fileUploudServerAddress = null;  
	
	 @Autowired
	 private SysFileService sysFileService;
	
	 @RequestMapping(value="/upload",method={RequestMethod.POST})
	  public Response uploadFile(HttpServletRequest request, HttpServletResponse response) throws Exception{
		    DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload servletUpload = new ServletFileUpload(factory);
			servletUpload.setHeaderEncoding("utf-8");// 设置头的编码格式
			StandardMultipartHttpServletRequest req = (StandardMultipartHttpServletRequest)request;
			//存储所有对象
			List<Map<String,Object>> pojoList = new ArrayList<Map<String,Object>>();
			//读取配置文件
			readProfile();
			List<Map<String, Object>> objectList= assemblyObjects(req.getMultiFileMap(),req.getParameterMap(),pojoList) ;
			LogContext.setFileInfo2ReqBody(objectList);
		    sysFileService.uploadFile(objectList); 
		    return ResponseContext.getFinalResponse(true);
	  } 
	 
	   public void readProfile() {
			PropertiesReader propertiesReader = new PropertiesReader("basicConfiguration.properties");
			fileUploudKeySeparator = propertiesReader.readProperty("fileUploudKeySeparator", ".");
			sizes = Integer.parseInt(propertiesReader.readProperty("sizes", "10"));
			fileUploudServerAddress = propertiesReader.readProperty("fileUploudServerAddress");
		}
	 
	   private List<Map<String, Object>> assemblyObjects(MultiValueMap<String, MultipartFile> multiFileMap, Map<String, String[]> parameterMap,
			List<Map<String, Object>> pojoList) {
		    Map<String, Object> objMap = null;
		    MultipartFile  file = null;
		    try {
		    for(Entry<String, List<MultipartFile>> itemList : multiFileMap.entrySet()) {
		    	objMap = new HashMap<String, Object>();
		    	String[] key = itemList.getKey().split("\\"+fileUploudKeySeparator);
		    	for(Entry<String, String[]> paramList : parameterMap.entrySet()) {
		    		String[] key2 = paramList.getKey().split("\\"+fileUploudKeySeparator);
		    		if(key[1].equals(key2[1])) {
		    			objMap.put(key2[0], paramList.getValue()[0]);
		    			parameterMap.remove(key2[0]);
		    		}
		    	}
			    file = itemList.getValue().get(0);
		    	String name = file.getOriginalFilename();
		        String filename = name.substring(0,name.lastIndexOf("."));//获取没有后缀的文件名
		        String suffix = name.substring(name.lastIndexOf("."));//获取没有后缀的文件名 
		        String code = null;
		    	if(file.getSize()<=sizes*1024*1024) {
		    		objMap.put("SIZES",file.getSize());
		    		objMap.put("ACT_NAME",filename);
			    	objMap.put("SUFFIX",suffix);
			    	code=DigestUtils.md5Hex(name);
			    	objMap.put("CODE",code);
			    	File dir = new File(fileUploudServerAddress);
			    	if(dir.exists()) {
			    		objMap.put("SAVE_PATH",fileUploudServerAddress+"\\"+code+suffix);
			    	}else {
			    		dir.mkdir();
			    		objMap.put("SAVE_PATH",fileUploudServerAddress+"\\"+code+suffix);
			    	}
			    	if(objMap.get("BATCH")==null) {
			    		 SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
			             objMap.put("BATCH",df.format(new Date()));
			    	}
			    	pojoList.add(objMap);
			    	//上传文件
			    	uploudFile(file,suffix,code);
		    	}else {
		    		ResponseContext.addValidationFull(file.getSize(), "SIZES","上传文件大小不可超过10M","smartone.file.upload.fileIsTooLarge");
		    	}
		      }
		    } catch (Exception e) {
				e.printStackTrace();
			}
		    return pojoList; 
	 }
    //上传文件
	@SuppressWarnings("unused")
	public void uploudFile(MultipartFile file,String suffix,String code) {
		File fileObj = null;
		InputStream is=null;
		try {
			is = file.getInputStream();
			fileObj = multipartFileToFile(file,suffix,code);
			delteTempFile(fileObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static File multipartFileToFile(MultipartFile file,String suffix,String code) throws Exception {
        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile,suffix,code);
            ins.close();
        }
        return toFile;
    }
 
    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file,String suffix,String code) {
        try {
            OutputStream os = new FileOutputStream(new File(fileUploudServerAddress,code+suffix));
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 删除本地临时文件 
     * @param file
     */
    public static void delteTempFile(File file) {
    if (file != null) {
        File del = new File(file.toURI());
        del.delete();
        }
    }
    
    @RequestMapping(value="/downloadByRefDataId",method={RequestMethod.GET})
    public Response download(HttpServletRequest request, HttpServletResponse response) {
    	String refDataId = request.getParameter("refDataId");
    	sysFileService.download(refDataId,response); 
    	return ResponseContext.getFinalResponse();
    }
    
    @RequestMapping(value="/download",method={RequestMethod.GET})
    public Response downloadOnly(HttpServletRequest request, HttpServletResponse response,String type) {
    	String id = request.getParameter("id");
    	sysFileService.downloadOnly(id,response,type); 
    	return ResponseContext.getFinalResponse();
    }
    
    @RequestMapping(value="/deleteByRefDataId",method={RequestMethod.GET})
    public Response deleteByRefDataId(HttpServletRequest request) {
    	String refDataId = request.getParameter("refDataId");
    	readProfile();
    	sysFileService.deleteByRefDataId(refDataId,fileUploudServerAddress); 
    	return ResponseContext.getFinalResponse();
    }
    
    @RequestMapping(value="/delete",method={RequestMethod.GET})
    public Response delete(HttpServletRequest request) {
    	String id = request.getParameter("id");
    	readProfile();
    	sysFileService.delete(id,fileUploudServerAddress); 
    	return ResponseContext.getFinalResponse();
    }
}
