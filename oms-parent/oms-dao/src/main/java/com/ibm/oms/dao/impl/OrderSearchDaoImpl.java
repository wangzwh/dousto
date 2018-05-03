package com.ibm.oms.dao.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.oms.dao.constant.CommonConst;
import com.ibm.oms.dao.constant.Global;
import com.ibm.oms.dao.constant.OrderColumn;
import com.ibm.oms.dao.intf.OrderSearchDao;
import com.ibm.oms.domain.persist.OrderItem;
import com.ibm.oms.domain.persist.OrderSearch;
import com.ibm.sc.bean.Pageable;
import com.ibm.sc.bean.Pager;
import com.ibm.sc.bean.QueryOrder.Direction;
import com.ibm.sc.dao.impl.BaseDaoImpl;
import com.ibm.sc.util.BeanUtils;
import com.ibm.sc.util.DateUtils;

/**
 * DAOģʽ,
 * 
 * Creation date:2014-03-14 04:20:47
 * 
 * @author:Yong Hong Luo
 */
@Repository("orderSearchDaoImpl")
public class OrderSearchDaoImpl extends BaseDaoImpl<OrderSearch, Long>
		implements OrderSearchDao {

	@Resource
	private OrderItemDaoImpl orderItemDaoImpl;

	@SuppressWarnings("rawtypes")
    @Autowired
	public void setTransportAreaDao(TransportAreaDaoImpl transportAreaDao) {
	}

	@Autowired
	public void setOrderPayDao(OrderPayDaoImpl orderPayDao) {
	}

	/*
	 * 非分页查询
	 * 
	 * @see com.ibm.sc.dao.oms.intf.OrderSearchDao#findByOrderSearch(int,
	 * com.ibm.sc.oms.persist.OrderSearch, java.util.List, java.util.List)
	 */
	public List<OrderSearch> findByOrderSearch(int columnId, OrderSearch order,
			List<String> statusPayOther, List<String> statusTotalOther) {
		
		return  queryDateListBySql(order,statusPayOther,statusTotalOther,columnId,null);
	}

	

	/* 分页查询
	 * @see com.ibm.sc.dao.oms.intf.OrderSearchDao#findByOrderSearch(int, com.ibm.sc.oms.persist.OrderSearch, com.ibm.sc.bean.Pager, java.util.List, java.util.List)
	 */
	@SuppressWarnings("rawtypes")
    public Pager findByOrderSearch(int columnId, OrderSearch order,
			Pager pager, List<String> statusPayOther,
			List<String> statusTotalOther) {
		Pager	page = queryDateBySql(order,statusPayOther,statusTotalOther,columnId,pager);
		
		return page;
	}

	private boolean isNotAllInt(java.lang.Long value) {
		boolean flag = false;
		if (null != value && value != Global.ALL_INT) {
			flag = true;
		}
		return flag;
	}

	private boolean isNotAllString(String value) {
		boolean flag = false;
		if (StringUtils.isNotEmpty(value) && !value.equals(Global.ALL_STRING)) {
			flag = true;
		}
		return flag;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.sc.dao.impl.BaseDaoImpl#findByPager(javax.persistence.criteria
	 * .CriteriaQuery, com.ibm.sc.bean.Pageable)
	 */
	@SuppressWarnings("unchecked")
    public <E> Pager<E> findByPager(CriteriaQuery<E> ocq, Pageable pager) {
		if (pager == null) {
			pager = new Pageable();
		}

		Integer pageNumber = pager.getPageNumber();
		Integer pageSize = pager.getPageSize();

		CriteriaBuilder cb = getCriteriaBuilder();

		// 执行count查询
		Set<Root<?>> roots = ocq.getRoots();
		Root<?> root = roots.iterator().next();

		CriteriaQuery<Long> ccq = cb.createQuery(Long.class);
		try {
			Object qs = BeanUtils.getPrivateProperty(ccq, "queryStructure");
			BeanUtils.setPrivateProperty(qs, "roots", roots);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ccq.select(cb.countDistinct(root));
		Predicate restriction = ocq.getRestriction();
		if (restriction != null) {
			ccq.where(restriction);
		}

		TypedQuery<Long> ctq = getEntityManager().createQuery(ccq);

		Long totalCount = ctq.getSingleResult();

		Pager<E> result = new Pager<E>(pager);
		if (totalCount == null || totalCount <= 0) {
			return result;
		}
		int startIndex = (pageNumber - 1) * pageSize;

		// 2.加入pager中传入的排序
		String orderBy = pager.getOrderBy();
		Direction orderType = pager.getOrderType();

		if (StringUtils.isNotEmpty(orderBy) && orderType != null) {
			if (orderType == Direction.asc) {
				ocq.orderBy(cb.asc(root.get(orderBy)));
			} else {
				ocq.orderBy(cb.desc(root.get(orderBy)));
			}
		}

		TypedQuery<E> query = getEntityManager().createQuery(ocq);


		// 从起始页码查询指定条数的记录
		query.setFirstResult(startIndex);
		query.setMaxResults(pageSize);
		result.setTotalCount(totalCount.intValue());

		List<OrderSearch> resultList = (List<OrderSearch>) query
				.getResultList();


		result.setList((List<E>) resultList);

		return result;
	}
	
	
	
	private boolean isNull(Object obj){
		return obj==null?true:false;
	}
	
	
	
	private List<OrderSearch> convertOrderSearchSqlList(List<Object[]> resultList){
		List<OrderSearch> list =new ArrayList<OrderSearch>();
		OrderSearch orderSearch = null;
		if(resultList!=null && !resultList.isEmpty()){
			for ( Object[] values : resultList ) {
				orderSearch = new OrderSearch();
			    orderSearch.setId(isNull(values[0])?null:((BigInteger) values[0]).longValue());
			    orderSearch.setOrderNo(isNull(values[1])?null:(String)values[1]);
			    orderSearch.setMemberNo(isNull(values[2])?null:(String)values[2]);
			    orderSearch.setOrderTime(isNull(values[3])?null:(Date)values[3]);
			    orderSearch.setFinishTime(isNull(values[4])?null:(Date)values[4]);
			    
			    orderSearch.setOrderSource(isNull(values[5])?null:(String)values[5]);
			    orderSearch.setOrderType(isNull(values[6])?null:(String)values[6]);
			    orderSearch.setMerchantNo(isNull(values[7])?null:(String)values[7]);
			    orderSearch.setOrderRelatedOrigin(isNull(values[8])?null:(String)values[8]);
			    orderSearch.setFinishUserNo(isNull(values[9])?null:(String)values[9]);
			    
			    orderSearch.setConfirmerName(isNull(values[10])?null:(String)values[10]);
			    orderSearch.setTotalProductPrice(isNull(values[11])?null:(BigDecimal)values[11]);
			    orderSearch.setDiscountTotal(isNull(values[12])?null:(BigDecimal)values[12]);
			    orderSearch.setTransportFee(isNull(values[13])?null:(BigDecimal)values[13]);
			    orderSearch.setDiscountTransport(isNull(values[14])?null:(BigDecimal)values[14]);
			    
			    orderSearch.setBillType(isNull(values[15])?null:((Integer)values[15]).longValue());
			    orderSearch.setDateCreated(isNull(values[16])?null:(Date)values[16]);
			    orderSearch.setTotalPayAmount(isNull(values[17])?null:(BigDecimal)values[17]);
			    orderSearch.setCustomerName(isNull(values[18])?null:(String)values[18]);
			    orderSearch.setOrderCategory(isNull(values[19])?null:(String)values[19]);
			    
			    orderSearch.setClientServiceRemark(isNull(values[20])?null:(String)values[20]);
			    orderSearch.setRemark(isNull(values[21])?null:(String)values[21]);
			    orderSearch.setIfPriviledgedMember(isNull(values[22])?null:((Integer)values[22]).longValue());
			    orderSearch.setIfWarnOrder(isNull(values[23])?null:((Integer)values[23]).longValue());
			    orderSearch.setAliasOrderNo(isNull(values[24])?null:(String)values[24]);
			    
			    orderSearch.setIfNeedRefund(isNull(values[25])?null:((Integer)values[25]).longValue());
			    orderSearch.setCreatedBy(isNull(values[26])?null:(String)values[26]);
			    orderSearch.setIfPayOnArrival(isNull(values[27])?null:((Integer)values[27]).longValue());
			    orderSearch.setWeight(isNull(values[28])?null:(BigDecimal)values[28]);
			    orderSearch.setConfirmTime(isNull(values[29])?null:(Date)values[29]);
			    
			    orderSearch.setChgOurOrderNo(isNull(values[30])?null:(String)values[30]);
			    orderSearch.setOrderSubId(isNull(values[31])?null:((BigInteger)values[31]).longValue());
			    orderSearch.setDistributeType(isNull(values[32])?null:(String)values[32]);
			    orderSearch.setAddressCode(isNull(values[33])?null:(String)values[33]);
			    orderSearch.setAddressDetail(isNull(values[34])?null:(String)values[34]);
			    
			    orderSearch.setLogisticsStatus(isNull(values[35])?null:(String)values[35]);
			    orderSearch.setUserName(isNull(values[36])?null:(String)values[36]);
			    orderSearch.setSelfFetchAddress(isNull(values[37])?null:(String)values[37]);
			    orderSearch.setMobPhoneNum(isNull(values[38])?null:(String)values[38]);
			    orderSearch.setPhoneNum(isNull(values[39])?null:(String)values[39]);
			    
			    orderSearch.setInvoicePrinted(isNull(values[40])?null:((Integer)values[40]).longValue());
			    orderSearch.setOrderSubNo(isNull(values[41])?null:(String)values[41]);
			    orderSearch.setDeliveryMerchantNo(isNull(values[42])?null:(String)values[42]);
			    orderSearch.setDeliveryMerchantName(isNull(values[43])?null:(String)values[43]);
			    orderSearch.setCheckCode(isNull(values[44])?null:(String)values[44]);
			    
			    orderSearch.setStatusTotal(isNull(values[45])?null:(String)values[45]);
			    orderSearch.setStatusPay(isNull(values[46])?null:(String)values[46]);
			    orderSearch.setStatusConfirm(isNull(values[47])?null:(String)values[47]);
			    orderSearch.setLogisticsOutNo(isNull(values[48])?null:(String)values[48]);
			    orderSearch.setOrderSubRelatedOrigin(isNull(values[49])?null:(String)values[49]);
			    orderSearch.setIfBlackListMember(isNull(values[50])?null:((Integer)values[50]).longValue());
			    orderSearch.setChgOurOrderNo(isNull(values[51])?null:((String)values[51]));
			    orderSearch.setUpdatedBy(isNull(values[52])?null:((String)values[52]));
			    orderSearch.setMerchantType(isNull(values[53])?null:((String)values[53]));
			    orderSearch.setOutStoreTime(isNull(values[54])?null:((Date)values[54]));
			    orderSearch.setClientRemark(isNull(values[55])?null:((String)values[55]));
			    orderSearch.setSellerMessage(isNull(values[56])?null:((String)values[56]));
			    orderSearch.setIsSplit(isNull(values[57])?null:((Integer)values[57]));
			    list.add(orderSearch);
			}
		}
		return list;
	}
	
	public static Date getStartDate(Date time) {
		
		time = org.apache.commons.lang.time.DateUtils.setHours(time, 0);
		time = org.apache.commons.lang.time.DateUtils.setMinutes(time, 0);
		time = org.apache.commons.lang.time.DateUtils.setSeconds(time, 0);
		time = org.apache.commons.lang.time.DateUtils.setMilliseconds(time, 0);
		
		return time;
	}
	
	public static Date getEndDate(Date time) {
		
		time = org.apache.commons.lang.time.DateUtils.setHours(time, 23);
		time = org.apache.commons.lang.time.DateUtils.setMinutes(time, 59);
		time = org.apache.commons.lang.time.DateUtils.setSeconds(time, 59);
		time = org.apache.commons.lang.time.DateUtils.setMilliseconds(time, 999);
		
		return time;
	}
	
	public String querySql(OrderSearch order,List<String> statusPayOther, List<String> statusTotalOther, int columnId) {
		
		//List<String> addressCodes = setAddressCodeByTransportAreaId(order);
	   StringBuffer sb = new StringBuffer();
	   sb.append(" SELECT ");
		if (null != order.getProductYear() 
				|| StringUtils.isNotBlank(order.getSkuNo())
				|| StringUtils.isNotBlank(order.getCommodityName()) 
				|| StringUtils.isNotBlank(order.getCommodityCode()) 
				|| StringUtils.isNotBlank(order.getSupplierCode())
				|| null != order.getPayTimeFrom()
				|| null != order.getPayTimeTo()
				|| StringUtils.isNotBlank(order.getPayCode())
				|| null!=(order.getSkuNumMax())
			    || null!=(order.getSkuNumMin())
				) {
			sb.append(" distinct ");

		
		}
		  
				
		
	   sb.append(" ordersearc0_.id   as col_0_0_,");
	   sb.append("ordersearc0_.ORDER_NO                   as col_1_0_,");
	   sb.append(" ordersearc0_.MEMBER_NO                  as col_2_0_,");
	   sb.append("ordersearc0_.ORDER_TIME                 as col_3_0_,");
	   sb.append("ordersearc0_.FINISH_TIME                as col_4_0_,");
	   sb.append("ordersearc0_.ORDER_SOURCE               as col_5_0_,");
	   sb.append("ordersearc0_.ORDER_TYPE                 as col_6_0_,");
	   sb.append("ordersearc0_.MERCHANT_NO                as col_7_0_,");
	   sb.append("ordersearc0_.ORDER_RELATED_ORIGIN       as col_8_0_,");
	   sb.append("ordersearc0_.FINISH_USER_NO             as col_9_0_,");
	   sb.append("ordersearc0_.CONFIRMER_NAME             as col_10_0_,");
	   sb.append("ordersearc0_.TOTAL_PRODUCT_PRICE        as col_11_0_,");
	   sb.append("ordersearc0_.DISCOUNT_TOTAL             as col_12_0_,");
	   sb.append("ordersearc0_.TRANSPORT_FEE              as col_13_0_,");
	   sb.append("ordersearc0_.DISCOUNT_TRANSPORT         as col_14_0_,");
	   sb.append("ordersearc0_.BILL_TYPE                  as col_15_0_,");
	   sb.append("ordersearc0_.DATE_CREATED               as col_16_0_,");
	   sb.append("ordersearc0_.TOTAL_PAY_AMOUNT           as col_17_0_,");
	   sb.append("ordersearc0_.CUSTOMER_NAME              as col_18_0_,");
	   sb.append("ordersearc0_.ORDER_CATEGORY             as col_19_0_,");
	   sb.append("ordersearc0_.CLIENT_SERVICE_REMARK      as col_20_0_,");
	   sb.append("ordersearc0_.REMARK                     as col_21_0_,");
	   sb.append("ordersearc0_.IF_PRIVILEDGED_MEMBER      as col_22_0_,");
	   sb.append("ordersearc0_.IF_WARN_ORDER              as col_23_0_,");
	   sb.append("ordersearc0_.ALIAS_ORDER_NO             as col_24_0_,");
	   sb.append("ordersearc0_.IF_NEED_REFUND             as col_25_0_,");
	   sb.append("ordersearc0_.CREATED_BY                 as col_26_0_,");
	   sb.append("ordersearc0_.IF_PAY_ON_ARRIVAL          as col_27_0_,");
	   sb.append("ordersearc0_.WEIGHT                     as col_28_0_,");
	   sb.append("ordersearc0_.CONFIRM_TIME               as col_29_0_,");
	   sb.append("ordersearc0_.CHGOUT_ORDER_NO            as col_30_0_,");
	   sb.append("ordersearc0_2_.ID                       as col_31_0_,");
	   sb.append("ordersearc0_2_.DISTRIBUTE_TYPE          as col_32_0_,");
	   sb.append("ordersearc0_2_.ADDRESS_CODE             as col_33_0_,");
	   sb.append("ordersearc0_2_.ADDRESS_DETAIL           as col_34_0_,");
	   sb.append("ordersearc0_2_.LOGISTICS_STATUS         as col_35_0_,");
	   sb.append("ordersearc0_2_.USER_NAME                as col_36_0_,");
	   sb.append("ordersearc0_2_.SELF_FETCH_ADDRESS       as col_37_0_,");
	   sb.append("ordersearc0_2_.MOB_PHONE_NUM            as col_38_0_,");
	   sb.append("ordersearc0_2_.PHONE_NUM                as col_39_0_,");
	   sb.append("ordersearc0_2_.INVOICE_PRINTED          as col_40_0_,");
	   sb.append("ordersearc0_2_.ORDER_SUB_NO             as col_41_0_,");
	   sb.append("ordersearc0_2_.DELIVERY_MERCHANT_NO     as col_42_0_,");
	   sb.append("ordersearc0_2_.DELIVERY_MERCHANT_NAME   as col_43_0_,");
	   sb.append("ordersearc0_2_.CHECK_CODE               as col_44_0_,");
	   sb.append("ordersearc0_.STATUS_TOTAL               as col_45_0_,");
	   sb.append("ordersearc0_.STATUS_PAY                 as col_46_0_,");
	   sb.append("ordersearc0_.STATUS_CONFIRM             as col_47_0_,");
	   sb.append("ordersearc0_2_.LOGISTICS_OUT_NO         as col_48_0_,");
	   sb.append("ordersearc0_2_.ORDER_SUB_RELATED_ORIGIN as col_49_0_,");
	   sb.append("ordersearc0_.IF_BLACKLIST_MEMBER        as col_50_0_,");
	   sb.append("ordersearc0_.CHGOUT_ORDER_NO            as col_51_0_,");
	   sb.append("ordersearc0_.UPDATED_BY                 as col_52_0_,");
	   sb.append("ordersearc0_.MERCHANT_TYPE              as col_53_0_,");
	   sb.append("ordersearc0_2_.OUT_STORE_TIME           as col_54_0_,");
	   sb.append("ordersearc0_.CLIENT_REMARK              as col_55_0_,");
	   sb.append("ordersearc0_.SELLER_MESSAGE             as col_56_0_,");
	   sb.append("ordersearc0_.IS_SPLIT             as col_57_0_");

		if ( null!=(order.getSkuNumMax())
			    || null!=(order.getSkuNumMin())
				) {
			sb.append(", ordersearc0_1_.ORDER_SUB_NO ,");
			sb.append(" SUM(ordersearc0_1_.SALE_NUM) AS sku ");
			
		}
	   //where
	   sb.append(" from ORDER_MAIN ordersearc0_");
	   sb.append(" left join ORDER_SUB ordersearc0_2_ on ordersearc0_.id = ordersearc0_2_.ID_ORDER");
	   if(StringUtils.isNotBlank(order.getCommodityName()) 
			   || StringUtils.isNotBlank(order.getSupplierCode())
			   || null != order.getProductYearStart() 
			   || null != order.getProductYearEnd() 
			   || StringUtils.isNotBlank(order.getSkuNo())
			   || StringUtils.isNotBlank(order.getCommodityCode())
			   || null!=(order.getSkuNumMax())
			   || null!=(order.getSkuNumMin())
			   ){
		   sb.append(" left join  ORDER_ITEM ordersearc0_1_  on ordersearc0_.id = ordersearc0_1_.ID_ORDER");
	   }
	   //增加2个判定条件 有支付日期时搜索

		   if(StringUtils.isNotBlank(order.getPayNo())
				   || isNotAllString(order.getPayCode())
				   || null != order.getPayTimeFrom()
				   || null != order.getPayTimeTo()
				   ){
		   sb.append(" left join ORDER_PAY  ordersearc0_3_  on ordersearc0_.id = ordersearc0_3_.ID_ORDER   AND  ordersearc0_3_.PAY_NO is not null");
		   }
	   //END YUSL 1/12
	   
	   sb.append(" where 1=1 ");

	   // 收货人信息统一搜索
	   if (StringUtils.isNotBlank(order.getReceiverInfo())) {
		   sb.append(" and  (ordersearc0_2_.USER_NAME like '%").append(order.getReceiverInfo()).append("%'");
		   sb.append(" or ordersearc0_2_.MOB_PHONE_NUM like '%").append(order.getReceiverInfo()).append("%'");
		   sb.append(" or ordersearc0_2_.PHONE_NUM like '%").append(order.getReceiverInfo()).append("%'");
		   sb.append(" or ordersearc0_2_.ADDRESS_DETAIL like '%").append(order.getReceiverInfo()).append("%')");
	   }
		if (StringUtils.isNotBlank(order.getAddressDetail())) {
			if ("1".equals(order.getIsAddressDetail())) {
				   sb.append(" and ordersearc0_.CLIENT_REMARK like '%").append(order.getClientRemark()).append("%'");
				
			}else if ("0".equals(order.getIsAddressDetail())) {
				   sb.append(" and ordersearc0_.CLIENT_REMARK not like '%").append(order.getClientRemark()).append("%'");
				
			}
		}
	   // 会员信息统一搜索
	   if (StringUtils.isNotBlank(order.getMemberInfo())) {
	       sb.append(" and  (ordersearc0_.CUSTOMER_NAME like '%").append(order.getMemberInfo()).append("%'");
	       sb.append(" or ordersearc0_.MEMBER_NO like '%").append(order.getMemberInfo()).append("%')");
	   }
		if (StringUtils.isNotBlank(order.getClientRemark())) {
			if ("1".equals(order.getIsClientRemark())) {
				   sb.append(" and ordersearc0_.CLIENT_REMARK like '%").append(order.getClientRemark()).append("%'");
				
			}else if ("0".equals(order.getIsClientRemark())) {
				   sb.append(" and ordersearc0_.CLIENT_REMARK not like '%").append(order.getClientRemark()).append("%'");
				
			}
		}
		
		 if ("2".equals(order.getIsClientRemark())) {
			   sb.append(" and ordersearc0_.CLIENT_REMARK is null");
				
		}
			if (StringUtils.isNotBlank(order.getClientServiceRemark())) {
				if ("1".equals(order.getIsRemark())) {
					   sb.append(" and ordersearc0_.CLIENT_SERVICE_REMARK like '%").append(order.getClientServiceRemark()).append("%'");
					
				}else if ("0".equals(order.getIsRemark())) {
					   sb.append(" and ordersearc0_.CLIENT_SERVICE_REMARK not like '%").append(order.getClientServiceRemark()).append("%'");
					
				}
			}
			if ("2".equals(order.getIsRemark())) {
				   sb.append(" and ordersearc0_.CLIENT_SERVICE_REMARK is null");
					
			}
			if (StringUtils.isNotBlank(order.getSellerMessage())) {
				if ("1".equals(order.getIsSellerMessage())) {
					   sb.append(" and ordersearc0_.seller_message like '%").append(order.getSellerMessage()).append("%'");
					
				}else if ("0".equals(order.getIsSellerMessage())) {
					   sb.append(" and ordersearc0_.seller_message not like '%").append(order.getSellerMessage()).append("%'");
					
				}
			}
			 if ("2".equals(order.getIsSellerMessage())) {
				   sb.append(" and ordersearc0_.seller_message is null");
					
			}
	   if(StringUtils.isNotBlank(order.getRefundType())){
		   if ("0".equals(order.getRefundType())) {

			   sb.append(" and ordersearc0_.refund_type  is null");
			
		}else{
		   sb.append(" and ordersearc0_.refund_type = '").append(order.getRefundType()).append("'");
		}
	   }
	   if(null != order.getIsSuspension()){
		   sb.append(" and ordersearc0_.is_suspension = ").append(order.getIsSuspension());
	   }
	   if(null != order.getIsMerge()){
		   sb.append(" and ordersearc0_.is_merge = ").append(order.getIsMerge());
	   }
	   if(null != order.getIsSplit()){
		   sb.append(" and ordersearc0_.is_split = ").append(order.getIsSplit());
	   }
	   if(null != order.getIsBarter()){
		   sb.append(" and ordersearc0_.is_barter = ").append(order.getIsBarter());
	   }
	   if(StringUtils.isNotBlank(order.getDeliveryType())){
		   sb.append(" and ordersearc0_.DELIVERY_TYPE = '").append(order.getDeliveryType()).append("'");
	   }
	   
	   //关联出拆单的父子单   我用了or or  or   我有罪
	   if(StringUtils.isNotBlank(order.getOrderSubNo())){
	       String[] orderSubNos = order.getOrderSubNo().split(",");
           if(orderSubNos.length==1){
               sb.append(" and ordersearc0_2_.ORDER_SUB_NO = '").append(order.getOrderSubNo()).append("'");
           }else{
               //update by 20141016 for 支持多个订单号一起搜索
               sb.append(" and (");
               String orderNoSql = "";
               int countTmp = 0;
               for(String orNo:orderSubNos){
                   countTmp++;
                   if(countTmp==1){
						orderNoSql = "( ordersearc0_2_.ORDER_SUB_NO = '" + orNo
								+ "'  OR ordersearc0_.ORDER_NO_P = (SELECT os.ORDER_NO FROM order_sub os WHERE os.ORDER_SUB_NO = '" + orNo
								+ "') OR ordersearc0_2_.ORDER_NO = (SELECT om.ORDER_NO_P FROM order_sub os INNER JOIN order_main om WHERE os.ORDER_SUB_NO ='"
								+ orNo + "' LIMIT 1 ))";
					} else {
						orderNoSql = orderNoSql + " or ( ordersearc0_2_.ORDER_SUB_NO = '" + orNo
								+ "'  OR ordersearc0_.ORDER_NO_P = (SELECT os.ORDER_NO FROM order_sub os WHERE os.ORDER_SUB_NO = '" + orNo
								+ "') OR ordersearc0_2_.ORDER_NO = (SELECT om.ORDER_NO_P FROM order_sub os INNER JOIN order_main om WHERE os.ORDER_SUB_NO ='"
								+ orNo + "' LIMIT 1 ))";
                   }
               }
               sb.append(orderNoSql).append(")");
           }
		   //sb.append(" and ordersearc0_2_.ORDER_SUB_NO = '").append(order.getOrderSubNo()).append("'");
	   }
	   
	   if(StringUtils.isNotBlank(order.getOrderNo())){
		   sb.append(" and ordersearc0_.ORDER_NO = '").append(order.getOrderNo()).append("'");
	   }
	   
	   if(order.getBillType()!=null){
		   sb.append(" and ordersearc0_.BILL_TYPE=").append(order.getBillType()); 
	   }
	   
	   if(StringUtils.isNotBlank(order.getOrderType())){
		   sb.append(" and ordersearc0_.ORDER_TYPE = '").append(order.getOrderType()).append("'");
	   }
	   if(StringUtils.isNotBlank(order.getDeliveryMerchantNo())){
		   
		   String[] deliveryMerchantNos = order.getDeliveryMerchantNo().replace(" ", "").split(",");
		 
		   for (int i = 0; i < deliveryMerchantNos.length; i++) {
				if(i == 0){
					sb.append(" and (ordersearc0_2_.DELIVERY_MERCHANT_NO = '").append(deliveryMerchantNos[0]).append("'");
				}
				else{
					 sb.append(" or ").append("ordersearc0_2_.DELIVERY_MERCHANT_NO = '").append(deliveryMerchantNos[i]).append("'");
					
				}

		        if (i==deliveryMerchantNos.length-1) {

				    sb.append(" )");
				}
		        
		}

	   }
	   if(StringUtils.isNotBlank(order.getStatusPay())){
		   sb.append(" and (ordersearc0_.STATUS_PAY = '").append(order.getStatusPay()).append("'");
		   
		   if (statusPayOther != null && statusPayOther.size() > 0) {

				for (String statusPay : statusPayOther) {
					 sb.append(" or ").append("ordersearc0_.STATUS_PAY = '").append(statusPay).append("'");
				}
			}
		   
		   sb.append(" )");
		   
		   if(columnId == OrderColumn.ORDER_REVERSE)
			{
			   sb.append(" and ordersearc0_.IF_NEED_REFUND = ").append(1l).append("");
			}
	   }
	   
	   if(StringUtils.isNotBlank(order.getStatusTotal())) {
			
			// 待支付时判断某些状态是否作不等条件查询  true: 当做不等条件查询   false:不当做不等条件查询
			if(order.getIsNotEqual4OrderNeedPay())
			{
				
				 sb.append(" and (ordersearc0_.STATUS_TOTAL <> '").append(order.getStatusTotal()).append("'");
				
				if (statusTotalOther != null && statusTotalOther.size() > 0) {
					for (String statusTotal : statusTotalOther) {
						 sb.append(" or ").append("ordersearc0_.STATUS_TOTAL <> '").append(statusTotal).append("'");
					}
				}
				 sb.append(" )");
			}
			else
			{
				 sb.append(" and (ordersearc0_.STATUS_TOTAL = '").append(order.getStatusTotal()).append("'");
				if (statusTotalOther != null && statusTotalOther.size() > 0) {
	
					for (String statusTotal : statusTotalOther) {
						 sb.append(" or ").append("ordersearc0_.STATUS_TOTAL = '").append(statusTotal).append("'");
					}
				}
				 sb.append(" )");
			}
		}
	   
	   
        // 根据多个支付方式(payCode)查询
        if (null != order.getPayCodeList() && order.getPayCodeList().size() > 0) {
            List<String> payCodeList = order.getPayCodeList();

            for (int i = 0; i < payCodeList.size(); i++) {
                if (i == 0) {
                    sb.append(" and (ordersearc0_3_.PAY_CODE = '").append(payCodeList.get(0)).append("'");
                } else {
                    sb.append(" or ").append("ordersearc0_3_.PAY_CODE = '").append(payCodeList.get(i))
                            .append("'");

                }

            }
            sb.append(" )");

        }
        
        
        // 根据多个处理状态查询
        if (null != order.getStatusTotalList() && order.getStatusTotalList().size() > 0) {
            List<String> statusTotalList = order.getStatusTotalList();

            for (int i = 0; i < statusTotalList.size(); i++) {
                if (i == 0) {
                    sb.append(" and (ordersearc0_.STATUS_TOTAL = '").append(statusTotalList.get(0)).append("'");
                } else {
                    sb.append(" or ").append("ordersearc0_.STATUS_TOTAL = '").append(statusTotalList.get(i))
                            .append("'");

                }

            }
            sb.append(" )");

        }
		// 根据多个物流状态查询
		if (StringUtils.isNotBlank(order.getLogisticsStatus())) {

			   String[] logisticsStatusList = order.getLogisticsStatus().replace(" ", "").split(",");
		    for(int i = 0;i< logisticsStatusList.length;i++){
		        if(i == 0){
		            sb.append(" and (ordersearc0_2_.LOGISTICS_STATUS = '").append(logisticsStatusList[0]).append("'");
		        }else{
		            sb.append(" or ").append("ordersearc0_2_.LOGISTICS_STATUS = '").append(logisticsStatusList[i]).append("'");
		            
		        }
		        if (i==logisticsStatusList.length-1) {

				    sb.append(" )");
				}
		        
		    }
		    
		}
		// 根据单个orderCategory查询
		if (isNotAllString(order.getOrderCategory())) {
		    sb.append(" and ordersearc0_.ORDER_CATEGORY = '").append(order.getOrderCategory()).append("'");
        }
		
		// 根据多个orderCategory查询
		if (null != order.getOrderCategoryList()&& order.getOrderCategoryList().size() > 0) {
			List<String> orderCategoryList = order.getOrderCategoryList();
			for(int i = 0;i< orderCategoryList.size();i++){
				if(i == 0){
					sb.append(" and (ordersearc0_.ORDER_CATEGORY = '").append(orderCategoryList.get(0)).append("'");
				}
				else{
					 sb.append(" or ").append("ordersearc0_.ORDER_CATEGORY = '").append(orderCategoryList.get(i)).append("'");
					
				}
				
			}
			 sb.append(" )");
		}
		
		String areaCode = "";
		if (null != order.getState()){
			areaCode = String.valueOf(order.getState()).substring(0,2);
		}
		if (null != order.getCity()){
			areaCode = String.valueOf(order.getCity()).substring(0,4);
		}
		if (null != order.getCounty()){
			areaCode = String.valueOf(order.getCounty());
		}
		if (StringUtils.isNotBlank(areaCode)) { // 配送地址
			sb.append(" and LOCATE("+areaCode+",ordersearc0_2_.ADDRESS_CODE)>0");
		}
		
	   
	// 逆向单不需查询“运费补款单”
		if(columnId == OrderColumn.ORDER_REVERSE){
			sb.append(" and ordersearc0_.ORDER_CATEGORY <> '").append(CommonConst.OrderMain_OrderCategory_TransportFee.getCode()).append("'");
		}
	   
		// 供应商编码
		if (isNotAllString(order.getSupplierCode())) {
			sb.append(" and ordersearc0_1_.SUPPLIER_CODE = '").append(order.getSupplierCode()).append("'");
		}
	    //TODO商品年份区间查询
		// 商品年份
		if (null != order.getProductYearStart()) {
			sb.append(" and ordersearc0_1_.product_year >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(order.getProductYearStart()) + "','%Y-%m-%d')");
		}
		if (null != order.getProductYearEnd()) {
			sb.append(" and ordersearc0_1_.product_year <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(order.getProductYearEnd()) + "','%Y-%m-%d')");
		}
		
		// 商品sku
		if (isNotAllString(order.getSkuNo())) {
		if ("1".equals(order.getIsSkuNo())) {
			sb.append(" and ordersearc0_1_.SKU_NO like '%").append(order.getSkuNo()).append("%'");
			
		}else if ("0".equals(order.getIsSkuNo())) {
			sb.append(" and ordersearc0_1_.SKU_NO not like '%").append(order.getSkuNo()).append("%'");
			
		}
		}
		// 商品货号
		if (isNotAllString(order.getCommodityCode())) {
		if ("1".equals(order.getIsCommodityCode())) {
			sb.append(" and ordersearc0_1_.COMMODITY_CODE like '%").append(order.getCommodityCode()).append("%'");
			
		}else if ("0".equals(order.getIsCommodityCode())) {
			sb.append(" and ordersearc0_1_.COMMODITY_CODE not like '%").append(order.getCommodityCode()).append("%'");
			
		}
		
		}
		
		// 需退款(网上天虹财务) / 门店退款
		if(null != order.getIfNeedRefundList() && order.getIfNeedRefundList().size() > 0){
			List<Long> ifNeedRefundList = order.getIfNeedRefundList();
			if(ifNeedRefundList!=null && !ifNeedRefundList.isEmpty()){
				
				sb.append(" and (ordersearc0_.IF_NEED_REFUND = ").append(ifNeedRefundList.get(0));
			
				for(int i = 1;i< ifNeedRefundList.size();i++){
					 sb.append(" or ").append("ordersearc0_.IF_NEED_REFUND = ").append(ifNeedRefundList.get(i));
				}
				sb.append(" )");
			
			}
		}
	   
	   if(StringUtils.isNotBlank(order.getOrderSource())){
		   sb.append(" and ordersearc0_.ORDER_SOURCE = '").append(order.getOrderSource()).append("'");
	   }
	   
	   if (isNotAllString(order.getMemberNo())) {
		   sb.append(" and ordersearc0_.MEMBER_NO = '").append(order.getMemberNo()).append("'");
		}
	   
	   if (isNotAllString(order.getOrderRelatedOrigin())) {
			  sb.append(" and ordersearc0_.ORDER_RELATED_ORIGIN = '").append(order.getOrderRelatedOrigin()).append("'");
		}
	   
		if (isNotAllString(order.getConfirmerName())) {
			sb.append(" and ordersearc0_.CONFIRMER_NAME = '").append(order.getConfirmerName()).append("'");
		}
	   
	   if(StringUtils.isNotBlank(order.getStatusConfirm())){
		   sb.append(" and ordersearc0_.STATUS_CONFIRM = '").append(order.getStatusConfirm()).append("'");
	   }
	   
		if (isNotAllString(order.getFinishStatus())) {
			if ("0".equalsIgnoreCase(order.getFinishStatus())) {
				sb.append(" and ordersearc0_.FINISH_USER_NO is null ");
			} else {
				sb.append(" and ordersearc0_.FINISH_USER_NO is not null ");
			}
		}
		//支付完成时间段 增加 查询 并修改查询语句 由ORCALE 到MYSQL
		Date payTimeFrom = order.getPayTimeFrom();
		Date payTimeTo = order.getPayTimeTo();
		if (!(payTimeFrom == null) && !(payTimeTo == null)) {
			sb.append(" and ordersearc0_3_.PAY_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(payTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
			sb.append(" and ordersearc0_3_.PAY_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(payTimeTo) + "','%Y-%m-%d %H:%i:%s')");
		} else if (!(null == payTimeFrom)) {
			sb.append(" and ordersearc0_3_.PAY_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(payTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
		} else if (!(null == payTimeTo)) {
			sb.append(" and ordersearc0_3_.PAY_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(payTimeTo) + "','%Y-%m-%d %H:%i:%s')");
		
		}

		Integer skuNumMax = order.getSkuNumMax();
		Integer skuNumMin = order.getSkuNumMin();
		if (!(skuNumMax == null) && !(skuNumMax == null)) {
			sb.append("");
			
		} 
		//下单时间段
		Date orderTimeFrom = order.getOrderTimeFrom();
		Date orderTimeTo = order.getOrderTimeTo();
		if (!(orderTimeFrom == null) && !(orderTimeTo == null)) {
			sb.append(" and ordersearc0_.ORDER_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(orderTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
			sb.append(" and ordersearc0_.ORDER_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(orderTimeTo) + "','%Y-%m-%d %H:%i:%s')");
		} else if (!(null == orderTimeFrom)) {
			sb.append(" and ordersearc0_.ORDER_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(orderTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
		} else if (!(null == orderTimeTo)) {
			sb.append(" and ordersearc0_.ORDER_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(orderTimeTo) + "','%Y-%m-%d %H:%i:%s')");
		}
		
		// 订单完成时间段
		Date finishTimeFrom = order.getFinishTimeFrom();
		Date finishTimeTo = order.getFinishTimeTo();
		if (!(finishTimeFrom == null) && !(finishTimeTo == null)) {
		    finishTimeFrom = getStartDate(finishTimeFrom); //此方法只会截图年、月、日，时、分、秒会重新组装
		    finishTimeTo = getEndDate(finishTimeTo);
		    sb.append(" and ordersearc0_.FINISH_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(finishTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
		    sb.append(" and ordersearc0_.FINISH_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(finishTimeTo) + "','%Y-%m-%d %H:%i:%s')");
		} else if (!(null == finishTimeFrom)) {
		    sb.append(" and ordersearc0_.FINISH_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(finishTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
		} else if (!(null == finishTimeTo)) {
		    sb.append(" and ordersearc0_.FINISH_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(finishTimeTo) + "','%Y-%m-%d %H:%i:%s')");
		}
		
		//审核时间段
		Date confirmTimeFrom = order.getConfirmTimeFrom();
        Date confirmTimeTo = order.getConfirmTimeTo();
        if (!(confirmTimeFrom == null) && !(confirmTimeTo == null)) {
            sb.append(" and ordersearc0_.CONFIRM_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(confirmTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
            sb.append(" and ordersearc0_.CONFIRM_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(confirmTimeTo) + "','%Y-%m-%d %H:%i:%s')");
        } else if (!(null == confirmTimeFrom)) {
            sb.append(" and ordersearc0_.CONFIRM_TIME >= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(confirmTimeFrom) + "','%Y-%m-%d %H:%i:%s')");
        } else if (!(null == confirmTimeTo)) {
            sb.append(" and ordersearc0_.CONFIRM_TIME <= " + "str_to_date('" + DateUtils.formatGeneralDateTimeFormat(confirmTimeTo) + "','%Y-%m-%d %H:%i:%s')");
        }
        //END 1/15 YUSL
		
		if(StringUtils.isNotBlank(order.getAliasOrderNo())){
			 sb.append(" and ordersearc0_.ALIAS_ORDER_NO = '").append(order.getAliasOrderNo()).append("'");
		}
			
		if(StringUtils.isNotBlank(order.getDistributeType())){
			 sb.append(" and ordersearc0_2_.DISTRIBUTE_TYPE = '").append(order.getDistributeType()).append("'");
		}
		
		
		if(StringUtils.isNotBlank(order.getSelfFetchAddress())){
			 sb.append(" and ordersearc0_2_.SELF_FETCH_ADDRESS = '").append(order.getSelfFetchAddress()).append("'");
		}
		
		if(StringUtils.isNotBlank(order.getCheckCode())){
			 sb.append(" and ordersearc0_2_.CHECK_CODE = '").append(order.getCheckCode()).append("'");
		}
		
		if (order.getAmountDown()!=null) {
			sb.append(" and ordersearc0_.TOTAL_PAY_AMOUNT <= ").append(order.getAmountDown());
		}
		
		if(order.getAmountUp()!=null){
			sb.append(" and ordersearc0_.TOTAL_PAY_AMOUNT >= ").append(order.getAmountUp());
		}
		
		if (StringUtils.isNotBlank(order.getCommodityName())) {
			if ("1".equals(order.getIsCommodityName())) {
				   sb.append(" and ordersearc0_1_.COMMODITY_NAME like '%").append(order.getCommodityName()).append("%'");
				
			}else if ("0".equals(order.getIsCommodityName())) {
				   sb.append(" and ordersearc0_1_.COMMODITY_NAME not like '%").append(order.getCommodityName()).append("%'");
				
			}
			}
		if (isNotAllInt(order.getIfPriviledgedMember())) {
			if(order.getIfPriviledgedMember()==1){
				sb.append(" and ordersearc0_.IF_PRIVILEDGED_MEMBER = ").append(order.getIfPriviledgedMember());
			}else{
				sb.append(" and (ordersearc0_.IF_PRIVILEDGED_MEMBER = ").append(order.getIfPriviledgedMember()).append(" or ordersearc0_.IF_PRIVILEDGED_MEMBER is null) ");
			}
		}
		
		if (isNotAllInt(order.getIfWarnOrder())) {
			if(order.getIfWarnOrder()==1){
				sb.append(" and ordersearc0_.IF_WARN_ORDER = ").append(order.getIfWarnOrder());
			}else{
				sb.append(" and (ordersearc0_.IF_WARN_ORDER = ").append(order.getIfWarnOrder()).	append(" or ordersearc0_.IF_WARN_ORDER is null )");
			}
		}
		
		if (StringUtils.isNotBlank(order.getOrderSubRelatedOrigin())) {
			 sb.append(" and ordersearc0_2_.ORDER_SUB_RELATED_ORIGIN = '").append(order.getOrderSubRelatedOrigin()).append("'");
        }
		
		if (isNotAllString(order.getPurchase())) {
			 sb.append(" and ordersearc0_.MERCHANT_NO <> '").append(order.getPurchase()).append("'");//采购
		}
		
		if (order.getIfPayOnArrival()!=null) {
			 sb.append(" and ordersearc0_.IF_PAY_ON_ARRIVAL = ").append(order.getIfPayOnArrival());
			
		}
		
		if (isNotAllInt(order.getSelfTakePointId())) {
			// sb.append(" and ordersearc0_2_.IF_PAY_ON_ARRIVAL = ").append(order.getIfPayOnArrival());
			sb.append(" and ordersearc0_2_.SELF_FETCH_ADDRESS = '").append(order.getSelfTakePointId().toString()).append("'");
		}
		// 商户（多个自提点id）
        if(null != order.getSelfTakePointIdList() && order.getSelfTakePointIdList().size() > 0)
        {
            List<String> selfTakePointIdList = order.getSelfTakePointIdList();
            sb.append(" and ordersearc0_2_.SELF_FETCH_ADDRESS in (");
       
            for (int i = 0; i < selfTakePointIdList.size(); i++) {
                if(i ==  selfTakePointIdList.size()-1){
                    sb.append("'").append( selfTakePointIdList.get(i)).append("'");
                }else{
                    sb.append("'").append( selfTakePointIdList.get(i)).append("' ,");
                }
            }
            
          
            sb.append(") ");
        }
        // 支付方式
        if (isNotAllString(order.getPayCode())) {
            sb.append(" and ordersearc0_3_.PAY_CODE = '").append(order.getPayCode()).append("'");
        }
        // 支付号
        if(StringUtils.isNotBlank(order.getPayNo())){
            sb.append(" and ordersearc0_3_.PAY_NO = '").append(order.getPayNo()).append("'");
        }
        
        // 经手人
        if (isNotAllString(order.getCreatedBy())) {
            sb.append(" and ordersearc0_.CREATED_BY = '").append(order.getCreatedBy()).append("'");
        }
        // 黑名单
        if (isNotAllInt(order.getIfBlackListMember())) {
            sb.append(" and ordersearc0_.IF_BLACKLIST_MEMBER = ").append(order.getIfBlackListMember());
        }
        
		if (isNotAllString(order.getUserName())) {
			   sb.append(" and ordersearc0_2_.USER_NAME = '").append(order.getUserName()).append("'");
		}
		
		if (StringUtils.isNotBlank(order.getMobPhoneNum())) {
			  sb.append(" and ordersearc0_2_.MOB_PHONE_NUM = '").append(order.getMobPhoneNum()).append("'");
		}
        if (isNotAllString(order.getChgOurOrderNo())) {
            sb.append(" and ordersearc0_.CHGOUT_ORDER_NO = '")
                    .append(order.getChgOurOrderNo().substring(0, order.getChgOurOrderNo().length() - 2)).append("'");
        }
        if (isNotAllString(order.getRemark())) {
            sb.append(" and ordersearc0_.REMARK like '%").append(order.getRemark()).append("%'");
        }
        if (isNotAllString(order.getMerchantNo())) {
            sb.append(" and ordersearc0_.MERCHANT_NO = '").append(order.getMerchantNo()).append("'");//商家
        }
        if (isNotAllString(order.getMerchantType())) {
            sb.append(" and ordersearc0_.MERCHANT_TYPE = '").append(order.getMerchantType()).append("'");
        }
        
        
		Integer skuMax =order.getSkuNumMax();
		Integer skuMin =order.getSkuNumMin();
		 if (!(skuMax == null) && !(skuMin == null)) {
	            sb.append(" GROUP BY 	ordersearc0_1_.ORDER_SUB_NO HAVING SKU  BETWEEN'" +skuMin+ "' and '"+skuMax+"'");
	        } else if (!(null == skuMax)) {
	            sb.append(" GROUP BY ordersearc0_1_.ORDER_SUB_NO HAVING SKU <='"+skuMax+"'");
	        } else if (!(null == skuMin)) {

	            sb.append(" GROUP BY ordersearc0_1_.ORDER_SUB_NO HAVING SKU >='"+skuMin+"'");
	        }
			// 根据下单时间排序
			sb.append(" order by ordersearc0_.ORDER_TIME desc");
	   return sb.toString();
	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public Pager queryDateBySql(OrderSearch order,List<String> statusPayOther, List<String> statusTotalOther, int columnId, Pager pager) {
		String querySql =	querySql(order,statusPayOther,statusTotalOther,columnId);
		
		StringBuffer totalHqlHeader = new StringBuffer("select count(1) from ( ");
		
		SQLQuery totalQuery = getSession().createSQLQuery(totalHqlHeader.append(querySql).append( " )t ").toString());
		SQLQuery recordQuery = getSession().createSQLQuery(querySql);
		
		// 查总数
		List totalList = totalQuery.list();
		Object totalObj = (Object) totalList.get(0);
		pager.setTotalCount(Integer.parseInt(totalObj.toString()));

		// 查分页记录
		recordQuery.setFirstResult(pager.getStart()).setMaxResults(pager.getPageSize());
		
		List<OrderSearch> orderReportLists = convertOrderSearchSqlList(recordQuery.list());
	
		pager.setList(orderReportLists);
		return pager;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public List<OrderSearch>  queryDateListBySql(OrderSearch order,List<String> statusPayOther, List<String> statusTotalOther, int columnId, Pager pager) {
		String querySql =	querySql(order,statusPayOther,statusTotalOther,columnId);
		
		SQLQuery recordQuery = getSession().createSQLQuery(querySql);
		
		return convertOrderSearchSqlList(recordQuery.list());
		
	}
	/**
	 * Description:
	 * @param list
	 */


    //END YUSL 1/16
}
