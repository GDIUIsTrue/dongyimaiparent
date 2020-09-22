package com.page.serviceImpl;

import com.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;

@Component
public class pageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage)message;
        try {
            String text = textMessage.getText();
            System.out.println("接收到消息："+text);
            boolean b = itemPageService.genItemHtml(Long.parseLong(text));
        } catch (JMSException | IOException e) {
            e.printStackTrace();
        }
    }
}
