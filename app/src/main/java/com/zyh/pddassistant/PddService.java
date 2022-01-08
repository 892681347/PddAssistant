package com.zyh.pddassistant;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PddService extends AccessibilityService {
    private static final String TAG = "Service";
    private static final String PDD_Package_Name = "com.xunmeng.pinduoduo";
    private static final String DianTao_Package_Name = "com.taobao.live";
    private boolean onSlide = false;
    private long offsetTime;
    private double menuTime = 0;
    private boolean inMenu = false;
    private boolean inBrowse = false;
    private double browseTime = 0;
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        Toast.makeText(getApplicationContext(),"开启服务", Toast.LENGTH_SHORT).show();
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
        String[] texts = new String[]{"^喝水赚钱!^全部领取","^可领取", "^可打卡",".*我喝橙汁了，打个卡","收下奖励","^去升级",
                "去看\\d{2}:\\d{2}视频领奖励",".*领取额外健康补贴","\\d{2}:\\d{2}:\\d{2}后来喝橙汁",
                ".*后喝橙汁打卡","明天继续喝水打卡",".*我喝水了，打个卡"};
        for(String text : texts){
            boolean action = performAllActionByText(root,text);
            if(text.equals("明天继续喝水打卡") && action) back();
        }
        /*
        //HH:MM:SS后来喝橙汁
        List<AccessibilityNodeInfo> laterDrinkJuiceNodes = getAllNodesByText(root,"后来喝橙汁");
        if(laterDrinkJuiceNodes.isEmpty()) Log.d(TAG, "后来喝橙汁emtpy");
        else if(laterDrinkJuiceNodes.size()==2){
            Log.d(TAG, "后来喝橙汁——执行");
            laterDrinkJuiceNodes.get(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else Log.d(TAG, "后来喝橙汁数量："+laterDrinkJuiceNodes.size());
        //后喝橙汁打卡
        List<AccessibilityNodeInfo> laterDrinkJuiceClockNodes = getAllNodesByText(root,"后喝橙汁打卡");
        if(laterDrinkJuiceClockNodes.isEmpty()) Log.d(TAG, "后喝橙汁打卡汁emtpy");
        else if(laterDrinkJuiceClockNodes.size()==1 &&
                getAllNodesByText(root,"收下奖励").isEmpty() && laterDrinkJuiceNodes.isEmpty()){
            Log.d(TAG, "后喝橙汁打卡——执行");
            laterDrinkJuiceClockNodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else Log.d(TAG, "后喝橙汁打卡数量："+laterDrinkJuiceClockNodes.size());
         */
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
        String[] texts = new String[]{"^可开启", "看视频\\d{2}:\\d{2}领红包"};
        for(String text : texts){
            performAllActionByText(root,text);
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
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 500L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    private void backInSimulator(){//90,220
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "返回");
            Path path = new Path();
            path.moveTo(90,220);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 500L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    private void openMenu(){//90,220
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "openMenu");
            Path path = new Path();
            path.moveTo(1325,590);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 500L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    private void browse(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        double nowTime = (new Date().getTime()-offsetTime)/60000.0;
        if(!inMenu){ //菜单未打开
            if(menuTime==0 || (nowTime- menuTime)>=5){
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
                if(performOneActionByText(root,"^1000金币")){
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
            Path path = new Path();
//                path.moveTo(500, 1287);//设置Path的起点
//                path.quadTo(450, 1036, 90, 864);
            path.moveTo(750, 1500);//设置Path的起点
            path.lineTo(750,1000);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 500L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    private void slideInSimulator(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(750, 2000);//设置Path的起点
            path.lineTo(750,100);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 100L, 800L)).build();
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
    private void PDDAssistant(){//仿滑动
        if(onSlide) return;
        Log.d(TAG, "PDDAssistant");
        new Thread(()->{
            onSlide = true;
            Log.d(TAG, "allTexts: "+getAllTexts(getRootInActiveWindow()));
//            back();
//            browse();
            if(!inMenu){
                drink();
                meal();
                redPacket();
                sleep();
            }else{
                if(inBrowse) Log.d(TAG, "inBrowse");
                else Log.d(TAG, "inMenu");
            }
            slide();
            try {
                int time = new Random().nextInt(500)+3000;
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onSlide = false;
        }).start();
    }
    private void clickGoldIngot(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "点击元宝");
            Path path = new Path();
            path.moveTo(1325,875);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 500L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }//backInSimulator()
    private void clickCloseInSimulator(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "关闭窗口");
            Path path = new Path();
            path.moveTo(1250,800);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription description = builder.addStroke(new GestureDescription.StrokeDescription(path, 500L, 100L)).build();
            dispatchGesture(description, new MyCallBack(), null);
        }
    }
    private void DTAssistant(){
        if(onSlide) return;
        Log.d(TAG, "DTAssistant");
        new Thread(()->{
            onSlide = true;
            AccessibilityNodeInfo root = getRootInActiveWindow();
            Log.d(TAG, "allTexts: "+getAllTexts(root));
            int time = 10000;
            if(!getAllNodesByText(root,"^元宝中心$").isEmpty()){
                Log.d(TAG, "DTAssistant: 元宝中心");//邀请好友 再赚38元
                boolean action = false;
                String[] texts = new String[]{"^领取","邀请好友 再赚.*元"};
                for(String text : texts){
                    action|=performAllActionByText(root,text);
                }
                if(action) time = 2000;
                else back();
            }else if(!getAllNodesByText(root,"^6/6$").isEmpty()){
                clickGoldIngot();
                time = 2000;
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onSlide = false;
        }).start();
    }
    private void DTAssistantInSimulator(){
        if(onSlide) return;
        Log.d(TAG, "DTAssistant");
        new Thread(()->{
            onSlide = true;
            AccessibilityNodeInfo root = getRootInActiveWindow();
            Log.d(TAG, "allTexts: "+getAllTexts(root));
            int time = 10000;
            if(!getAllNodesByText(root,"^元宝中心$").isEmpty()){
                Log.d(TAG, "DTAssistant: 元宝中心");//邀请好友 再赚38元
                if(performAllActionByText(root,"^领取")) time = 2000;
                else if(performAllActionByText(root,"去看直播赚.*元宝")) time = 2000;
                else if(!getAllNodesByText(root,"邀请好友 再赚.*元").isEmpty()) {
                    time = 2000;
                    clickCloseInSimulator();
                }else if(!getAllNodesByText(root,"走路赚元宝 每日.*元宝").isEmpty()) {
                    time = 2000;
                    clickCloseInSimulator();
                }
                else {
                    backInSimulator();
                }
            }else if(!getAllNodesByText(root,"^6/6$").isEmpty()){
                slideInSimulator();
                clickGoldIngot();
                time = 2000;
            }else{
                slideInSimulator();
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onSlide = false;
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    class MyCallBack extends GestureResultCallback {

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
    void printEvent(AccessibilityEvent event){
        String pkgName = event.getPackageName().toString();
        int eventType = event.getEventType();
        Log.d(TAG, "eventType: " + eventType + " pkgName: " + pkgName);
    }
    boolean canReceiveRedPacket(AccessibilityEvent event){
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
                //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                if (content.contains("可领取")) {
                    return true;
                }
            }
        }
        return false;
    }
    public List<AccessibilityNodeInfo> findNodesByText(String text) {
        AccessibilityNodeInfo noteInfo = getRootInActiveWindow();
        if (noteInfo != null) {
            return noteInfo.findAccessibilityNodeInfosByText(text);
        }
        return null;
    }
    /**
     * 根据View的ID搜索符合条件的节点,精确搜索方式;
     * 这个只适用于自己写的界面，因为ID可能重复
     * api要求18及以上
     * @param viewId
     */
    public List<AccessibilityNodeInfo> findNodesById(AccessibilityEvent event,String viewId) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            if (Build.VERSION.SDK_INT >= 18) {
                return nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
            }else Log.d(TAG, "findNodesById: SDK_INT < 18");
        }else Log.d(TAG, "findNodesById: null");
        return null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        printEvent(event);
        if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
//            Log.d(TAG, "onAccessibilityEvent: 窗口状态更改");
        }else if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
//            Log.d(TAG, "onAccessibilityEvent: 窗口内容更改");

//            newer_time_redpack
        }
        try {
            //拿到根节点
            AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo == null) {
                return;
            }
            if(rootInfo.getPackageName().equals(PDD_Package_Name)) {
                if(getAllNodesByText(rootInfo, "拼小圈").isEmpty()) PDDAssistant();
            }else if(rootInfo.getPackageName().equals(DianTao_Package_Name)) DTAssistantInSimulator();
        }catch (Exception e){
            Log.d(TAG, "onAccessibilityEvent: Error");
        }
    }
}
