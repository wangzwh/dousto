package com.ibm.oms.rs.service.mqListener;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.oms.dao.constant.IntfReceiveConst;
import com.ibm.oms.domain.persist.IntfReceived;
import com.ibm.oms.intf.intf.TmsStatusDTO;
import com.ibm.oms.service.IntfReceivedService;
import com.ibm.oms.service.OrdiErrOptLogService;
import com.ibm.oms.service.business.trans.TmsOmsLogisticsStatusTransService;
import com.ibm.oms.service.mq.QueueListenerAbstract;
import com.ibm.oms.service.util.CommonConstService;
import com.ibm.oms.service.util.CommonUtilService;

/**
 * @author pjsong
 * 
 */
@Component
public class ThirdTmsLogListener extends QueueListenerAbstract {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	IntfReceivedService intfReceivedService;
    @Autowired
    TmsOmsLogisticsStatusTransService tmsOmsLogisticsStatusTransService;
	@Autowired
	OrdiErrOptLogService ordiErrOptLogService;

	@Override
	protected boolean doProcess(String reqXml) {
	    ObjectMapper om = new ObjectMapper();
        TmsStatusDTO tmsStatusDTO;
        try {
            tmsStatusDTO = om.readValue(reqXml, TmsStatusDTO.class);
        } catch (Exception e1) {
            logger.info("{}", e1);
            return false;
        } 
        String msg = CommonUtilService.createOrderValidate(tmsStatusDTO);
        if (!msg.equals(CommonConstService.OK)) {
            return false;
        }
        try {
            tmsOmsLogisticsStatusTransService.saveThirdTmsLog(tmsStatusDTO);
            return true;
        } catch (Exception e) {
            logger.error("TmsStatusListener  --> {}", e);
            return false;
        }
	}

	@Override
	protected boolean doProcess(Object reqObject) {
		return false;
	}

    @Override
    protected <T> IntfReceived saveTrack(T reqObject) {
        return null;
    }
    
    @Override
    protected IntfReceived saveTrack(String reqXml) {
        IntfReceived ir = new IntfReceived();
        ir.setIntfCode(IntfReceiveConst.TMS_THIRD_STATUS_TO_OMS.getCode());
        ir.setMsg(reqXml);
        ir.setSucceed(1l);
        ir.setCreateTime(new Date());
        intfReceivedService.save(ir);
        return ir;
    }

    @Override
    protected void updateTrack(IntfReceived intfReceived) {
        intfReceivedService.update(intfReceived);
    }
}
