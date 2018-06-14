package za.co.qg;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class TestApp {
    public static void main(String[] args) {
        try {
            new TestApp().execute();
        } catch (JMSException e) {
            System.out.println("bad");
            e.printStackTrace();
        }
    }

    private void execute() throws JMSException {
        System.out.println("Oh hi");
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        final Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = session.createQueue("ding");
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("textMessage = " + textMessage.getText());
                    if (message.getJMSReplyTo() != null) {
                        MessageProducer producer = session.createProducer(message.getJMSReplyTo());
                        producer.send(session.createTextMessage("I responded"));
                        System.out.println("Responded");
                    }
                    message.acknowledge();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
