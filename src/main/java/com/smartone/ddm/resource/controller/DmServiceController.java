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
import com.douglei.orm.sessionfactory.validator.table.handler.UpdateValidateHandler;
import com.ibs.code.controller.BasicController;
import com.ibs.code.result.DataValidationResult;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.ibs.spring.resolver.method.argument.CommonParams;
import com.ibs.spring.resolver.method.argument.CommonParamsModel;
import com.smartone.ddm.resource.entity.DmResourcePublishTarget;
import com.smartone.ddm.resource.entity.DmService;
import com.smartone.ddm.resource.entity.PublishResource;
import com.smartone.ddm.resource.service.DmServiceService;

@RestController
@RequestMapping("/service")
public class DmServiceController extends BasicController{
	
	@Autowired
	private DmServiceService dmServiceService;
	
	@Api(name="批量添加资源",
			 request=@ApiParam(params ={ 
				 @ApiParam_(entity=DmService.class,entityStruct=ParamStructType.ARRAY)
			 }))
	@RequestMapping({"/insert"})
	public Response insertMany(@CommonParams(cls=DmService.class)CommonParamsModel<DmService> commonParamsModel){  
		if(commonParamsModel.getList()!=null) {
	       	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
	       		dmServiceService.insertMany(commonParamsModel.getList());
	   		} 
        }
	 	return ResponseContext.getFinalResponse(commonParamsModel.getIsBatch());
	}
	
	@Api(name="批量修改资源",
			 request=@ApiParam(params ={
				 @ApiParam_(entity=DmService.class,entityStruct=ParamStructType.ARRAY)
			 }))
	@RequestMapping({"/update"})
	public Response updateMany(@CommonParams(cls=DmService.class)CommonParamsModel<DmService> commonParamsModel){
		if(commonParamsModel.getList()!=null) {
	       	if(validate4Table(commonParamsModel.getList(),UpdateValidateHandler.getInstance4UpdateNullValue())==DataValidationResult.SUCCESS) { 
	       		dmServiceService.updateMany(commonParamsModel.getList());
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
			dmServiceService.delete(ids);
		}
	    return ResponseContext.getFinalResponse();
	}
	
	@Api(name="查询服务信息")
	@RequestMapping(value="/resource/query",method=RequestMethod.GET)
	public Response queryById(HttpServletRequest request) {
		PublishResource publishResource = new PublishResource(request.getParameter("resourceName"),
				request.getParameter("moduleId"),request.getParameter("id"));
		if(request.getParameter("resourceType")!=null) {
			publishResource.setResourceType(Integer.parseInt(request.getParameter("resourceType")));
		}
		if(request.getParameter("_rows")!=null) {
			publishResource.setRows(Integer.parseInt(request.getParameter("_rows")));
		}
		if(request.getParameter("_page")!=null) {     
			publishResource.setPage(Integer.parseInt(request.getParameter("_page")));
		}
	    dmServiceService.queryById(publishResource);
	    return ResponseContext.getFinalResponse();
	}
	
	//初始化服务数据
	@RequestMapping(value="/initResources",method=RequestMethod.POST)
	public Response initResourcesById(@CommonParams(cls=DmService.class)CommonParamsModel<DmService> commonParamsModel) {
	    dmServiceService.initResourcesById(commonParamsModel.getList().get(0).getId());
	    return ResponseContext.getFinalResponse(true);
	}
	
	//初始化服务数据
	@RequestMapping(value="/syncMenuData",method=RequestMethod.POST)
	public Response syncMenuData(@CommonParams(cls=DmService.class)CommonParamsModel<DmService> commonParamsModel) {
	    dmServiceService.syncMenuData(commonParamsModel.getList().get(0).getId(),"data");
	    return ResponseContext.getFinalResponse(true);
	}
	
	//初始化服务数据
	@RequestMapping(value="/syncModuleData",method=RequestMethod.POST)
	public Response syncModelData(@CommonParams(cls=DmService.class)CommonParamsModel<DmService> commonParamsModel) {
	    dmServiceService.syncMenuData(commonParamsModel.getList().get(0).getId(),"module");
	    return ResponseContext.getFinalResponse(true);
	}
	
	
	//单个资源发布
	@RequestMapping(value="/publishResource",method=RequestMethod.POST)
	public Response publishResource(@CommonParams(cls=DmResourcePublishTarget.class)CommonParamsModel<DmResourcePublishTarget> commonParamsModel) {
	    dmServiceService.publishResource(commonParamsModel.getList().get(0));
	    return ResponseContext.getFinalResponse(true);
	}
	
}
