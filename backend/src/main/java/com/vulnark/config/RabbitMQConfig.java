package com.vulnark.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {
    
    // 扫描任务队列
    public static final String SCAN_TASK_QUEUE = "scan.task.queue";
    public static final String SCAN_TASK_EXCHANGE = "scan.task.exchange";
    public static final String SCAN_TASK_ROUTING_KEY = "scan.task.start";
    
    // 扫描结果队列
    public static final String SCAN_RESULT_QUEUE = "scan.result.queue";
    public static final String SCAN_RESULT_EXCHANGE = "scan.result.exchange";
    public static final String SCAN_RESULT_ROUTING_KEY = "scan.result.completed";
    
    // 扫描日志队列
    public static final String SCAN_LOG_QUEUE = "scan.log.queue";
    public static final String SCAN_LOG_EXCHANGE = "scan.log.exchange";
    public static final String SCAN_LOG_ROUTING_KEY = "scan.log.update";
    
    // 死信队列
    public static final String SCAN_DLX_QUEUE = "scan.dlx.queue";
    public static final String SCAN_DLX_EXCHANGE = "scan.dlx.exchange";
    public static final String SCAN_DLX_ROUTING_KEY = "scan.dlx";
    
    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
    
    /**
     * 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
    
    // ===================== 扫描任务队列配置 =====================
    
    @Bean
    public Queue scanTaskQueue() {
        return QueueBuilder.durable(SCAN_TASK_QUEUE)
                .withArgument("x-dead-letter-exchange", SCAN_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SCAN_DLX_ROUTING_KEY)
                .withArgument("x-message-ttl", 3600000) // 1小时TTL
                .build();
    }
    
    @Bean
    public DirectExchange scanTaskExchange() {
        return new DirectExchange(SCAN_TASK_EXCHANGE);
    }
    
    @Bean
    public Binding scanTaskBinding() {
        return BindingBuilder.bind(scanTaskQueue())
                .to(scanTaskExchange())
                .with(SCAN_TASK_ROUTING_KEY);
    }
    
    // ===================== 扫描结果队列配置 =====================
    
    @Bean
    public Queue scanResultQueue() {
        return QueueBuilder.durable(SCAN_RESULT_QUEUE)
                .withArgument("x-dead-letter-exchange", SCAN_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SCAN_DLX_ROUTING_KEY)
                .build();
    }
    
    @Bean
    public DirectExchange scanResultExchange() {
        return new DirectExchange(SCAN_RESULT_EXCHANGE);
    }
    
    @Bean
    public Binding scanResultBinding() {
        return BindingBuilder.bind(scanResultQueue())
                .to(scanResultExchange())
                .with(SCAN_RESULT_ROUTING_KEY);
    }
    
    // ===================== 扫描日志队列配置 =====================
    
    @Bean
    public Queue scanLogQueue() {
        return QueueBuilder.durable(SCAN_LOG_QUEUE)
                .withArgument("x-dead-letter-exchange", SCAN_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SCAN_DLX_ROUTING_KEY)
                .build();
    }
    
    @Bean
    public DirectExchange scanLogExchange() {
        return new DirectExchange(SCAN_LOG_EXCHANGE);
    }
    
    @Bean
    public Binding scanLogBinding() {
        return BindingBuilder.bind(scanLogQueue())
                .to(scanLogExchange())
                .with(SCAN_LOG_ROUTING_KEY);
    }
    
    // ===================== 死信队列配置 =====================
    
    @Bean
    public Queue scanDlxQueue() {
        return QueueBuilder.durable(SCAN_DLX_QUEUE).build();
    }
    
    @Bean
    public DirectExchange scanDlxExchange() {
        return new DirectExchange(SCAN_DLX_EXCHANGE);
    }
    
    @Bean
    public Binding scanDlxBinding() {
        return BindingBuilder.bind(scanDlxQueue())
                .to(scanDlxExchange())
                .with(SCAN_DLX_ROUTING_KEY);
    }
} 