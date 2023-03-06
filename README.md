# Eurika - A lightweight RPC framework for Spring ecosystem 

[中文简介 Chinese Introduction](./README-cn.md)

Eurika is a concise, lightweight, and well-structured RPC (Remote Procedure Call) framework designed to work with
the Spring ecosystem. The framework aims to simplify the mutual invocations within distributed systems by providing 
an easy-to-use interface that subtly hides the complexity of cross-network communications among services.


## Features
- Simple annotation-based API that creates minimum intrusions into your code
- Support for synchronous and asynchronous invocation
- Built on top of Spring, providing seamless integration with existing projects
- Supports multiple transmission, including HTTP and customized RPC
- Provides load balancing and fault tolerance out of the box, including message resending on failure
- Supports service registration and discovery using Zookeeper

