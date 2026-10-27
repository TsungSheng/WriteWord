package tw.bishun.practice;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** A horizontal strip of cumulative glyphs; exactly one new stroke per cell. */
public final class StrokeOverviewView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<Path> paths=new ArrayList<>();
    private String[] names=new String[0]; private int done;
    private final float density;
    public StrokeOverviewView(Context c){super(c);density=getResources().getDisplayMetrics().density;}
    public void setData(List<Path> strokes,String[] strokeNames){paths.clear();paths.addAll(strokes);names=strokeNames.clone();done=0;requestLayout();invalidate();}
    public void setProgress(int value){done=value;invalidate();}
    public int getCellWidth(){return Math.round(88*density);}
    @Override protected void onMeasure(int widthSpec,int heightSpec){setMeasuredDimension(resolveSize(Math.max(1,paths.size())*getCellWidth(),widthSpec),resolveSize(Math.round(104*density),heightSpec));}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float w=getCellWidth(),h=getHeight();c.drawColor(AppColors.SURFACE);
        for(int i=0;i<paths.size();i++) {
            float x=i*w;boolean selected=done==i+1;
            paint.setStyle(Paint.Style.FILL);paint.setColor(selected?AppColors.SELECTED:AppColors.SURFACE);c.drawRect(x,0,x+w,h,paint);
            paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(11*density);paint.setColor(AppColors.MUTED);c.drawText(String.valueOf(i+1),x+w/2,14*density,paint);
            float size=Math.min(w-10*density,65*density);
            c.save();c.translate(x+(w-size)/2,19*density+size*900/1024);c.scale(size/1024,-size/1024);
            for(int j=0;j<=i;j++){paint.setColor(j==i?AppColors.ACCENT:AppColors.TEXT);c.drawPath(paths.get(j),paint);}c.restore();
            paint.setTextSize(10*density);paint.setColor(AppColors.MUTED);
            String label=i<names.length&&names[i]!=null?names[i]:"第 "+(i+1)+" 筆";
            c.drawText(label,x+w/2,100*density,paint);
        }
    }
}
