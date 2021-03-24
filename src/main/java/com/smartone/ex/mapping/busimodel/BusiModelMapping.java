package com.smartone.ex.mapping.busimodel;

import com.douglei.orm.mapping.Mapping;
import com.douglei.orm.mapping.metadata.AbstractMetadata;
import com.smartone.ex.mapping.ExMappingTypeConstants;

/**
 * busimode 映射
 * @author DougLei
 */
public class BusiModelMapping extends Mapping {

	public BusiModelMapping(AbstractMetadata metadata) {
		super(ExMappingTypeConstants.BUSIMODEL,metadata);
	}
}
