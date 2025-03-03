package com.yc.networklib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.yc.appcontextlib.AppToolUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


/**
 * <pre>
 *     @author yangchong
 *     GitHub : https://github.com/yangchong211/YCAppTool
 *     email  : yangchong211@163.com
 *     time  : 2016/09/23
 *     desc  : 网络相关工具类
 *     revise:
 * </pre>
 */
public final class AppNetworkUtils {

    private AppNetworkUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 打开网络设置界面
     */
    public static void openWirelessSettings() {
        AppToolUtils.getApp().startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 获取活动网络信息
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return NetworkInfo
     */
    @SuppressLint("MissingPermission")
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager manager =
                (ConnectivityManager) AppToolUtils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return null;
        }
        return manager.getActiveNetworkInfo();
    }

    /**
     * 判断网络是否连接
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isConnected() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }


    /**
     * 判断移动数据是否打开
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean getMobileDataEnabled() {
        try {
            TelephonyManager tm =
                    (TelephonyManager) AppToolUtils.getApp().getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) {
                return false;
            }
            @SuppressLint("PrivateApi")
            Method getMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("getDataEnabled");
            if (null != getMobileDataEnabledMethod) {
                return (boolean) getMobileDataEnabledMethod.invoke(tm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 打开或关闭移动数据
     * <p>需系统应用 需添加权限
     * {@code <uses-permission android:name="android.permission.MODIFY_PHONE_STATE" />}</p>
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    public static void setMobileDataEnabled(final boolean enabled) {
        try {
            TelephonyManager tm =
                    (TelephonyManager) AppToolUtils.getApp().getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) {
                return;
            }
            Method setMobileDataEnabledMethod =
                    tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(tm, enabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断网络是否是移动数据
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    @SuppressLint("MissingPermission")
    public static boolean isMobileData() {
        NetworkInfo info = getActiveNetworkInfo();
        return null != info
                && info.isAvailable()
                && info.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * 判断网络是否是 4G
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean is4G() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null
                && info.isAvailable()
                && info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE;
    }

    /**
     * 判断 wifi 是否打开
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean getWifiEnabled() {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppToolUtils.getApp().getSystemService(Context.WIFI_SERVICE);
        return manager != null && manager.isWifiEnabled();
    }

    /**
     * 打开或关闭 wifi
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />}</p>
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    @SuppressLint("MissingPermission")
    public static void setWifiEnabled(final boolean enabled) {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppToolUtils.getApp().getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            return;
        }
        if (enabled) {
            if (!manager.isWifiEnabled()) {
                manager.setWifiEnabled(true);
            }
        } else {
            if (manager.isWifiEnabled()) {
                manager.setWifiEnabled(false);
            }
        }
    }

    /**
     * 判断 wifi 是否连接状态
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: 连接<br>{@code false}: 未连接
     */
    @SuppressLint("MissingPermission")
    public static boolean isWifiConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) AppToolUtils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null
                && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 获取网络运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return 运营商名称
     */
    public static String getNetworkOperatorName() {
        TelephonyManager tm =
                (TelephonyManager) AppToolUtils.getApp().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : null;
    }

    private static final int NETWORK_TYPE_GSM      = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN    = 18;

    /**
     * 获取当前网络类型
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return 网络类型
     * <ul>
     * <li>{@link NetworkType#NETWORK_WIFI   } </li>
     * <li>{@link NetworkType#NETWORK_4G     } </li>
     * <li>{@link NetworkType#NETWORK_3G     } </li>
     * <li>{@link NetworkType#NETWORK_2G     } </li>
     * <li>{@link NetworkType#NETWORK_UNKNOWN} </li>
     * <li>{@link NetworkType#NETWORK_NO     } </li>
     * </ul>
     */
    public static NetworkType getNetworkType() {
        NetworkType netType = NetworkType.NETWORK_NO;
        NetworkInfo info = getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {

            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {

                    case NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = NetworkType.NETWORK_2G;
                        break;

                    case NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = NetworkType.NETWORK_3G;
                        break;

                    case NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NetworkType.NETWORK_4G;
                        break;
                    default:

                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NetworkType.NETWORK_3G;
                        } else {
                            netType = NetworkType.NETWORK_UNKNOWN;
                        }
                        break;
                }
            } else {
                netType = NetworkType.NETWORK_UNKNOWN;
            }
        }
        return netType;
    }

    /**
     * 获取 IP 地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @param useIPv4 是否用 IPv4
     * @return IP 地址
     */
    public static String getIPAddress(final boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> nis =
                 NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
                NetworkInterface ni = nis.nextElement();
                // 防止小米手机返回 10.0.2.15
                if (!ni.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) {
                                return hostAddress;
                            }
                        } else {
                            if (!isIPv4) {
                                int index = hostAddress.indexOf('%');
                                return index < 0
                                        ? hostAddress.toUpperCase()
                                        : hostAddress.substring(0, index).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取域名 ip 地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @param domain 域名
     * @return ip 地址
     */
    public static String getDomainAddress(final String domain) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(domain);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 判断设备 是否使用代理上网
     * @param context                               上下文
     * @return                                      设备是否链接代理
     */
    public static boolean isWifiProxy(Context context) {
        // 是否大于等于4.0
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    /**
     * 获取wifi的强弱
     * @return
     */
    public static String getWifiState(){
        if (isWifiConnected()) {
            WifiManager mWifiManager = (WifiManager) AppToolUtils.getApp().getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = null;
            if (mWifiManager != null) {
                mWifiInfo = mWifiManager.getConnectionInfo();
                int wifi = mWifiInfo.getRssi();
                //获取wifi信号强度
                if (wifi > -50 && wifi < 0) {
                    //最强
                    return "最强";
                } else if (wifi > -70 && wifi < -50) {
                    //较强
                    return "较强";
                } else if (wifi > -80 && wifi < -70) {
                    //较弱
                    return "较弱";
                } else if (wifi > -100 && wifi < -80) {
                    //微弱
                    return "微弱";
                } else {
                    return "微弱";
                }
            }
        }
        return "无wifi连接";
    }


    /**
     * 通过域名获取真实的ip地址 (此方法需要在线程中调用)
     * @param domain                                host
     * @return
     */
    public static String getHostIP(String domain) {
        String ipAddress = "";
        InetAddress iAddress = null;
        try {
            iAddress = InetAddress.getByName(domain);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (iAddress == null) {
            ipAddress = "";
        } else {
            ipAddress = iAddress.getHostAddress();
        }
        return ipAddress;
    }



    /**
     * 通过域名获取真实的ip地址 (此方法需要在线程中调用)
     * @param domain                                host
     * @return
     */
    public static String getHostName(String domain) {
        String ipAddress = "";
        InetAddress iAddress = null;
        try {
            iAddress = InetAddress.getByName(domain);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (iAddress == null) {
            ipAddress = "";
        } else {
            ipAddress = iAddress.getHostName();
        }
        return ipAddress;
    }


    /**
     * 获取wifi的名称
     * @param context               上下文
     * @return
     */
    public static String getWifiName(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            return ssid;
        }
        return "无网络";
    }


    /**
     * 获取wifi的ip
     * @param context               上下文
     * @return
     */
    public static int getWifiIp(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return ipAddress;
        }
        return -1;
    }


    /**
     * 获取wifi的信息
     * @param context                   上下文
     * @return
     */
    public static WifiInfo getWifiInfo(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo;
        }
        return null;
    }

}
