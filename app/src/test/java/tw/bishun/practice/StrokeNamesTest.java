package tw.bishun.practice;
import org.junit.Test;
import static org.junit.Assert.*;
public class StrokeNamesTest {
    @Test public void missingNameRemainsMissing(){assertNull(StrokeNames.resolve(null,new float[][]{{0,0},{100,100}}));}
    @Test public void longLeftDownTailIsHorizontalLeftFalling(){assertEquals("橫撇",StrokeNames.resolve("橫撇|橫鉤",new float[][]{{100,700},{400,700},{200,400}}));}
    @Test public void shortTailIsHorizontalHook(){assertEquals("橫鉤",StrokeNames.resolve("橫撇|橫鉤",new float[][]{{100,700},{400,700},{370,670}}));}
    @Test public void steepSlantedHookDiffersFromLyingHook(){assertEquals("斜鉤",StrokeNames.resolve("斜鉤|臥鉤",new float[][]{{300,800},{500,200}}));assertEquals("臥鉤",StrokeNames.resolve("斜鉤|臥鉤",new float[][]{{300,300},{700,200}}));}
}
