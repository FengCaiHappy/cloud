# cloud
euureka-server:注册发现中心
eureka-client1、2:服务提供者(分布式业务)
eureka-consumer:消费者，主要是通过feign调用eureka_client1、2
cloud-gateway:网关，路由转发
cloud-config:配置中心，调用github
eureka-client1使用了配置中心
