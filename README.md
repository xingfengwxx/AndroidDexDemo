# 1.配置dex打包任务
```Groovy
//删除plugin.jar包任务
task clearJar(type: Delete) {
    delete('libs/plugin.jar')
}
//打包任务
task makeJar(type: org.gradle.api.tasks.bundling.Jar) {
//指定生成的jar名称
    baseName 'plugin'
//从哪里打包class文件  app/build/intermediates/javac/debug/classes/com/anlytics/plug
    from('build/intermediates/javac/debug/classes/com/anlytics/plug')
//打包到jar后的目录结构
    into('com/anlytics/plug')
//去掉不需要打包的目录和文件
    exclude('text/', 'R.class', 'BuildConfig.class', 'MainActivity.class', 'demo/')
//去掉R$开头的文件
    exclude { it.name.startsWith('R$') }
    
    makeJar.dependsOn(clearJar, build)
}
```


# 2.打开Terminal输入命令：gradle makeJar
输出路径：build/libs/plugin.jar

# 3.使用ProGuard工具进行混淆
混淆配置文件：app/proguard-rules.pro
输出混淆后的jar包：plugin_guard.jar

# 4.将jar包复制到D:\android-sdk\build-tools\19.1.0目录下
cmd进入该目录执行命令

```Shell
dx --dex --output=plugin_dex_guard.jar plugin_guard.jar
```

# 5.plugin_dex_guard.jar就是经过混淆的dex包了
