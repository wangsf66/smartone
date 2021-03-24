package com.smartone.ddm.resource.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.douglei.api.doc.annotation.Api;
import com.ibs.code.controller.BasicController;
import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.service.DmPublishStateService;

@RestController
@RequestMapping("/publishState")
public class DmPublishStateController extends BasicController{
	
	@Autowired
	private DmPublishStateService dmPublishStateService;
	
	@Api(name="查询资源")
	@RequestMapping(value="/query",method=RequestMethod.GET)
	public Response query(HttpServletRequest request) {
		String serviceId = request.getParameter("serviceId");
		dmPublishStateService.query(serviceId);
	    return ResponseContext.getFinalResponse();
	}
}
