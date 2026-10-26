package tw.bishun.practice;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Reference names come from cnchar 3.2.6, not the Ministry of Education. */
public final class StrokeNames {
    private final JSONObject dictionary;
    public StrokeNames(Context context) {
        JSONObject parsed;
        try (InputStream in=context.getAssets().open("stroke-names.json")) {
            ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buffer=new byte[8192]; int n;
            while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
            parsed=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
        } catch(Exception e) { parsed=new JSONObject(); }
        dictionary=parsed;
    }
    public String[] forCharacter(String character,int count) {
        String[] result=new String[count]; JSONArray names=dictionary.optJSONArray(character);
        // Do not align by index when upstream sources disagree about the stroke count.
        if(names!=null && names.length()==count) for(int i=0;i<count;i++) result[i]=names.optString(i,null);
        return result;
    }
    public static String resolve(String value,float[][] points) {
        if(value==null || value.isEmpty()) return null;
        if(!value.contains("|")) return value;
        // cnchar groups several shapes under the same code. Resolve the common
        // horizontal-hook / horizontal-left-falling group from its final segment.
        if(value.equals("橫撇|橫鉤")) {
            float[] end=points[points.length-1]; float maxX=points[0][0],atY=points[0][1];
            for(float[] p:points) if(p[0]>maxX){maxX=p[0];atY=p[1];}
            return atY-end[1]>70 && maxX-end[0]>50?"橫撇":"橫鉤";
        }
        if(value.equals("斜鉤|臥鉤")) {
            float[] first=points[0],last=points[points.length-1];
            return Math.abs(first[1]-last[1])>Math.abs(first[0]-last[0])*.65f?"斜鉤":"臥鉤";
        }
        if(value.equals("橫折折折鉤|橫撇彎鉤")) return "橫撇彎鉤";
        return value.split("\\|")[0];
    }
}
