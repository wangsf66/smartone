package com.smartone.ddm.resource.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.douglei.api.doc.annotation.Api;
import com.douglei.api.doc.annotation.ApiParam;
import com.douglei.api.doc.annotation.ApiParam_;
import com.douglei.api.doc.types.ParamStructType;
import com.douglei.orm.sessionfactory.validator.table.handler.InsertValidateHandler;
import com.douglei.orm.sessionfactory.validator.table.handler.UpdateValidateHandler;
import com.ibs.code.controller.BasicController;
import com.ibs.code.result.DataValidationResult;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.ibs.spring.resolver.method.argument.CommonParams;
import com.ibs.spring.resolver.method.argument.CommonParamsModel;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceMapping;
import com.smartone.ddm.resource.service.DmResourceService;


@RestController
@RequestMapping("/resource")
public class DmResourceController extends BasicController{
	
	@Autowired
	private DmResourceService resourceService;
	
	@RequestMapping(value = "/createModel", method = RequestMethod.POST)
	public Response createModel(@CommonParams(cls=DmResource.class)CommonParamsModel<DmResource> commonParamsModel) {
		if(commonParamsModel.getList()!=null&&commonParamsModel.getList().size()>1) {
			ResponseContext.addValidationFull(commonParamsModel.getList(), "id", "不可以进行批量建模", "smartone.resource.createModel.noBatch");
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }else {
        	if(validateByValidator(commonParamsModel.getList().get(0))==DataValidationResult.SUCCESS) {                                             
        		resourceService.createModel(commonParamsModel.getList().get(0));
        	}
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }
	}
	
    
	
	@RequestMapping(value = "/cancelModel", method = RequestMethod.POST)
	public Response cancelModel(@CommonParams(cls=DmResource.class)CommonParamsModel<DmResource> commonParamsModel) {
		if(commonParamsModel.getList()!=null&&commonParamsModel.getList().size()>1) {
			ResponseContext.addValidationFull(commonParamsModel.getList(), "id", "不可以进行批量建模", "smartone.resource.createModel.noBatch");
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }else {
        	if(validateByValidator(commonParamsModel.getList().get(0))==DataValidationResult.SUCCESS) {                                             
        		resourceService.cancelModel(commonParamsModel.getList().get(0));
        	}
        	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
        }
	}
	
	@Api(name="修改资源",
			 request=@ApiParam(params ={
				 @ApiParam_(entity=DmResource.class,entityStruct=ParamStructType.OBJECT)
			 }))
	@RequestMapping({"/update","/batchUpdate"})
	public Response update(@CommonParams(cls=DmResource.class)CommonParamsModel<DmResource> commonParamsModel) {
		if(commonParamsModel.getList()!=null) {
	       	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
	       		resourceService.updateMany(commonParamsModel.getList());
	   		} 
       }
	   return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@Api(name="添加资源",
			 request=@ApiParam(params ={
				 @ApiParam_(entity=DmResource.class,entityStruct=ParamStructType.OBJECT)
			 }))
	@RequestMapping({"/insert","/batchInsert"})
	public Response insert(@CommonParams(cls=DmResource.class)CommonParamsModel<DmResource> commonParamsModel) {
		if(commonParamsModel.getList()!=null) {
       	if(validate4Table(commonParamsModel.getList(), InsertValidateHandler.getSingleton())==DataValidationResult.SUCCESS) { 
       		resourceService.insertMany(commonParamsModel.getList());
   		}
       }
	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}

	@Api(name="删除资源",
			 url=@ApiParam(params ={
					 @ApiParam_(name="ids", required=true, description="资源id", egValue="1111")
			 }))
	@RequestMapping(value="/delete",method=RequestMethod.DELETE)
	public Response delete(HttpServletRequest request) {
		String ids = getDeleteIds(request);
		if(ids!=null) {
			resourceService.delete(ids);
		}
	    return ResponseContext.getFinalResponse();
	}
	
	//修改数据库数据接口
	@RequestMapping(value = "/createModel", method = RequestMethod.GET)
	public Response createModel() {
		resourceService.createModel();
    	return ResponseContext.getFinalResponse(true);
	}
	
	
	@RequestMapping(value = "/getXmlById", method = RequestMethod.GET) 
	public Response getXmlById(HttpServletRequest request) {
		String resourceId = request.getParameter("resourceId");
		resourceService.getXmlById(resourceId);
    	return ResponseContext.getFinalResponse(true);
	}
	
	@RequestMapping(value = "/createModelByXml", method = RequestMethod.POST)
	public Response createModelByXml(@CommonParams(cls=DmResourceMapping.class)CommonParamsModel<DmResourceMapping> commonParamsModel) {
		resourceService.createModelByXml(commonParamsModel.getList().get(0));
    	return ResponseContext.getFinalResponse(true);
	}
	
	@RequestMapping(value = "/text", method = RequestMethod.POST)
	public Response createModelByXml() {
		
		String str = null;
		if(str==null) {
			throw new NullPointerException();
		}
    	return ResponseContext.getFinalResponse();
	}
}

