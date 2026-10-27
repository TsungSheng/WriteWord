package tw.bishun.practice;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private PracticeView board; private TextView title,status,progress,details,otherReadings; private EditText input;
    private StrokeOverviewView overview; private LinearLayout overviewPanel; private HorizontalScrollView overviewScroll;
    private StrokeNames names; private boolean overviewVisible; private Button overviewButton;
    private CharacterInfo characterInfo;
    private final PracticeSession session=new PracticeSession();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable advanceTask;
    private String completionId;
    private long completionSerial;
    private String current="永"; private int request=0; private TextToSpeech speech; private boolean speechReady,foreground;
    private final int green=AppColors.ACCENT;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setStatusBarColor(AppColors.BACKGROUND);getWindow().setNavigationBarColor(AppColors.BACKGROUND);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(12),dp(8),dp(12),dp(8));root.setBackgroundColor(AppColors.BACKGROUND);
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(12),insets.getSystemWindowInsetTop()+dp(8),dp(12),insets.getSystemWindowInsetBottom()+dp(8));return insets;});
        setContentView(root);
        LinearLayout search=new LinearLayout(this);search.setGravity(Gravity.CENTER_VERTICAL);search.setBaselineAligned(false);search.setPadding(0,dp(8),0,dp(4));
        input=new EditText(this);input.setSingleLine();input.setHint("輸入國字或詞語，最多20字");input.setContentDescription("輸入要依序練習的國字或詞語，最多20字");input.setText("永");input.setTextSize(18);
        input.setTextColor(AppColors.TEXT);input.setHintTextColor(AppColors.MUTED);
        GradientDrawable inputBackground=new GradientDrawable();inputBackground.setColor(AppColors.SURFACE);inputBackground.setCornerRadius(dp(10));inputBackground.setStroke(dp(1),AppColors.BORDER);input.setBackground(inputBackground);
        // Let the text and font padding determine height, with a density-aware
        // minimum. A fixed 56 physical pixels clipped text on high-density phones.
        input.setMinHeight(dp(56));input.setGravity(Gravity.CENTER_VERTICAL);input.setPadding(dp(12),dp(8),dp(12),dp(8));
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);input.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_SEARCH){search();return true;}return false;});
        search.addView(input,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        Button start=button("開始",v->search());start.setMinHeight(dp(56));
        LinearLayout.LayoutParams startParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);startParams.leftMargin=dp(8);search.addView(start,startParams);
        root.addView(search,new LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout infoLine=new LinearLayout(this);infoLine.setGravity(Gravity.CENTER_VERTICAL);infoLine.setPadding(dp(4),0,dp(4),0);
        title=text("永",24);title.setTextColor(green);title.setPadding(0,0,dp(12),0);infoLine.addView(title);
        details=text("",14);infoLine.addView(details,new LinearLayout.LayoutParams(0,-2,1));root.addView(infoLine);
        otherReadings=text("",14);otherReadings.setTextColor(green);otherReadings.setVisibility(View.GONE);root.addView(otherReadings);
        progress=text("載入中…",14);progress.setGravity(17);root.addView(progress);
        names=new StrokeNames(this);characterInfo=new CharacterInfo(this);
        LinearLayout practiceArea=new LinearLayout(this);practiceArea.setOrientation(LinearLayout.VERTICAL);
        board=new PracticeView(this);practiceArea.addView(board,new LinearLayout.LayoutParams(-1,0,1));
        board.navigationListener=new PracticeView.NavigationListener(){
            @Override public void previous(){jumpCharacter(-1);}
            @Override public void next(){jumpCharacter(1);}
        };
        overviewPanel=new LinearLayout(this);overviewPanel.setGravity(Gravity.CENTER_VERTICAL);
        overviewScroll=new HorizontalScrollView(this);overviewScroll.setFillViewport(true);
        overview=new StrokeOverviewView(this);overview.setContentDescription("全筆順累積字形，由左至右顯示，左右滑動查看");overviewScroll.addView(overview,new HorizontalScrollView.LayoutParams(-2,-1));
        overviewPanel.addView(overviewScroll,new LinearLayout.LayoutParams(0,dp(104),1));
        Button closeOverview=button("×",v->setOverview(false));closeOverview.setContentDescription("隱藏全筆順");overviewPanel.addView(closeOverview,new LinearLayout.LayoutParams(dp(48),dp(48)));
        practiceArea.addView(overviewPanel,new LinearLayout.LayoutParams(-1,-2));
        overviewPanel.setVisibility(View.GONE);root.addView(practiceArea,new LinearLayout.LayoutParams(-1,0,1));
        status=text("",16);status.setGravity(17);root.addView(status);
        board.listener=(done,total,message)->{progress.setText(wordProgress()+(total==0?"準備練習":"已完成 "+done+" / "+total+" 筆"));status.setText(message);overview.setProgress(done);
            if(overviewVisible) overviewScroll.post(()->overviewScroll.smoothScrollTo(Math.max(0,overview.getCellWidth()*(done-2)),0));};
        board.strokeListener=(number,name,demo)->{
            speak(name==null?"第"+number+"筆":name,TextToSpeech.QUEUE_ADD);
            if(number==board.getStrokeCount()){
                if(demo)speak(current,TextToSpeech.QUEUE_ADD);
                else completeCharacter();
            }
        };
        LinearLayout tools=new LinearLayout(this);tools.setGravity(17);
        overviewButton=button("全筆順",v->setOverview(!overviewVisible));tools.addView(overviewButton);
        tools.addView(button("重寫",v->{restartCurrent();board.reset();}));tools.addView(button("示範",v->{restartCurrent();board.demonstrate();}));tools.addView(button("提示",v->board.toggleHint()));tools.addView(button("朗讀",v->readCurrent()));root.addView(tools);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(17);nav.addView(button("☆ 收藏",v->favorite()));nav.addView(button("收藏字",v->favorites()));nav.addView(button("教育部",v->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://stroke-order.learningweb.moe.edu.tw/")))));root.addView(nav);
        setOverview(state==null?getPreferences(0).getBoolean("overview",false):state.getBoolean("overview",false));
        Button info=button("說明",v->about());info.setContentDescription("資料來源與使用說明");nav.addView(info);
        distributeButtons(tools);distributeButtons(nav);
        speech=new TextToSpeech(this,s->{if(s==TextToSpeech.SUCCESS){int r=speech.setLanguage(Locale.TAIWAN);speechReady=r>=0;}});
        speech.setOnUtteranceProgressListener(new UtteranceProgressListener(){
            @Override public void onStart(String id){}
            @Override public void onDone(String id){handler.post(()->{if(!isDestroyed())scheduleAdvance(id,700);});}
            @Override public void onError(String id){handler.post(()->{if(!isDestroyed())scheduleAdvance(id,1000);});}
        });
        if(state==null)session.start("永");else session.restore(state.getString("sessionText",state.getString("character","永")),state.getInt("sessionIndex",0));
        input.setText(state==null?session.text():state.getString("inputText",session.text()));load(session.current());
    }
    private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(AppColors.TEXT);t.setPadding(0,5,0,5);return t;}
    private Button button(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextSize(14);b.setAllCaps(false);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(dp(48));b.setTextColor(green);b.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{AppColors.SELECTED,AppColors.BUTTON}));b.setOnClickListener(l);return b;}
    private void distributeButtons(LinearLayout row){for(int i=0;i<row.getChildCount();i++){View item=row.getChildAt(i);item.setPadding(dp(3),item.getPaddingTop(),dp(3),item.getPaddingBottom());item.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));}}
    private String wordProgress(){return session.size()>1?"第 "+(session.position()+1)+" / "+session.size()+" 字 · ":"";}
    private void search(){String value=input.getText().toString().trim();if(!session.start(value)){Toast.makeText(this,"請輸入國字",0).show();return;}
        InputMethodManager keyboard=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(keyboard!=null)keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);input.clearFocus();
        load(session.current());}
    private int speakWithId(String text,int queue,String id){return foreground&&speechReady&&speech!=null?speech.speak(text,queue,null,id):TextToSpeech.ERROR;}
    private void speak(String text,int queue){speakWithId(text,queue,"speech-"+System.nanoTime());}
    private void stopSpeech(){if(speech!=null)speech.stop();}
    private void readCurrent(){
        if(!speechReady){Toast.makeText(this,"尚無可用的中文語音",0).show();return;}
        if(!session.isComplete()){speak(current,TextToSpeech.QUEUE_FLUSH);return;}
        completionId="complete-"+(++completionSerial);
        int result=speakWithId(current,TextToSpeech.QUEUE_FLUSH,completionId);
        scheduleAdvance(completionId,result==TextToSpeech.SUCCESS?15000:1200);
    }
    private void cancelAdvance(){if(advanceTask!=null)handler.removeCallbacks(advanceTask);advanceTask=null;completionId=null;}
    private void restartCurrent(){cancelAdvance();stopSpeech();session.restartCurrent();}
    private void jumpCharacter(int direction){if(session.move(direction))load(session.current());}
    private void completeCharacter(){
        if(!session.completeCurrent())return;
        completionId="complete-"+(++completionSerial);
        int result=speakWithId(current,TextToSpeech.QUEUE_ADD,completionId);
        // A broken or unavailable voice engine must not trap a completed session.
        scheduleAdvance(completionId,result==TextToSpeech.SUCCESS?15000:1200);
    }
    private void scheduleAdvance(String id,long delay){
        if(id==null||!id.equals(completionId)||!session.isComplete()||!foreground)return;
        if(advanceTask!=null)handler.removeCallbacks(advanceTask);
        advanceTask=()->{
            if(!id.equals(completionId)||!foreground||isDestroyed()||!session.isComplete())return;
            completionId=null;advanceTask=null;
            if(session.advance())load(session.current());
            else status.setText(session.size()>1?"這組 "+session.size()+" 字全部完成！ 🎉":"這個字完成了！ 🎉");
        };
        handler.postDelayed(advanceTask,delay);
    }
    private void setOverview(boolean visible){overviewVisible=visible;overviewPanel.setVisibility(visible?View.VISIBLE:View.GONE);overviewButton.setText(visible?"隱藏筆順":"全筆順");getPreferences(0).edit().putBoolean("overview",visible).apply();}
    private void updateCharacterInfo(int count){
        board.setNavigationState(session.size()>1,session.hasPrevious(),session.hasNext());
        details.setText("部首："+characterInfo.radical(current)+"　總筆畫："+(count>0?count:"—"));
        List<String> readings=characterInfo.readings(current);board.setPronunciation(readings.isEmpty()?"":readings.get(0));
        otherReadings.setVisibility(readings.size()>1?View.VISIBLE:View.GONE);
        otherReadings.setText(readings.size()>1?"其他讀音："+String.join("　／　",readings.subList(1,readings.size())):"");
        if(readings.isEmpty()){otherReadings.setText("注音：尚未收錄");otherReadings.setVisibility(View.VISIBLE);}
    }
    private void load(String character){cancelAdvance();stopSpeech();int token=++request;current=character;title.setText(character);board.clear();updateCharacterInfo(0);overview.setData(Collections.emptyList(),new String[0]);overviewScroll.scrollTo(0,0);progress.setText(wordProgress()+"載入筆順…");status.setText("第一次查字需要網路，之後可離線練習");
        worker.submit(()->{try{String json;File file=new File(getFilesDir(),Integer.toHexString(character.codePointAt(0))+".json");
            if(file.exists())json=new String(java.nio.file.Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8);
            else {try(InputStream bundled=getAssets().open("data/"+character+".json")){json=new String(read(bundled),StandardCharsets.UTF_8);}catch(IOException missing){
                URL url=new URL("https://cdn.jsdelivr.net/npm/hanzi-writer-data@2.0.1/"+URLEncoder.encode(character,"UTF-8")+".json");HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);
                try{if(c.getResponseCode()!=200)throw new IOException("無此字資料");try(InputStream in=c.getInputStream()){json=new String(read(in),StandardCharsets.UTF_8);}}finally{c.disconnect();}
            }new JSONObject(json).getJSONArray("medians");java.nio.file.Files.write(file.toPath(),json.getBytes(StandardCharsets.UTF_8));}
            JSONObject data=new JSONObject(json);runOnUiThread(()->{if(token!=request||isDestroyed())return;try{board.load(data,names.forCharacter(character,data.getJSONArray("strokes").length()));overview.setData(board.getStrokes(),board.getStrokeNames());updateCharacterInfo(board.getStrokeCount());}catch(Exception e){status.setText("筆順格式錯誤，請換一個字");}});
        }catch(Exception e){runOnUiThread(()->{if(token!=request||isDestroyed())return;progress.setText("無法載入「"+character+"」");status.setText("請檢查網路或改查其他字，再按開始重試");});}});
    }
    private byte[] read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>2000000)throw new IOException("資料過大");}return out.toByteArray();}
    private void favorite(){Set<String> set=new TreeSet<>(getPreferences(0).getStringSet("favorites",Collections.emptySet()));boolean removed=!set.add(current);if(removed)set.remove(current);getPreferences(0).edit().putStringSet("favorites",set).apply();Toast.makeText(this,removed?"已取消收藏":"已收藏「"+current+"」",0).show();}
    private void favorites(){String[] items=new TreeSet<>(getPreferences(0).getStringSet("favorites",Collections.emptySet())).toArray(new String[0]);new AlertDialog.Builder(this).setTitle(items.length==0?"尚未收藏國字":"我的收藏").setItems(items,(d,n)->{input.setText(items[n]);session.start(items[n]);load(session.current());}).setPositiveButton("關閉",null).show();}
    private void about(){
        String instructions="可一次輸入最多20個國字，從第一個字依序練習。多字時可按注音上方「上一字」或下方「下一字」跳字；跳字後從所選字第一筆重練，首尾不循環。寫對後會朗讀筆畫名稱；完成一字會朗讀國字，再自動切到下一字。整組完成後停在最後一字。示範只示範目前國字，不會跳字；重寫會重練目前國字。第一排「全筆順」可顯示或隱藏下方橫向筆順。";
        String metadata="注音與部首參考 cnchar / cnchar-trad / cnchar-radical 3.2.6（MIT 授權），拼音轉換為注音。大字旁顯示主要讀音，其他讀音列於上方。部分讀音與臺灣用法可能不同，也可能缺少異讀，請使用教育部入口查核。語音依手機中文引擎的預設讀音，不保證與多音字的首個注音相同。總筆畫數以目前練習字形實際筆數為準。";
        String labels="筆畫名稱參考 cnchar 3.2.6，另有對齊本 App 字形的人工修訂。名稱缺少或筆畫數不同時會改唸「第幾筆」，尚未逐字驗證兩份資料的筆順完全相同。";
        String license="字形及「陳」字圖示：Hanzi Writer Data 2.0.1／Make Me a Hanzi，原始字型：文鼎科技，依 Arphic Public License 使用。完整授權附於 assets/licenses。這份開放資料並非教育部標準字形資料庫。";
        String privacy="不會將手寫軌跡上傳；查字時會向 jsDelivr 下載該字資料。已下載資料與收藏保存在本機。";
        new AlertDialog.Builder(this).setTitle("資料來源與使用說明").setMessage(String.join("\n\n",instructions,metadata,labels,license,privacy)).setPositiveButton("知道了",null).show();
    }
    @Override protected void onSaveInstanceState(Bundle b){b.putString("character",current);b.putBoolean("overview",overviewVisible);b.putString("sessionText",session.text());b.putInt("sessionIndex",session.position());b.putString("inputText",input.getText().toString());super.onSaveInstanceState(b);}
    @Override protected void onResume(){super.onResume();foreground=true;if(session.isComplete()){completionId="complete-"+(++completionSerial);scheduleAdvance(completionId,900);}}
    @Override protected void onPause(){foreground=false;cancelAdvance();stopSpeech();super.onPause();}
    @Override protected void onDestroy(){request++;cancelAdvance();handler.removeCallbacksAndMessages(null);worker.shutdownNow();if(speech!=null)speech.shutdown();super.onDestroy();}
}
