package com.zyh.pddassistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PddService extends AccessibilityService {
    private static final String TAG = "Service";
    private static final String PDD_Package_Name = "com.xunmeng.pinduoduo";
    private int waitTime = 3000;
    private boolean onSlide = false;
    private long offsetTime;
    private double menuTime = 0;
    private double slideTime = 0;
    private double realTime = 0;
    private boolean inMenu = false;
    private boolean inBrowse = false;
    private double browseTime = 0;
    private SimpleDateFormat sdf;
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        Toast.makeText(getApplicationContext(),"开启服务", Toast.LENGTH_SHORT).show();
        sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        offsetTime = calendar.getTime().getTime();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        Toast.makeText(getApplicationContext(),"关闭服务", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: ");
    }

    private void drawPath(Path path){
        //设置Paint
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
        Canvas canvas = new Canvas();
        canvas.drawPath(path, paint);
    }

    private List<String> getAllTexts(AccessibilityNodeInfo root){
        List<String> texts = new ArrayList<>();
        if(root==null) return texts;
        if(root.getText()!=null && !root.getText().toString().isEmpty()) texts.add(root.getText().toString());
        int count = root.getChildCount();
        for(int i=0;i<count;i++){
            texts.addAll(getAllTexts(root.getChild(i)));
        }
        return texts;
    }
    private List<AccessibilityNodeInfo> getAllNodesByText(AccessibilityNodeInfo root, String regex){
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        if(root==null) return nodes;//s.matches(regex)
        if(root.getText()!=null && root.getText().toString().matches(regex)) nodes.add(root);
        int count = root.getChildCount();
        for(int i=0;i<count;i++){
            nodes.addAll(getAllNodesByText(root.getChild(i), regex));
        }
        return nodes;
    }
    /**
     * 通用组件执行方法，适用于页面只有一个，且不需要考虑与其他组件共存的情况
     * @param root
     * @param text
     */
    private boolean performOneActionByText(AccessibilityNodeInfo root, String text){
        boolean action = false;
        List<AccessibilityNodeInfo> nodes = getAllNodesByText(root,text);
        for(AccessibilityNodeInfo node : nodes){
            if(node.isEnabled()){
                action = true;
                Log.d(TAG, node.getText().toString()+"——执行");
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        }
        return action;
    }
    private boolean performAllActionByText(AccessibilityNodeInfo root, String text){

        String[] arr = text.split("&|!");
        String regex = arr[0];
        if(text.contains("!")&&text.contains("&")){
            if(getAllNodesByText(root,arr[1]).isEmpty()) return false;
            if(!getAllNodesByText(root,arr[2]).isEmpty()) return false;
        }else if(text.contains("&")){
            if(getAllNodesByText(root,arr[1]).isEmpty()) return false;
        }else if(text.contains("!")){
            if(!getAllNodesByText(root,arr[1]).isEmpty()) return false;
        }
        List<AccessibilityNodeInfo> nodes = getAllNodesByText(root,regex);
        if(nodes.isEmpty()) {
//            Log.d(TAG, "无"+text);
            return false;
        }
        boolean action = false;
        for(AccessibilityNodeInfo node : nodes){
            if(node.isEnabled()){
                action = true;
                Log.d(TAG, node.getText().toString()+"——执行");
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
        return action;
    }
    private void drink(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root==null) {
            root = getRootInActiveWindow();
        }
        if(root==null) return;
        String[] texts = new String[]{"^喝水赚钱!^全部领取", "^可打卡",".*我喝橙汁了，打个卡","收下奖励","^去升级",
                "^可升级","去看\\d{2}:\\d{2}视频领奖励",".*领取额外健康补贴","\\d{2}:\\d{2}:\\d{2}后来喝橙汁",
                ".*后喝橙汁打卡",".*后喝水打卡","明天继续喝水打卡",".*我喝水了，打个卡","收下金币","(\\d)*分钟后来喝第(\\d)*杯水"};
        for(String text : texts){
            boolean action = performAllActionByText(root,text);
            if(action && (text.equals("明天继续喝水打卡") || text.equals("(\\d)*分钟后来喝第(\\d)*杯水"))) back();
        }
    }
    private void meal(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        String[] texts = new String[]{"^可领取", "领取[早午晚]餐(额外)?补贴","^收下补贴","5点来领早餐补贴"
                ,"11点来领午餐补贴","17点来领晚餐补贴"};
        for(String text : texts){
            performAllActionByText(root,text);
        }
    }
    private void redPacket(){//可开启
        AccessibilityNodeInfo root = getRootInActiveWindow();
        String[] texts = new String[]{"^可开启", "看视频(\\d{2}:\\d{2})?领红包"};
        for(String text : texts){
            boolean action = performAllActionByText(root,text);
//            if(action && text.equals("^可开启")) waitTime = 5000;
        }
    }
    private void sleep(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        String[] texts = new String[]{"^可参与","开始通宵赚钱","再看\\d{2}:\\d{2}视频提升奖励"};
        for(String text : texts){
            performAllActionByText(root,text);
        }
    }
    private void back(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "back: ");
            Path path = new Path();
            path.moveTo(0, 2000);//设置Path的起点
            path.lineTo(500, 2000);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    private void openMenu(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "openMenu");
            Path path = new Path();
            path.moveTo(1325,590);//1325,590
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    private void browse(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        double nowTime = (new Date().getTime()-offsetTime)/60000.0;
        if(!inMenu){ //菜单未打开
            if(menuTime==0 || (nowTime- menuTime)>=0.2){
                openMenu();
                Log.d(TAG, "打开菜单,相隔时间： "+(nowTime- menuTime)+" 分钟");
                menuTime = nowTime;
                inMenu = true;
//                if(performAllActionByText(root,"^换现金")){
//                    menuTime = nowTime;
//                    inMenu = true;
//                }else{
//                    Log.d(TAG, "没找到现金");
//                }

            }
        }else{ //菜单打开了
            if(!inBrowse){ //未浏览
                if(performOneActionByText(root,"^去逛逛")){
                    inBrowse = true;
                    browseTime = nowTime;
//                }else if(performOneActionByText(root,"^去浏览")){
//                    inBrowse = true;
//                    browseTime = nowTime;
                } else {
                    back();
                    inMenu = false;
                    menuTime = nowTime;
                }
            }else{ //在浏览
                if(nowTime-browseTime>=1.2){
                    Log.d(TAG, "浏览结束,浏览时间： "+(nowTime-browseTime)+" 分钟");
                    back();
                    inBrowse = false;
                }
            }

        }

    }
    private void slide(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Date date = new Date();
            double nowTime = (date.getTime()-offsetTime)/1000.0;
            slideTime = nowTime;
            Path path = new Path();
//                path.moveTo(500, 1287);//设置Path的起点
//                path.quadTo(450, 1036, 90, 864);
            path.moveTo(750, 1500);//设置Path的起点
            path.lineTo(750,1000);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    /**
     * 各元素位置：
     * 换现金：1325,590
     * 领红包：1325,815
     * 去打卡（菜单页顶部喝水打卡）：500,1750
     * 去领取（菜单页顶部划到右边吃饭赚钱）：1200,1750
     * 去逛逛（菜单页浏览赚现金，第一个）：1225,2050
     * 去浏览（菜单页百亿补贴，第二个）：1225,2325
     * 全部领取（菜单页面好友红包）：1325,2900
     * 领红包（看视频领红包页面领取按钮）：750,2150
     * 吃饭补贴领取(吃饭补贴页面领取按钮)：750,2050
     */
    private void PDDAssistant(){
        if(onSlide) return;
        Log.d(TAG, "PDDAssistant");
        Log.d(TAG, "allTexts: "+getAllTexts(getRootInActiveWindow()));
//        if(true) return;
        new Thread(()->{
            Date firstDate = new Date();
            onSlide = true;
//            back();
//            browse();
            drink();
            Date midData1 = new Date();
            Log.d(TAG, "PDDAssistant: 中间时间1："+String.format("%.2f", (midData1.getTime()-firstDate.getTime())/1000.0)+"s");
            meal();
            Date midData2 = new Date();
            Log.d(TAG, "PDDAssistant: 中间时间2："+String.format("%.2f", (midData2.getTime()-midData1.getTime())/1000.0)+"s");
            redPacket();
            Date midData3 = new Date();
            Log.d(TAG, "PDDAssistant: 中间时间3："+String.format("%.2f", (midData3.getTime()-midData2.getTime())/1000.0)+"s");
            sleep();
            Date midData4 = new Date();
            Log.d(TAG, "PDDAssistant: 中间时间4："+String.format("%.2f", (midData4.getTime()-midData3.getTime())/1000.0)+"s");
            slide();
            Date midData5 = new Date();
            Log.d(TAG, "PDDAssistant: 中间时间5："+String.format("%.2f", (midData5.getTime()-midData4.getTime())/1000.0)+"s");
            Date secondDate = new Date();
            double midTime = (secondDate.getTime()-firstDate.getTime())/1000.0;
            if(midTime>5.0){
                File file = new File(new ContextWrapper(getApplicationContext()).getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PddService.txt");
                @SuppressLint("DefaultLocale")
                String content = "\n"+sdf.format(new Date())+"  pdd  "+String.format("%.2f", midTime)+"s";
                Utils.writeToFile(file, content);
            }

            if(midTime>3.5) waitTime = 0;
            else waitTime = 4000-(int)(midTime*1000)+new Random().nextInt(200);
            Log.d(TAG, "PDDAssistant: 中间时间："+String.format("%.2f", midTime));
            try {
//                int time = new Random().nextInt(500)+waitTime;
                realTime = midTime + waitTime/1000.0;
                Log.d(TAG, "PDDAssistant: waitTime: "+waitTime/1000.0+"s,    realTime: "+String.format("%.2f", realTime)+"s");
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onSlide = false;
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    static class MyCallBack extends GestureResultCallback {
        public MyCallBack() {
            super();
        }

        @Override
        public void onCompleted(GestureDescription gestureDescription) {
            super.onCompleted(gestureDescription);
        }

        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            super.onCancelled(gestureDescription);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            //拿到根节点
            AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo == null) {
                return;
            }
            if(rootInfo.getPackageName().equals(PDD_Package_Name) &&
                    getAllNodesByText(rootInfo, "拼小圈").isEmpty()) {
                PDDAssistant();
            }
        }catch (Exception e){
            Log.d(TAG, "onAccessibilityEvent: Error");
        }
    }
}
