package tw.bishun.practice;

import java.util.*;

/** Ordered practice queue. Duplicate characters and supplementary Han are kept. */
public final class PracticeSession {
    private List<String> characters=Collections.emptyList();
    private int index;
    private boolean complete;

    public boolean start(String text) {
        ArrayList<String> parsed=new ArrayList<>();
        if(text!=null)text.codePoints()
            .filter(cp->Character.UnicodeScript.of(cp)==Character.UnicodeScript.HAN)
            .limit(20).forEach(cp->parsed.add(new String(Character.toChars(cp))));
        if(parsed.isEmpty())return false;
        characters=parsed;index=0;complete=false;return true;
    }
    public void restore(String text,int position) {
        if(!start(text))start("永");
        index=Math.max(0,Math.min(position,characters.size()-1));
    }
    public String current(){return characters.isEmpty()?"永":characters.get(index);}
    public String text(){return String.join("",characters);}
    public int position(){return index;}
    public int size(){return characters.size();}
    public boolean isComplete(){return complete;}
    public boolean hasPrevious(){return index>0;}
    public boolean hasNext(){return index+1<characters.size();}
    public boolean move(int offset){
        int target=index+offset;
        if(offset==0||target<0||target>=characters.size())return false;
        index=target;complete=false;return true;
    }
    public boolean completeCurrent(){if(complete)return false;complete=true;return true;}
    public void restartCurrent(){complete=false;}
    public boolean advance(){if(!complete||!hasNext())return false;index++;complete=false;return true;}
}
