package com.smartone.ddm.resource.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.douglei.orm.context.SessionFactoryContainer;
import com.douglei.orm.mapping.MappingProperty;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.douglei.orm.sessionfactory.sessions.session.sql.purpose.Purpose;
import com.ibs.code.controller.BasicController;
import com.ibs.code.result.DataValidationResult;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.ibs.spring.resolver.method.argument.CommonParams;
import com.ibs.spring.resolver.method.argument.CommonParamsModel;
import com.smartone.ddm.resource.service.ResourceDynamicOperateService;
import com.smartone.ddm.util.IsBuiltInResource;
import com.smartone.ddm.util.ProcedurePrefixUtil;
import com.smartone.ddm.util.RegResource;

/**
 * 
 * @author wangShuFang
 */
@RestController
@RequestMapping("/resource")
public class ResourceDynamicOperateController extends BasicController{
	
	@Autowired
	private ResourceDynamicOperateService resourceDynamicOperateService;
	
	@Autowired
	private IsBuiltInResource isBuiltInResource;
	
	//统一查询接口
	@RequestMapping("/{resourceName}/query")
	public Response queryCondition(HttpServletRequest request,@PathVariable(name = "resourceName") String resourceName){
		RegResource regResource = isBuiltInResource.builtInResourceCheck(resourceName);
		resourceDynamicOperateService.dynamicQuery(request,resourceName,regResource.isBuilt());
		return ResponseContext.getFinalResponse();
	}
	
	
	@RequestMapping({"/{resourceName}/insert","/{resourceName}/batchInsert"})
	public Response insert(@CommonParams(cls=Map.class)CommonParamsModel<Map<String,Object>> commonParamsModel,@PathVariable(name = "resourceName") String resourceName ) {
		RegResource regResource = isBuiltInResource.builtInResourceCheck(resourceName);
		if(regResource.isBuilt()) {
			ResponseContext.addValidationFull(resourceName, "resurceName","不可以对内置资源执行动态操作 ", "smartone.dynamic.resource.notExecute");
		}else {
			if(commonParamsModel.getList()!=null) {
        		resourceDynamicOperateService.insertMany(resourceName,commonParamsModel.getList());
	        }
		}
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@RequestMapping({"/{resourceName}/update","/{resourceName}/batchUpdate"})
	public Response update(@CommonParams(cls=Map.class)CommonParamsModel<Map<String,Object>> commonParamsModel,@PathVariable(name = "resourceName") String resourceName ) {
		RegResource regResource = isBuiltInResource.builtInResourceCheck(resourceName);
		if(regResource.isBuilt()) {
			ResponseContext.addValidationFull(resourceName, "resurceName","不可以对内置资源执行动态操作 ", "smartone.dynamic.resource.notExecute");
		}else {
		  if(commonParamsModel.getList()!=null) {
        	 resourceDynamicOperateService.updateMany(resourceName,commonParamsModel.getList());
          }
		}
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	                                                                                                                                                                   
	@RequestMapping(value="/{resourceName}/delete",method=RequestMethod.DELETE) 
	public Response deleteObject(HttpServletRequest request,@PathVariable(name = "resourceName") String resourceName ) {
		RegResource regResource = isBuiltInResource.builtInResourceCheck(resourceName);
		if(regResource.isBuilt()) {
			ResponseContext.addValidationFull(resourceName, "resurceName","不可以对内置资源执行动态操作 ", "smartone.dynamic.resource.notExecute");
		}else {
			String ids = getDeleteIds(request);
			if(ids!=null) {
				resourceDynamicOperateService.deleteObject(resourceName,ids);
			}
		}
	    return ResponseContext.getFinalResponse();
	}
	
	//sql资源统一执行接口
	@RequestMapping({"/{resourceName}/operate","/{resourceName}/batchOperate"})
	public Response operate(@CommonParams(cls=Map.class)CommonParamsModel<Map<String,Object>> commonParamsModel,@PathVariable(name = "resourceName") String resourceName ) {
		RegResource regResource = isBuiltInResource.builtInResourceCheck(resourceName);
		if(regResource.isBuilt()) {
			ResponseContext.addValidationFull(resourceName, "resurceName","不可以对内置资源执行动态操作 ", "smartone.dynamic.resource.notExecute");
		}else {
			if(commonParamsModel.getList()!=null) {
				MappingProperty property = SessionFactoryContainer.getSingleton().get().getMappingHandler().getMappingProperty(resourceName);
				if(property == null) {
					ResponseContext.addValidation("不存在名为[%s]的资源", "smartone.resource.unexists", resourceName);
				}else {
					if(MappingTypeNameConstants.TABLE.equals(property.getType())) {
						 ResponseContext.addValidationFull(resourceName, "resurceName", "table资源接口调用错误","smartone.dynamic.resource.tableErroeInterface");
					}else {
						JSONObject extend = JSONObject.parseObject(property.getExtend());
						int sqlType = extend.getInteger("type");
						if(sqlType != 1 || (sqlType == 1 && validate4Sql(Purpose.PROCEDURE,ProcedurePrefixUtil.CALL+resourceName,null,commonParamsModel.getList())==DataValidationResult.SUCCESS)) 
			        		resourceDynamicOperateService.operateMany(resourceName,commonParamsModel.getList(), sqlType);
					}
				}
	        }
		}
		return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
}
