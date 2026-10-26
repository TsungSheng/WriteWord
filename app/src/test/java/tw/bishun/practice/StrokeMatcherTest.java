package tw.bishun.practice;
import org.junit.Test;
import static org.junit.Assert.*;
public class StrokeMatcherTest {
    final float[][] target={{100,700},{300,700},{600,700}};
    @Test public void acceptsUnevenTouchSampling(){assertTrue(StrokeMatcher.matches(new float[][]{{105,705},{110,704},{200,690},{590,700}},target));}
    @Test public void rejectsReverse(){assertFalse(StrokeMatcher.matches(new float[][]{{600,700},{100,700}},target));}
    @Test public void rejectsWrongShape(){assertFalse(StrokeMatcher.matches(new float[][]{{100,700},{300,300},{600,700}},target));}
    @Test public void rejectsTap(){assertFalse(StrokeMatcher.matches(new float[][]{{100,700},{100,700}},target));}
}
