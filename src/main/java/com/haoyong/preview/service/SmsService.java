package com.haoyong.preview.service;

public interface SmsService {
    boolean sendSms(String phoneNumber,String randomCode);
}
