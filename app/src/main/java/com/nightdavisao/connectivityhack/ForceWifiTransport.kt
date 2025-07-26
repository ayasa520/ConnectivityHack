package com.nightdavisao.connectivityhack

import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class ForceWifiTransport : IXposedHookZygoteInit {

    companion object {
        private const val NC_CLASS = "android.net.NetworkCapabilities"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        try {
            val ncClass = XposedHelpers.findClass(NC_CLASS, null)
            val transportWifi = XposedHelpers.getStaticIntField(ncClass, "TRANSPORT_WIFI") as Int

            XposedHelpers.findAndHookMethod(
                NC_CLASS,
                null,
                "hasTransport",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val transport = param.args[0] as Int
                        if (transport == transportWifi) {
                            param.result = true
                            XposedBridge.log("ForceWifiTransport: returning true for hasTransport(WIFI)")
                        }
                    }
                }
            )

            XposedBridge.log("ForceWifiTransport: hook installed. TRANSPORT_WIFI=$transportWifi")
        } catch (t: Throwable) {
            XposedBridge.log("ForceWifiTransport: failed to hook hasTransport: $t")
        }
    }
}