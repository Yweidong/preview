package com.haoyong.preview.controller;

import com.haoyong.preview.common.ResultBody;
import com.haoyong.preview.constant.RedisConstant;
import com.haoyong.preview.service.SmsService;
import com.haoyong.preview.util.CodeUtil;
import com.haoyong.preview.util.PhoneUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-12 18:19
 **/
@RestController
@RequestMapping("/sms")
@CrossOrigin
public class SmsSendController {
    @Autowired
    SmsService smsService;
    @Autowired
    RedisTemplate redisTemplate;


    @PostMapping("/sendCodeSms/{phoneNumber}")
    public ResultBody SmsSendKaptcha(@PathVariable("phoneNumber") String phoneNumber) {

        if (PhoneUtil.isPhoneLegal(phoneNumber)) {
            String randomCode = CodeUtil.getRandomCode();
            boolean sign = smsService.sendSms(phoneNumber, randomCode);
            if (sign) {
                String key = RedisConstant.PHONE_CODE_KEY + phoneNumber;
                redisTemplate.opsForValue().set(key,randomCode,180, TimeUnit.SECONDS);
            }else {
                return ResultBody.error("发送短信失败");
            }

        }else {
            return ResultBody.error("手机号码不符合规则，请重新输入");
        }
        return ResultBody.success();
    }

}
