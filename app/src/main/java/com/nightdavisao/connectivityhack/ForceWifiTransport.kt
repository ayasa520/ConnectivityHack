package com.nightdavisao.connectivityhack

import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class ForceWifiTransport : IXposedHookZygoteInit {

    companion object {
        private const val NC_CLASS = "android.net.NetworkCapabilities"
        private const val CM_CLASS = "android.net.ConnectivityManager"
        private const val NI_CLASS = "android.net.NetworkInfo"
        private const val WM_CLASS = "android.net.wifi.WifiManager"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        try {
            // Hook NetworkCapabilities.hasTransport()
            hookNetworkCapabilities()
            
            // Hook ConnectivityManager methods
            hookConnectivityManager()
            
            // Hook NetworkInfo methods
            hookNetworkInfo()
            
            // Hook WifiManager methods
            hookWifiManager()
            
            XposedBridge.log("ForceWifiTransport: all hooks installed successfully")
        } catch (t: Throwable) {
            XposedBridge.log("ForceWifiTransport: failed to install hooks: $t")
        }
    }
    
    private fun hookNetworkCapabilities() {
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

            XposedBridge.log("ForceWifiTransport: NetworkCapabilities hook installed. TRANSPORT_WIFI=$transportWifi")
        } catch (t: Throwable) {
            XposedBridge.log("ForceWifiTransport: failed to hook NetworkCapabilities: $t")
        }
    }
    
    private fun hookConnectivityManager() {
        try {
            // Hook getActiveNetworkInfo() - deprecated but still used
            XposedHelpers.findAndHookMethod(
                CM_CLASS,
                null,
                "getActiveNetworkInfo",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val networkInfo = param.result
                        if (networkInfo != null) {
                            XposedBridge.log("ForceWifiTransport: hooking getActiveNetworkInfo()")
                            // Force network type to WIFI
                            XposedHelpers.callMethod(networkInfo, "setType", 1) // TYPE_WIFI = 1
                        }
                    }
                }
            )
            
            // Hook getNetworkInfo(Network)
            XposedHelpers.findAndHookMethod(
                CM_CLASS,
                null,
                "getNetworkInfo",
                XposedHelpers.findClass("android.net.Network", null),
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val networkInfo = param.result
                        if (networkInfo != null) {
                            XposedBridge.log("ForceWifiTransport: hooking getNetworkInfo(Network)")
                            XposedHelpers.callMethod(networkInfo, "setType", 1) // TYPE_WIFI = 1
                        }
                    }
                }
            )
            
            XposedBridge.log("ForceWifiTransport: ConnectivityManager hooks installed")
        } catch (t: Throwable) {
            XposedBridge.log("ForceWifiTransport: failed to hook ConnectivityManager: $t")
        }
    }
    
    private fun hookNetworkInfo() {
        try {
            // Hook NetworkInfo.getType()
            XposedHelpers.findAndHookMethod(
                NI_CLASS,
                null,
                "getType",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("ForceWifiTransport: returning TYPE_WIFI for NetworkInfo.getType()")
                        param.result = 1 // TYPE_WIFI = 1
                    }
                }
            )
            
            // Hook NetworkInfo.getTypeName()
            XposedHelpers.findAndHookMethod(
                NI_CLASS,
                null,
                "getTypeName",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("ForceWifiTransport: returning 'WIFI' for NetworkInfo.getTypeName()")
                        param.result = "WIFI"
                    }
                }
            )
            
            // Hook NetworkInfo.isConnected()
            XposedHelpers.findAndHookMethod(
                NI_CLASS,
                null,
                "isConnected",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("ForceWifiTransport: returning true for NetworkInfo.isConnected()")
                        param.result = true
                    }
                }
            )
            
            XposedBridge.log("ForceWifiTransport: NetworkInfo hooks installed")
        } catch (t: Throwable) {
            XposedBridge.log("ForceWifiTransport: failed to hook NetworkInfo: $t")
        }
    }
    
    private fun hookWifiManager() {
        try {
            // Hook WifiManager.isWifiEnabled()
            XposedHelpers.findAndHookMethod(
                WM_CLASS,
                null,
                "isWifiEnabled",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("ForceWifiTransport: returning true for WifiManager.isWifiEnabled()")
                        param.result = true
                    }
                }
            )
            
            XposedBridge.log("ForceWifiTransport: WifiManager hooks installed")
        } catch (t: Throwable) {
            XposedBridge.log("ForceWifiTransport: failed to hook WifiManager: $t")
        }
    }
}