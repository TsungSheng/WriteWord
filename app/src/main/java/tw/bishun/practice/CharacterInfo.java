package tw.bishun.practice;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Offline reference metadata. Total strokes always come from the active glyph. */
public final class CharacterInfo {
    private JSONObject dictionary=new JSONObject();
    public CharacterInfo(Context context){
        try(InputStream in=context.getAssets().open("character-info.json")){
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[8192];int n;
            while((n=in.read(bytes))!=-1)out.write(bytes,0,n);
            dictionary=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
        }catch(Exception ignored){}
    }
    public String radical(String character){JSONObject info=dictionary.optJSONObject(character);return info==null?"未收錄":info.optString("radical","").isEmpty()?"未收錄":info.optString("radical");}
    public List<String> readings(String character){
        ArrayList<String> result=new ArrayList<>();JSONObject info=dictionary.optJSONObject(character);
        JSONArray array=info==null?null:info.optJSONArray("readings");
        if(array!=null)for(int i=0;i<array.length();i++)result.add(array.optString(i));
        return result;
    }
}
