# SpringBoot-AI-SQL-Search

基于自然语言查询的数据库智能搜索系统，接入阿里千问大模型将中文查询自动转为 SQL 语句查询

功能：
AI 自然语言搜索 — 输入"年龄大于25岁的用户"自动生成 WHERE age > 25
多表支持 — 通过 tableName 参数指定查询任意表
自动获取表结构 — 运行时动态读取表字段和类型
性能监控 — AOP 切面记录所有接口耗时

访问localhost:8088/ai-search.html,可以使用自然语言查询功能。
访问localhost:8088/test.html，可以通过浏览器对数据库进行操作。

注意：
##数据库
需要在application.properties文件中配置你的数据库名、mysql用户名、mysql登录密码
##ai
方法一：在环境变量中配置好阿里云api-key
方法二：直接修改application.properties文件中的alibaba.dashscope.api-key
