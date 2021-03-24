package com.smartone.ddm.resource.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibs.components.response.Response;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.ResourceAdapters;
import com.smartone.ddm.resource.service.ResourceMappingService;


@RestController
@RequestMapping("/mapping")
public class DmResourceMappingController {
    
	@Autowired
	private ResourceMappingService resourceMappingService;
	
	@RequestMapping(value="/load", method=RequestMethod.POST)
	public Response receive(@RequestBody List<ResourceAdapters> resourceList){
		resourceMappingService.load(resourceList);
		return ResponseContext.getFinalResponse(true);
	}
	
	@RequestMapping(value="/data/load", method=RequestMethod.POST)
	public Response data(@RequestBody Map<String, List<Map<String, Object>>> map){
		resourceMappingService.loadResourceData(map);
		return ResponseContext.getFinalResponse(true);
	}
	
}
