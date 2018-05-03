package com.ibm.oms.admin.action.order.maintenance;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.struts2.convention.annotation.ParentPackage;

import com.ibm.oms.admin.action.order.AbstractOrderAction;
import com.ibm.oms.service.OrdiErrOptLogService;

/**
 * 后台运维Action类
 */

@ParentPackage("admin")
public class MaintenanceAction extends AbstractOrderAction {

	private Map<String, String> map = new HashMap<String, String>();
	
	@Resource
	private OrdiErrOptLogService ordiErrOptLogService;
	/**
	 * 
	 */
	private static final long serialVersionUID = -8846588470340142948L;

	public String execute() {
		pager = ordiErrOptLogService.getPagerByMap(map, pager);
		return "maintenance";
	}
	
	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
