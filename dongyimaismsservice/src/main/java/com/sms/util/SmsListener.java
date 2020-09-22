package com.sms.util;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component("smsListener")
public class SmsListener implements MessageListener {
    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {
        if (message instanceof MapMessage){
            MapMessage mapMessage = (MapMessage)message;
            try {
                System.out.println("收到短信发送请求--》mobile:"+mapMessage.getString("mobile")+"  param:"+mapMessage.getString("param"));
                HttpResponse response = smsUtil.sendSms(mapMessage.getString("mobile"), mapMessage.getString("param"));
                // 结果是 00000 则为正常
                System.out.println("data:"+response.toString());
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
