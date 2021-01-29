package com.haoyong.preview.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.haoyong.preview.Enum.CommonEnum;
import com.haoyong.preview.common.BaseErrorInfoInterface;
import com.haoyong.preview.config.AliSMSConfig;
import com.haoyong.preview.exce.BizException;
import com.haoyong.preview.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-12 17:51
 **/
@Service
public class SmsServiceImpl implements SmsService {
    @Autowired
    AliSMSConfig aliSMSConfig;
    @Override
    public boolean sendSms(String phoneNumber, String randomCode) {
        DefaultProfile profile = DefaultProfile.getProfile(
                aliSMSConfig.getRegionId(),
                aliSMSConfig.getAccessKeyId(),
                aliSMSConfig.getAccessKeySecret()

        );
        DefaultAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(aliSMSConfig.getDomain());
        request.setSysVersion(aliSMSConfig.getVersion());
        request.setSysAction(aliSMSConfig.getAction());
        request.putQueryParameter("RegionId",aliSMSConfig.getRegionId());
        request.putQueryParameter("PhoneNumbers",phoneNumber);
        request.putQueryParameter("SignName",aliSMSConfig.getSignName());
        request.putQueryParameter("TemplateCode",aliSMSConfig.getCodeTemplate());
        JSONObject object = new JSONObject();
        object.put("code",randomCode);
        String templateParam = object.toString();
        request.putQueryParameter("TemplateParam",templateParam);

        try {
            CommonResponse response = client.getCommonResponse(request);
            Map<String,Object> map = JSON.parseObject(response.getData());
            if (("OK").equals(map.get("Code"))){
                return true;
            }else{
                return  false;
            }
        } catch (ClientException e) {
            throw new BizException(CommonEnum.SMS_SEND_FAILED);
        }

    }
}
