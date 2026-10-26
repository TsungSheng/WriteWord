package tw.bishun.practice;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private PracticeView board; private TextView title,status,progress; private EditText input;
    private StrokeOverviewView overview; private LinearLayout overviewPanel; private ScrollView overviewScroll;
    private StrokeNames names; private boolean overviewVisible; private Button overviewButton;
    private String current="永"; private int request=0; private TextToSpeech speech; private boolean speechReady,foreground;
    private final int green=0xff16765b;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xfff6f8f3);getWindow().setNavigationBarColor(0xfff6f8f3);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(20,8,20,8);root.setBackgroundColor(0xfff6f8f3);
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(20,insets.getSystemWindowInsetTop()+8,20,insets.getSystemWindowInsetBottom()+8);return insets;});
        setContentView(root);
        TextView brand=text("筆順王   ·   每天一字，慢慢寫好",18);brand.setTextColor(green);root.addView(brand);
        LinearLayout search=new LinearLayout(this);input=new EditText(this);input.setSingleLine();input.setHint("輸入正體國字，例如：學習");input.setText("永");input.setTextSize(18);
        search.addView(input,new LinearLayout.LayoutParams(0,56,1));search.addView(button("開始",v->search()));root.addView(search);
        title=text("永",40);title.setGravity(17);title.setTextColor(green);root.addView(title);
        TextView subtitle=text("逐筆練習  ━━━━━━━━━",14);subtitle.setGravity(17);subtitle.setTextColor(green);root.addView(subtitle);
        progress=text("載入中…",14);progress.setGravity(17);root.addView(progress);
        names=new StrokeNames(this);
        LinearLayout practiceArea=new LinearLayout(this);
        board=new PracticeView(this);practiceArea.addView(board,new LinearLayout.LayoutParams(0,-1,1));
        overviewPanel=new LinearLayout(this);overviewPanel.setOrientation(1);
        Button closeOverview=button("全筆順 ×",v->setOverview(false));closeOverview.setContentDescription("隱藏全筆順");overviewPanel.addView(closeOverview,new LinearLayout.LayoutParams(-1,-2));
        overviewScroll=new ScrollView(this);overview=new StrokeOverviewView(this);overview.setContentDescription("全筆順累積字形，依序顯示每一筆");overviewScroll.addView(overview,new ScrollView.LayoutParams(-1,-2));
        overviewPanel.addView(overviewScroll,new LinearLayout.LayoutParams(-1,0,1));
        practiceArea.addView(overviewPanel,new LinearLayout.LayoutParams(Math.round(96*getResources().getDisplayMetrics().density),-1));
        overviewPanel.setVisibility(View.GONE);root.addView(practiceArea,new LinearLayout.LayoutParams(-1,0,1));
        status=text("",16);status.setGravity(17);root.addView(status);
        board.listener=(done,total,message)->{progress.setText(total==0?"準備練習":"已完成 "+done+" / "+total+" 筆");status.setText(message);overview.setProgress(done);
            if(overviewVisible) overviewScroll.post(()->overviewScroll.smoothScrollTo(0,Math.max(0,Math.round((done-2)*108*getResources().getDisplayMetrics().density))));};
        board.strokeListener=(number,name,demo)->{speak(name==null?"第"+number+"筆":name,TextToSpeech.QUEUE_ADD);};
        LinearLayout tools=new LinearLayout(this);tools.setGravity(17);
        tools.addView(button("重寫",v->{stopSpeech();board.reset();}));tools.addView(button("示範",v->{stopSpeech();board.demonstrate();}));tools.addView(button("提示",v->board.toggleHint()));tools.addView(button("朗讀",v->{if(speechReady)speak(current,TextToSpeech.QUEUE_FLUSH);else Toast.makeText(this,"尚無可用的中文語音",0).show();}));root.addView(tools);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(17);nav.addView(button("☆ 收藏",v->favorite()));nav.addView(button("收藏字",v->favorites()));nav.addView(button("教育部",v->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://stroke-order.learningweb.moe.edu.tw/")))));root.addView(nav);
        overviewButton=button("全筆順",v->setOverview(!overviewVisible));nav.addView(overviewButton);
        setOverview(state==null?getPreferences(0).getBoolean("overview",false):state.getBoolean("overview",false));
        TextView source=text("字形：Make Me a Hanzi／Arphic · 名稱：cnchar\n部分字形筆順與教育部標準不同 · 點此查看說明",11);source.setGravity(17);source.setOnClickListener(v->about());root.addView(source);
        speech=new TextToSpeech(this,s->{if(s==TextToSpeech.SUCCESS){int r=speech.setLanguage(Locale.TAIWAN);speechReady=r>=0;}});
        current=state==null?"永":state.getString("character","永");input.setText(current);load(current);
    }
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xff33453b);t.setPadding(0,5,0,5);return t;}
    private Button button(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextSize(14);b.setMinWidth(0);b.setMinimumWidth(0);b.setTextColor(green);b.setOnClickListener(l);return b;}
    private void search(){String value=input.getText().toString().trim();ArrayList<String> chars=new ArrayList<>();value.codePoints().filter(cp->Character.UnicodeScript.of(cp)==Character.UnicodeScript.HAN).limit(20).forEach(cp->chars.add(new String(Character.toChars(cp))));if(chars.isEmpty()){Toast.makeText(this,"請輸入國字",0).show();return;}if(chars.size()==1)load(chars.get(0));else new AlertDialog.Builder(this).setTitle("選擇要練習的字").setItems(chars.toArray(new String[0]),(d,n)->load(chars.get(n))).show();}
    private void speak(String text,int queue){if(foreground && speechReady && speech!=null)speech.speak(text,queue,null,"speech-"+System.nanoTime());}
    private void stopSpeech(){if(speech!=null)speech.stop();}
    private void setOverview(boolean visible){overviewVisible=visible;overviewPanel.setVisibility(visible?View.VISIBLE:View.GONE);overviewButton.setText(visible?"隱藏筆順":"全筆順");getPreferences(0).edit().putBoolean("overview",visible).apply();}
    private void load(String character){stopSpeech();int token=++request;current=character;title.setText(character);board.clear();overview.setData(Collections.emptyList(),new String[0]);overviewScroll.scrollTo(0,0);progress.setText("載入筆順…");status.setText("第一次查字需要網路，之後可離線練習");
        worker.submit(()->{try{String json;File file=new File(getFilesDir(),Integer.toHexString(character.codePointAt(0))+".json");
            if(file.exists())json=new String(java.nio.file.Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8);
            else {try(InputStream bundled=getAssets().open("data/"+character+".json")){json=new String(read(bundled),StandardCharsets.UTF_8);}catch(IOException missing){
                URL url=new URL("https://cdn.jsdelivr.net/npm/hanzi-writer-data@2.0.1/"+URLEncoder.encode(character,"UTF-8")+".json");HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);
                try{if(c.getResponseCode()!=200)throw new IOException("無此字資料");try(InputStream in=c.getInputStream()){json=new String(read(in),StandardCharsets.UTF_8);}}finally{c.disconnect();}
            }new JSONObject(json).getJSONArray("medians");java.nio.file.Files.write(file.toPath(),json.getBytes(StandardCharsets.UTF_8));}
            JSONObject data=new JSONObject(json);runOnUiThread(()->{if(token!=request||isDestroyed())return;try{board.load(data,names.forCharacter(character,data.getJSONArray("strokes").length()));overview.setData(board.getStrokes(),board.getStrokeNames());}catch(Exception e){status.setText("筆順格式錯誤，請換一個字");}});
        }catch(Exception e){runOnUiThread(()->{if(token!=request||isDestroyed())return;progress.setText("無法載入「"+character+"」");status.setText("請檢查網路或改查其他字，再按開始重試");});}});
    }
    private byte[] read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>2000000)throw new IOException("資料過大");}return out.toByteArray();}
    private void favorite(){Set<String> set=new TreeSet<>(getPreferences(0).getStringSet("favorites",Collections.emptySet()));boolean removed=!set.add(current);if(removed)set.remove(current);getPreferences(0).edit().putStringSet("favorites",set).apply();Toast.makeText(this,removed?"已取消收藏":"已收藏「"+current+"」",0).show();}
    private void favorites(){String[] items=new TreeSet<>(getPreferences(0).getStringSet("favorites",Collections.emptySet())).toArray(new String[0]);new AlertDialog.Builder(this).setTitle(items.length==0?"尚未收藏國字":"我的收藏").setItems(items,(d,n)->{input.setText(items[n]);load(items[n]);}).setPositiveButton("關閉",null).show();}
    private void about(){new AlertDialog.Builder(this).setTitle("資料來源與使用說明").setMessage("從紫色圓點起筆，依箭頭方向一筆一畫描寫。寫對後會朗讀該筆名稱；示範時也會逐筆朗讀。按「全筆順」可顯示或隱藏累積筆順字形，向上滑動可查看後面的筆畫。\n\n筆畫名稱參考 cnchar 3.2.6（MIT 授權），另有對齊本 App 字形的人工修訂。名稱資料缺少或筆畫數不同時，會改唸「第幾筆」。目前僅核對部分範例，尚未逐字驗證兩份資料的筆順完全相同。\n\n字形資料：Hanzi Writer Data 2.0.1／Make Me a Hanzi，原始字型：文鼎科技，依 Arphic Public License 使用。授權全文隨 App 附於 assets/licenses。\n\n這份開放資料並非教育部標準字形資料庫，部分國字的字形與筆順可能不同。教育部按鈕會開啟官方網站查核。\n\n不會將手寫軌跡上傳；查字時會向 jsDelivr 下載該字資料。已下載資料與收藏保存在本機。").setPositiveButton("知道了",null).show();}
    @Override protected void onSaveInstanceState(Bundle b){b.putString("character",current);b.putBoolean("overview",overviewVisible);super.onSaveInstanceState(b);}
    @Override protected void onResume(){super.onResume();foreground=true;}
    @Override protected void onPause(){foreground=false;stopSpeech();super.onPause();}
    @Override protected void onDestroy(){request++;worker.shutdownNow();if(speech!=null)speech.shutdown();super.onDestroy();}
}
