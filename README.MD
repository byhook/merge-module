逛掘金很久了，之前注册过邮箱账号，但是现在每次登录，都要求重置密码，改了也没用，很绝望，重新注册了一个账号。
今天第一次在掘金上分享，废话不多说，直接开始正题。

- [概述]()
- [合并的坑]()
- [思考与扩展]()
- [混淆配置]()
- [小结]()

### 概述

简单介绍一下项目情况，笔者做这个项目快两年了，之所以有这篇文章，源于项目的需求，因为项目除了公司内部使用，`还需要抽取sdk给第三方合作公司使用，并且不同的合作方可能会对sdk作改动，A公司可能不要录屏功能，B公司可能只要视频播放功能,不要视频发布`，如何在不侵入我们主版本业务的情况下解决这个问题呢？

聪明的你肯定想到了，切分支呗！

![](https://user-gold-cdn.xitu.io/2018/6/20/1641d1a105b6c9c9?w=1043&h=349&f=png&s=9993)

这种方式只能解决一时之需，但是后续的主版本迭代，差异会越来越大，主版本同步到SDK的效率越来越低。
所以一旦是你采用了此种方案，我个人建议你做好跑路的准备！

所以去年底的时候，将组件化落地到了项目中，将各个模块独立，按照第三方的需要，实现灵活的搭配，这样一来，可以解耦我们的各个模块，便于维护，也可以适应第三方的定制需求，但是今天笔者讨论的并非组件化相关的内容，与该主题相关的内容很多，笔者今天想讨论的是组件化之后踩到的一些坑，，可能这些坑你永远也不会碰到，但是既然来了，看完又何妨呢？

目前项目架构如图所示：

![](https://user-gold-cdn.xitu.io/2018/6/20/1641d16854168988?w=710&h=674&f=png&s=26198)

组件化已基本完成，这才迈开了第一步，如何实现差异化呢？

>1.分别打包

    将各个独立的组件分别打包成对应的aar，提供给第三方，但是又涉及到一个问题，那就是混淆的问题，如果直接分别提供原始的aar包，那么源代码几乎等于完全暴露，如果分别混淆，又会存在一个问题，公共组件中常用的工具类被混淆，上层的短视频这些组件就会找不到对应的类。
> 2.合并打包

    这种方案具备良好的可行性，因为最终合并的文件只有一个，便于混淆，遗憾的是Android官方并没有提供这种合并的操作，但是发现github上有作者开源了一个合并脚本[fat-aar.gradle](https://github.com/adwiv/android-fat-aar)，这个脚本的作用实际就是合并我们的多个组件为一个aar

### 合并的坑

下面笔者将用一个示例工程来演示合并的一些相关问题。
[合并组件工程示例](https://github.com/byhook/merge-module)

用一张简单的图来描述其中的依赖关系
![](https://user-gold-cdn.xitu.io/2018/6/20/1641cd48f520d9de?w=447&h=399&f=png&s=6627)

最上层的是我们要生成的最终的merge.aar
他会合并直播间liveroom模块，合并video视频模块，而对应的模块也会依赖下层的组件，如何依赖合并呢？

```
apply from: "../fat-aar.gradle"
```

```
embedded project(':common')
embedded project(':upload')
embedded project(':download')
embedded project(':video')
embedded project(':liveroom')
```

>注意: 需要合并的组件，只需要在最上层的组件中使用`embedded`关键字标记即可，并且下层所依赖的所有组件，都需要标记一次

接下来直接使用命令打包合并
```
cd merge
gradle clean asR
```
合并完成之后你以为就结束了吗？你太年轻了！！！
当你给别人使用的时候，马上就会发现第一个坑：
![](https://user-gold-cdn.xitu.io/2018/6/20/1641cb50b294ade6?w=1053&h=495&f=png&s=93451)

出现这个错误的原因，经过笔者肉眼的分析(各种google，各种stackoverflow)，发现是由gradle 的插件版本引起的

笔者工作的环境
```
系统: ubuntu 16.04
gradle插件版本是2.3.3
gradle的版本是3.5
```
降级到gradle插件版本
```
classpath 'com.android.tools.build:gradle:2.2.3'
```
此时编译直接报错：

![](https://user-gold-cdn.xitu.io/2018/6/20/1641cdf779b50010?w=1102&h=380&f=png&s=29311)

笔者用了一个比较笨的方法：
强行指定fat-aar.gradle脚本中的版本

![](https://user-gold-cdn.xitu.io/2018/6/20/1641ce11eed36582?w=595&h=177&f=png&s=17535)

终于合并完成！！！
但是不明白这两者合并出来的aar包差异在哪里，所以我将两个插件版本分别合并的aar包截图观察了一下

`gradle2.3.3版本`合并的aar包
![](https://user-gold-cdn.xitu.io/2018/6/20/1641ccf9dc81d873?w=627&h=389&f=png&s=67879)
`gradle2.2.3版本`合并的aar包
![](https://user-gold-cdn.xitu.io/2018/6/20/1641ccfb78bb354f?w=627&h=389&f=png&s=69681)

可以看到后者打包出来的aar文件，在libs目录中有一个jar包，这个jar包里存放的就是相关的R文件

所以解决上述问题的方案：
>1.降级gradle插件版本到2.2.3版本，并修改对应脚本里的版本号

>2.使用gradle插件版本2.2.3以上，但是需要手动修改fat-aar.gradle插件的内容，使之合并相关的R文件的jar包，这个问题大家也可以思考一下？

>要知道，gradle的远程依赖功能实在是太方便了，我们可以很轻易的指定相关的依赖包，但是由于aar文件的特殊性，我们在组件中包含的一下远程依赖并不会被实际的合并到aar中去，例如你远程依赖了okhttp或者glide等相关的库，合并aar之后，就会出现如下的错误：

![](https://user-gold-cdn.xitu.io/2018/6/20/1641cf0d7434d3a9?w=1104&h=497&f=png&s=76637)

如何解决这个问题呢？聪明的你一定想到，`maven`

我们完全可以把这些依赖合并发布到maven中去，于是笔者尝试着搭建了[nexus私服](https://www.sonatype.com/download-oss-sonatype)，具体的搭建不是本文讨论的重点。

幸运的是，fat-aar的作者给我们提供了相关的publish.gradle的脚本，真的不得不说，想什么来什么啊，既然有了现成的轮子，我们就直接跑呗！

在最上层的`merge`模块中添加依赖
```
apply from: '../publish.gradle'
```
并添加如下配置
```
android {

    ...

    libraryVariants.all { variant ->
        variant.outputs.each { output ->
            def outputFile = output.outputFile
            if (outputFile != null && outputFile.name.endsWith('.aar')) {
                def fileName = getArtifactFileName()
                output.outputFile = new File(outputFile.parent, fileName)
            }
        }
    }

}

def getArtifactFileName() {
    return "${POM_ARTIFACT_ID}-${VERSION_NAME}.aar"
}
```

接下来配置自己的nexus私服即可
```
maven {
     //替换自己搭建的私服
     url "http://127.0.0.1:8081/nexus/content/repositories/releases"
 }
```

通刚才的合并打包方式，最后发布到自己的nexus私服上
![](https://user-gold-cdn.xitu.io/2018/6/20/1641cf7fa54a42da?w=563&h=597&f=png&s=44646)

你以为这样就结束了吗？并没有！！！

实际操作过程中，笔者发现，我们本地实际是有依赖`本地第三方的aar包`的，换句话说，并非所有的库都是远程依赖，你会发现，原来脚本居然会将本地依赖的aar文件，也合并到pom.xml文件中，继而发布到nexus私服上去了，这个时候给别人远程依赖，就会一直找不到相关的本地库

![](https://user-gold-cdn.xitu.io/2018/6/20/1641d010b3505e39?w=460&h=365&f=png&s=41851)

`如何解决呢？`

看来偷懒是不行的了，还是得改脚本，经过笔者的观察，发现在生成的pom.xml中可以过滤掉
```
pom.withXml {
                def dependenciesNode = asNode().appendNode('dependencies')

                depList.values().each {
                    ResolvedDependency dep ->
                        def hasGroup = dep.moduleGroup != null
                        def hasName = (dep.moduleName != null && !"unspecified".equals(dep.moduleName) && !"".equals(dep.moduleVersion))
                        def hasVersion = (dep.moduleVersion != null && !"".equals(dep.moduleVersion) && !"unspecified".equals(dep.moduleVersion))

                        if (hasGroup && hasName && hasVersion) {
                            def dependencyNode = dependenciesNode.appendNode('dependency')
                            dependencyNode.appendNode('groupId', dep.moduleGroup)
                            dependencyNode.appendNode('artifactId', dep.moduleName)
                            dependencyNode.appendNode('version', dep.moduleVersion)
                        }
                }
            }
```
即把版本号为空的过滤掉即可。

### 思考与扩展
经过一番折腾，好歹也是合并出来我们想要的东西，但是笔者刚刚也说到了，公司主项目除了自己使用，还是组合成sdk给第三方使用，第三方可能会改下首页的布局，颜色，等等。如何在不侵入主业务的情况下，作变更呢？其实很简单，借鉴Android中多渠道包的生成，同名的资源放在不同的目录
遗憾的是原生的脚本并不支持这种姿势，我在最上层的`merge`模块中使用同名的资源试图覆盖下层的资源，达到替换的目的，并未得逞！！！
没办法，还是得改脚本，改动的思想实际就是在脚本合并的过程中，优先记录最上层的资源名称，当合并下层模块的资源文件时，直接跳过即可，改过的脚本在文章的末尾。

### 混淆配置
关于混淆的配置，只需要在最上层的merge模块中配置即可

### 注意事项
>1.尽量不要使用原本的脚本文件，因为原作者已经几年未更新过，文章末尾有笔者的改动过的脚本文件

>2.各个组件的清单会合并，不需要在最上层的组件中统一注册

>3.本地依赖的jar包不用担心，因为脚本会合并到最终aar库的lib目录下

>4.本地依赖的aar包，要记得随着远程依赖，给第三方一起依赖，即第三方除了依赖我们的远程依赖，还需要本地依赖我们所使用的aar文件，这也算是一个缺陷吧

>5.第三方依赖的插件版本最好跟我们合并使用的gradle版本一致

### 小结


到目前为止，合并多组件的几个坑基本已经走了一遍了，其实在去年底，笔者在公司的直播项目中已经将组件化落地了，而后在实现多组件的道路上也踩了不少坑，本来这篇文章并没有打算发布出来，因为并不是所有人都会碰到这类需求，但是前段时间有个朋友公司问了我相关的问题，看他踩坑了很久，所以还是觉得发布出来，至少对于看到的人而言，以后碰到类似问题，可以少走些弯路，提高效率。

`纸上得来终觉浅，绝知此事要躬行！`


[示例项目](https://github.com/byhook/merge-module)

[脚本地址](https://github.com/byhook/merge-gradle)
