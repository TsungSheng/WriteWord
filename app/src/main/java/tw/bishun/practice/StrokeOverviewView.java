package tw.bishun.practice;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** A scrollable column of cumulative glyphs; exactly one new stroke per row. */
public final class StrokeOverviewView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<Path> paths=new ArrayList<>();
    private String[] names=new String[0]; private int done;
    private final float density;
    public StrokeOverviewView(Context c){super(c);density=getResources().getDisplayMetrics().density;}
    public void setData(List<Path> strokes,String[] strokeNames){paths.clear();paths.addAll(strokes);names=strokeNames.clone();done=0;requestLayout();invalidate();}
    public void setProgress(int value){done=value;invalidate();}
    @Override protected void onMeasure(int widthSpec,int heightSpec){setMeasuredDimension(MeasureSpec.getSize(widthSpec),Math.round(Math.max(1,paths.size())*108*density));}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float row=108*density,w=getWidth();c.drawColor(0xfffafbf7);
        for(int i=0;i<paths.size();i++) {
            float y=i*row;boolean selected=done==i+1;
            paint.setStyle(Paint.Style.FILL);paint.setColor(selected?0xffe0eee4:0xfffafbf7);c.drawRect(0,y,w,y+row,paint);
            paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(11*density);paint.setColor(0xff526b5d);c.drawText(String.valueOf(i+1),w/2,y+14*density,paint);
            float size=Math.min(w-10*density,65*density);
            c.save();c.translate((w-size)/2,y+19*density+size*900/1024);c.scale(size/1024,-size/1024);
            for(int j=0;j<=i;j++){paint.setColor(j==i?0xff16765b:0xff33453b);c.drawPath(paths.get(j),paint);}c.restore();
            paint.setTextSize(10*density);paint.setColor(0xff526b5d);
            String label=i<names.length&&names[i]!=null?names[i]:"第 "+(i+1)+" 筆";
            c.drawText(label,w/2,y+100*density,paint);
        }
    }
}
