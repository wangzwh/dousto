package com.ibm.oms.dao.intf;

import com.ibm.oms.domain.persist.HangOrderSub;
import com.ibm.sc.dao.BaseDao;
/**
 * @author wangqc
 * @date 2018年2月7日 下午2:21:14
 * 
 */
public interface HangOrderSubDao extends BaseDao<HangOrderSub,Long>{

	void deleteByIdOrder(long orderId);
	
}
