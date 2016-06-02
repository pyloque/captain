Captain --  基于Redis的极简服务发现系统
-------------
Captain通过牺牲一点点高可用性以达到简单高效。在大多数中小型公司，并没有成千上万台的机器设备，机器宕机可能性很小，N个9的高可用性还没有显得太重要。但是开源市场上只提供了zk/etcd/consul这类强一致性服务发现框架，它们都过于复杂，至少一般人很难理解系统内部到底是怎么回事。
[English](README.md)

架构
-------------
<img src="screenshot/flow.png" width="600" title="Captain Architecture" />
<img src="screenshot/arch.png" width="600" title="Captain Architecture" />

1. Captain系统是无状态的，客户端可以访问多个server
2. Captain客户端可以同时是服务生产者和消费者
3. 如果所有的Captain server还有redis都宕机了，Captain客户端依然会将服务发现信息保留在内存中
4. 如果只有一个Captain server宕机了，Captain客户端可以从其它Captain server获取服务信息
5. 如果只有redis master宕机了，可以在管理后台动态切换到redis slave
6. 仔细监控redis和captain server的状态，即时恢复，高可用性依然可以得到保证

内部结构
------------
1. 每个服务列表保存在redis的sortedset中，key就是host:port服务地址，score是${now+ttl}
2. 有一个专门负责清理过期服务的线程，它定时扫描所有的服务列表
3. 为了快速检测服务列表的变化，在redis中纪录了一个全局的服务版本号，还为每个服务列表纪录了一个子版本号
4. 客户端会周期性检测全局版本号，如果全局版本号编号，再检测依赖服务的子版本号，然后加载版本号变更了的服务列表
5. Less Keys, Large Value. 尽管captain提供了keyvalue的api，KeyValue也不可以被随便滥用。Captain缺少处理太多key的能力。请尽量只将keyvalue用于全局配置，将多个配置项聚合到一个kv中
6. 客户端和服务器之间只使用http api进行通讯

服务发现 API
------------------------
1. keep service /api/service/keep?name=sample&host=localhost&port=6000&ttl=30 GET
2. cancel service /api/service/cancel?name=sample&host=localhost&port=6000 GET
4. get service version /api/service/version?name=sample1&name=sample2 GET
4. get service list /api/service/set?name=sample GET

KV API
------------------------
1. set /api/kv/set?key=sample&value={"a": "m", "b": "n", "c": {"a": "m", "b": "n"}} POST
2. get /api/kv/get?key=sample GET
3. mget /api/kv/mget?key=sample1&key=sample2 GET
4. get kv version /api/kv/version?key=sample1&key=sample2 GET

全局 API
------------------------
1. get global service & kv version in single api /api/version


安装 Captain Server
---------------------
```bash
install redis
install java8
install maven

git clone github.com/pyloque/captain.git
cd captain
mvn package
java -jar target/captain.jar
java -jar target/captain.jar ${configfile}  # custom config file

open web ui
http://localhost:6789/ui/
```

配置
--------------------
Default Config File is ${user.home}/.captain/captain.ini
```ini
[server]
host = 0.0.0.0
port = 6789
thread = 24 # sparkjava threadpool size

[redis]
host = localhost
port = 6379
db = 0

[watch]
interval = 1000 # service expiring check interval, default 1000ms. server will run in readonly mode if interval=0.

```

只读模式
------------------------
1. 不启动服务过期检测线程，服务列表永远不会主动过期
2. 服务保持和取消API也不会提供

动态切换Redis
-----------------------
如果redis master遇到偶然宕机，你应该使用管理后台快速切换到redis slave
切换后，server会立即进入紧急模式，服务过期检测被停止，保持服务列表继续存活
当所有的server都切换成功后，再使用管理后台点击"start watcher"，退出紧急模式，进入正常状态

Web界面
------------------------
<img src="screenshot/all_services.png" width="600" title="All Services" />
<img src="screenshot/service_list.png" width="600" title="Service List" />
<img src="screenshot/config.png" width="600" title="Configuration"/ >
<img src="screenshot/kv.png" width="600" title="Kv Edit"/ >

客户端SDK
------------------------
1. Python Client https://github.com/pyloque/pycaptain
2. Java Client https://github.com/pyloque/captain-java
3. Golang Client https://github.com/pyloque/gocaptain
4. Agent Server https://github.com/pyloque/captain-agent
5. PHP Client https://github.com/pyloque/phpcaptain

饮用
-------------------------
1. 棒棒的SparkJava https://github.com/perwendel/spark/
2. 简单直接的ini4j http://ini4j.sourceforge.net/

注意
-------------------------
Captain还在持续的开发中，稳定性虽尚不明确。不过captain的设计如果简单清晰，如果你不想使用带bug的captain，你可以自己造一个

<img src="screenshot/weixin.png" width="320" title="QR"/ >
