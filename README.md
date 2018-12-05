# gradle-plugins
1. Java版本
2. 属性文件必须在META-INF/gradle-plugins
3. 属性文件名即为引入插件的id
4. 配置插件属性时使用的插件名为project.getTasks().create()方法指定的插件名(实际插件名)
5. 如果配置实际插件名冲突可能报错
6. 只能这样引入插件: apply plugin: 'help'
