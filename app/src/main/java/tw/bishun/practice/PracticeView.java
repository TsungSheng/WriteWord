package tw.bishun.practice;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import androidx.core.graphics.PathParser;
import org.json.*;
import java.util.*;

public class PracticeView extends View {
    private final Paint paint=new Paint(3);
    private final ArrayList<Path> strokes=new ArrayList<>();
    private final ArrayList<float[][]> medians=new ArrayList<>();
    private final ArrayList<float[]> gesture=new ArrayList<>();
    private String[] strokeNames=new String[0];
    public interface StrokeListener { void onStroke(int number,String name,boolean demonstration); }
    public StrokeListener strokeListener;
    private int done=0, generation=0; private boolean hint=true, demo=false;
    private float side, left, top;
    public interface Listener { void update(int done,int total,String message); }
    public Listener listener;
    public PracticeView(Context c) { super(c); setContentDescription("國字筆順練習區，沿著提示逐筆書寫"); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
    public void load(JSONObject data,String[] names) throws JSONException {
        reset(); strokes.clear(); medians.clear();
        JSONArray paths=data.getJSONArray("strokes"), points=data.getJSONArray("medians");
        for(int i=0;i<paths.length();i++) {
            strokes.add(PathParser.createPathFromPathData(paths.getString(i)));
            JSONArray line=points.getJSONArray(i); float[][] p=new float[line.length()][2];
            for(int j=0;j<line.length();j++) { p[j][0]=(float)line.getJSONArray(j).getDouble(0); p[j][1]=(float)line.getJSONArray(j).getDouble(1); }
            medians.add(p);
        }
        strokeNames=new String[strokes.size()];
        for(int i=0;i<strokeNames.length;i++) strokeNames[i]=StrokeNames.resolve(i<names.length?names[i]:null,medians.get(i));
        report("沿著起點，寫下第一筆"); invalidate();
    }
    public List<Path> getStrokes(){return Collections.unmodifiableList(strokes);}
    public String[] getStrokeNames(){return strokeNames.clone();}
    public void clear() { reset(); strokes.clear(); medians.clear(); strokeNames=new String[0]; invalidate(); }
    public void reset() { generation++; demo=false; done=0; gesture.clear(); report("重新開始，慢慢寫就好"); invalidate(); }
    public void toggleHint() { hint=!hint; invalidate(); }
    private void report(String s) { if(listener!=null) listener.update(done,strokes.size(),s); }
    public void demonstrate() {
        if(strokes.isEmpty()) return; reset(); demo=true; int token=generation; report("筆順示範中…"); play(token);
    }
    private void play(int token) {
        postDelayed(()-> { if(token!=generation) return; done++; invalidate();
            String name=strokeNames[done-1];report("示範第 "+done+" 筆"+(name==null?"":"："+name));
            if(strokeListener!=null) strokeListener.onStroke(done,name,true);
            if(done<strokes.size()) play(token); else postDelayed(()-> {if(token==generation) reset();},900);
        },1400);
    }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); side=Math.min(getWidth(),getHeight())-24; left=(getWidth()-side)/2; top=(getHeight()-side)/2;
        paint.setStyle(Paint.Style.FILL); paint.setColor(Color.WHITE); c.drawRect(left,top,left+side,top+side,paint);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1); paint.setColor(0xffcfd9d3); c.drawRect(left,top,left+side,top+side,paint);
        paint.setPathEffect(new DashPathEffect(new float[]{12,10},0));
        c.drawLine(left+side/2,top,left+side/2,top+side,paint); c.drawLine(left,top+side/2,left+side,top+side/2,paint); paint.setPathEffect(null);
        c.save(); c.translate(left,top+side*900/1024); c.scale(side/1024,-side/1024);
        for(int i=0;i<strokes.size();i++) {
            paint.setStyle(i<done?Paint.Style.FILL:Paint.Style.STROKE); paint.setStrokeWidth(2); paint.setColor(i<done?0xff16765b:0xffbac4bd); c.drawPath(strokes.get(i),paint);
        }
        if(hint && done<medians.size()) {
            float[][] p=medians.get(done); paint.setColor(0xff9b62b2); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(7); paint.setStrokeCap(Paint.Cap.ROUND);
            Path guide=new Path(); guide.moveTo(p[0][0],p[0][1]); for(int i=1;i<p.length;i++) guide.lineTo(p[i][0],p[i][1]); c.drawPath(guide,paint);
            paint.setStyle(Paint.Style.FILL); c.drawCircle(p[0][0],p[0][1],15,paint);
            int m=Math.max(1,p.length/2); float dx=p[m][0]-p[m-1][0],dy=p[m][1]-p[m-1][1],len=(float)Math.hypot(dx,dy);
            if(len>0) {dx/=len;dy/=len;Path arrow=new Path();arrow.moveTo(p[m][0],p[m][1]);arrow.lineTo(p[m][0]-dx*30-dy*15,p[m][1]-dy*30+dx*15);arrow.lineTo(p[m][0]-dx*30+dy*15,p[m][1]-dy*30-dx*15);arrow.close();c.drawPath(arrow,paint);}
        }
        if(!gesture.isEmpty()) {paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(18);paint.setColor(0xff294e40);Path p=new Path();p.moveTo(gesture.get(0)[0],gesture.get(0)[1]);for(float[] pt:gesture)p.lineTo(pt[0],pt[1]);c.drawPath(p,paint);}
        c.restore();
    }
    private float[] point(MotionEvent e) {return new float[]{(e.getX()-left)*1024/side,900-(e.getY()-top)*1024/side};}
    @Override public boolean onTouchEvent(MotionEvent e) {
        if(demo || done>=medians.size() || side<=0) return false;
        switch(e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: gesture.clear();gesture.add(point(e));getParent().requestDisallowInterceptTouchEvent(true);break;
            case MotionEvent.ACTION_MOVE: gesture.add(point(e));break;
            case MotionEvent.ACTION_UP:
                gesture.add(point(e)); boolean ok=StrokeMatcher.matches(gesture.toArray(new float[0][]),medians.get(done));
                if(ok) done++;
                gesture.clear();
                String name=ok?strokeNames[done-1]:null;
                report(ok?("第 "+done+" 筆"+(name==null?"":"："+name)+(done==strokes.size()?"。完成了！ 🎉":"，很好！")):"再試一次：注意起點、方向和筆畫長度");
                if(ok && strokeListener!=null) strokeListener.onStroke(done,name,false);
                performClick();getParent().requestDisallowInterceptTouchEvent(false);break;
            case MotionEvent.ACTION_CANCEL: gesture.clear();getParent().requestDisallowInterceptTouchEvent(false);break;
        }
        invalidate();return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override protected void onDetachedFromWindow(){generation++;super.onDetachedFromWindow();}
}
