package za.co.qg;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import sun.applet.resources.MsgAppletViewer_zh_TW;

import javax.jms.*;

public class TestApp {
    public static void main(String[] args) {
        try {
            new TestApp().execute();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void execute() throws JMSException {
        System.out.println("Oh hi");
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        final Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = session.createQueue("expectedReceiptsService.in");
//        MessageConsumer consumer = session.createConsumer(queue);
        MessageConsumer consumer = session.createConsumer(queue, "endpoint='BC'");
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("textMessage = " + textMessage.getText());
                    if (message.getJMSReplyTo() != null) {
                        MessageProducer producer = session.createProducer(message.getJMSReplyTo());
                        String someRandomJson = new Gson().toJson(getResponse());
                        TextMessage responseMessage = session.createTextMessage(someRandomJson);
                        responseMessage.setJMSCorrelationID(textMessage.getJMSCorrelationID());
                        producer.send(responseMessage);
                        System.out.println("Responded");
                    }
                    message.acknowledge();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private MatchResult getResponse() {
        MatchResult matchResult = new MatchResult();
        matchResult.setCarrier("carrier");
        matchResult.setAccountNumber("123");
        matchResult.setInvoiceNumber("234");
        matchResult.setPolicyNumber("234");
        matchResult.setFirstName("hi");
        matchResult.setLastName("last");
        matchResult.setCompanyName("comp");
        matchResult.setAmount(12);
        matchResult.setMatchType("EXACT");
        matchResult.setSystemOfRecord("BC");
        return matchResult;
    }
}
