# TrySophixApp
An android app demo for trying aliyun sophix hotfix sdk.

[帮助文档](https://help.aliyun.com/product/51340.html)

## 注册阿里云开发者
[阿里云](https://www.aliyun.com/)

## 进入控制台
[控制台](https://home.console.aliyun.com)

## 进入移动热修复控制台
[移动热修复控制台](https://hotfix.console.aliyun.com)

## 创建产品

## 创建 APP
获取如下信息
1 - AppId
2 - AppSecret
3 - RSA密钥

## 打开 Android Studio 创建新项目

## 项目级 build.gradle
```script
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    ext.kotlin_version = '1.1.51'
    repositories {
        google()
        jcenter()

        maven {
            url "http://maven.aliyun.com/nexus/content/repositories/releases"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.0'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()

        maven {
            url "http://maven.aliyun.com/nexus/content/repositories/releases"
        }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

## 模块级 build.gradle
```script
apply plugin: 'com.android.application'

apply plugin: 'kotlin-android'

apply plugin: 'kotlin-android-extensions'

android {
    compileSdkVersion 26
    defaultConfig {
        applicationId "com.hayukleung.tsa"
        minSdkVersion 14
        targetSdkVersion 26
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        hayukleung {
            keyAlias "${KEY_ALIAS}"
            keyPassword "${KEY_PASSWORD}"
            storeFile file('./hayukleung.jks')
            storePassword "${STORE_PASSWORD}"
        }
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.hayukleung
        }
    }
    dataBinding {
        enabled = true
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation"org.jetbrains.kotlin:kotlin-stdlib-jre7:$kotlin_version"
    implementation 'com.android.support:appcompat-v7:26.0.0-alpha1'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:0.5'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:2.2.2'

    implementation 'com.aliyun.ams:alicloud-android-hotfix:3.1.8'
}
```

## 创建 MainActivity

## 创建 App Application
```java
// App.java
package com.hayukleung.tsa;

import android.app.Application;

import com.taobao.sophix.SophixManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // queryAndLoadNewPatch不可放在attachBaseContext 中，否则无网络权限，建议放在后面任意时刻，如onCreate中
        SophixManager.getInstance().queryAndLoadNewPatch();
    }
}
```

## 创建 SophixStubApplication
```java
// SophixStubApplication.java
package com.hayukleung.tsa;

import android.content.Context;
import android.support.annotation.Keep;
import android.util.Log;

import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixApplication;
import com.taobao.sophix.SophixEntry;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;

/**
 * Sophix入口类，专门用于初始化Sophix，不应包含任何业务逻辑。
 * 此类必须继承自SophixApplication，onCreate方法不需要实现。
 * AndroidManifest中设置application为此类，而SophixEntry中设为原先Application类。
 * 注意原先Application里不需要再重复初始化Sophix，并且需要避免混淆原先Application类。
 * 如有其它自定义改造，请咨询官方后妥善处理。
 */
public class SophixStubApplication extends SophixApplication {

    private final String TAG = "SophixStubApplication";

    // 此处SophixEntry应指定真正的Application，并且保证RealApplicationStub类名不被混淆。
    @Keep
    @SophixEntry(App.class)
    static class RealApplicationStub {
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 如果需要使用MultiDex，需要在此处调用。
        // MultiDex.install(this);
        initSophix();
    }

    private void initSophix() {
        String appVersion = BuildConfig.VERSION_NAME;
        try {
            appVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        final SophixManager instance = SophixManager.getInstance();
        instance.setContext(this)
                .setAppVersion(appVersion)
                .setSecretMetaData(null, null, null)
                .setEnableDebug(true)
                .setEnableFullLog()
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onLoad(final int mode, final int code, final String info, final int handlePatchVersion) {
                        if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                            Log.i(TAG, "sophix load patch success!");
                        } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                            // 如果需要在后台重启，建议此处用SharePreference保存状态。
                            Log.i(TAG, "sophix preload patch success. restart app to make effect.");
                        }
                    }
                }).initialize();
    }
}
```

## Manifest.xml
```xml
<manifest package="com.hayukleung.tsa"
    xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <!-- 外部存储读权限，调试工具加载本地补丁需要 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>

    <application
        android:name=".SophixStubApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <!--AppId-->
        <meta-data
            android:name="com.taobao.android.hotfix.IDSECRET"
            android:value="24735460-1" />

        <!--AppSecret-->
        <meta-data
            android:name="com.taobao.android.hotfix.APPSECRET"
            android:value="68637b24b0b69ea5a74759edec18ab75" />

        <!--RSA 密钥-->
        <meta-data
            android:name="com.taobao.android.hotfix.RSASECRET"
            android:value="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCyk1A/hVAn2ipL3ptHcskyzo7OOHLYv/VNgImqaEbOQKbxP6Dv7tefVwU6TLbSap69BIUqqDGVC/0Q9t/W2SbRkVRFSlDXXXQhqyWh7+PHjCwHEXJ/kksManOaTNdt+P5btZu+8rbYs/XQjfXXLDjeZTNsBtVGItFLuPuep8kk5g9FY6tBhDetNo+XpEk1N/p1esDR4bYF+DvBBqlgLJv2TAX9qyyPHE9VeDxnkF7f9mcYip4E/wTdgtkGhcBjVe5V5w0q9sTtozJ4z72qIx+cKNYoPQIouYZemgCRAwPNcgx0iUgc0ByNJM2D6GFIzIsjtm8KNbdG9hoXHaiciOvRAgMBAAECggEBAIFXScx4VrJGxlPljDqX3Tq7eZvMLwLnJllYPF384SwKj+A04fBm4i467UJoxhJpv0fQulNHN4JpCIb9COuopQEtbggx55PV0IOCXuOHyUeDM3B7Jp9X6pu3jru35GF282ShiYkoa19SGT1RVTrggOA0FKat3FPMbCOYfEdR59KcyD2+LC+rGsOhxpGFMkRmsP3FakM89uv0JXsZs9RpFVWU3wJt0cio1v9Dl4P/M9ORjcrLSnc4VPJRduJLe/31v96X6bMSulMa+Nisp7+pgCvEwnOq2IgVW+qlwxS5bXn8/FNKjT8CZO+gNdVY+b0Vec9PKMFl349vsbpi684nsAECgYEA6/0V+93munpfzcnTXapgr416shyYR/PoDqX+jZDsKntQYyF4wpLE0ClGxvkBD0Jw/f860pePZXCg1hNxSd66MysaczM1f2WdqjmVHglYtJTRFqdcwqXakQ1vTvBE8rfT9Io0L6IJeEzUGccYz0hK2/xLzN2EHHpRYjpVmYU6+ZECgYEAwbfidbHMKJ6yuB8qv7LUwtixQNE/tfHnWnn58PoW8zlkEKrmQx7SaxaQTDWNAPKCUX8rvYVz3yLPzEPFBVziU+txMo4XRfl2yagQGh1X2QqNUm5REiDsAnRir8JfAbpf3bYiGHMZHQC2qsbOJsahyjkfTV7QXJ7e6hJiraR/rkECgYBb6kmDvlw9yahDCRcwZkoeVmaxtP2DGPcLrY/GJ+o0aa9XzROyBWRvB4SvvxCwltpXs+9/UtThV8rtVslLUlsHzwQQVqPBqRyNzrvL5SwNueDuh+VTUzIcSTVPc7oHJ56AosvMuboihxodqDR7l9bAdDY55xWr2eCU98+bag8cYQKBgDvTnozarEKfQHK8rWcmw66pXZS3CwYV/21h3l8d3ZigUNcU8KgqDLIl9cqTf0ibnMOuksOlQ3PWPHbJNz/Av+VLAVqrp2Rk9tqwnMhz532QVLTwfPksUbcwWooNbWoLNTAz37PNWCQs5D05yNEQ50ac/Z3XemIgbpe7td4Kv6QBAoGAS0RJ3OJ+mu/2ZUOCISbZyhfURLbpCcv7xJYGFcm/4oIAsemzI8Srz1Ft1bEzL7bHW1gfSy279s57dkvGEI/quMqwAK7tzJZcxwRSbXadmrZWzdRLmKJtHb+wJG2alI1llG8AN5OXOOMILZBu6YbOA2YDI97djTM8t0PxyIJzwGQ=" />

        <activity android:name=".activity.MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## 下载补丁生成工具
[下载地址](https://help.aliyun.com/document_detail/53247.html)

## Demo 示例地址
[Demo](https://github.com/hayukleung/TrySophixApp)
