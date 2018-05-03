<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>提示信息 - Powered By ${systemConfig.systemName}</title>
<meta name="Author" content="IBM Team" />
<meta name="Copyright" content="IBM" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<#include "/WEB-INF/template/common/include.ftl">
<link href="${base}/resources/admin/css/message.css" rel="stylesheet" type="text/css" />
</head>
<script>
function reload(redirectionUrl){
  var date = new Date().getTime();
  if(redirectionUrl.indexOf('?')>-1){
  	 redirectionUrl=redirectionUrl+"&date="+date
  }else{
     redirectionUrl=redirectionUrl+"?date="+date;
  }
	 window.location.href=redirectionUrl;
}

function goback(){

   var date = new Date().getTime();
   var column =0;
   <#if column??>  
      column = ${(column)!};
   </#if>

    var url = document.referrer;
    if(url.indexOf('?')>-1){
       if(url.indexOf('column')>-1){
    	 url= url +"&date="+date;
       }else{
         url= url +"&column="+column+"&date="+date;
       }
    }else{
    	url = url+"?column="+column+"&date="+date;
    }
	 window.location.href=url;
}

</script>
<body class="message">
	<div class="body">
		<div class="messageBox">
			<div class="boxTop">
				<div class="boxTitle">提示信息&nbsp;</div>
				<a class="boxClose windowClose" href="#" hidefocus="true"></a>
			</div>
			<div class="boxMiddle">
				<div class="messageContent">
					<span class="icon success">&nbsp;</span>
					<span class="messageText">
						<#if (errorMessages?size > 0)!>
							<#list errorMessages as list>${list}<br></#list>
						<#elseif (actionMessages?size > 0)!>
							<#list actionMessages as list>${list}<br></#list>
						<#elseif (fieldErrors?size > 0)!>
							<#list (fieldErrors?keys)! as key>
								${fieldErrors[key]?replace('^\\[', '', 'r')?replace('\\]$', '', 'r')}<br>
							</#list>
						<#else>
							您的操作已成功!
						</#if>
					</span>
				</div>
				<input type="button" class="formButton messageButton" <#if redirectionUrl??>  onclick="reload('${redirectionUrl}')";
				<#else>onclick="goback()"</#if> value="确  定" hidefocus="true" />
			</div>
			<div class="boxBottom"></div>
		</div>
	</div>
</body>
</html>