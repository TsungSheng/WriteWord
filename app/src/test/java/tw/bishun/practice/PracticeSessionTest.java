package tw.bishun.practice;

import org.junit.Test;
import static org.junit.Assert.*;

public class PracticeSessionTest {
    @Test public void beginsAtFirstAndKeepsDuplicates(){
        PracticeSession session=new PracticeSession();assertTrue(session.start("人人大"));
        assertEquals("人",session.current());assertEquals(3,session.size());
        assertTrue(session.completeCurrent());assertTrue(session.advance());
        assertEquals("人",session.current());assertEquals(1,session.position());
        session.completeCurrent();assertTrue(session.advance());assertEquals("大",session.current());
        session.completeCurrent();assertFalse(session.advance());assertFalse(session.hasNext());
    }
    @Test public void cannotAdvanceBeforeCompletionOrCompleteTwice(){
        PracticeSession session=new PracticeSession();session.start("學習");assertFalse(session.advance());
        assertTrue(session.completeCurrent());assertFalse(session.completeCurrent());
        assertTrue(session.advance());assertFalse(session.advance());
    }
    @Test public void replacingQueueClearsOldCompletion(){
        PracticeSession session=new PracticeSession();session.start("學習");session.completeCurrent();
        session.start("中文");assertFalse(session.isComplete());assertEquals("中",session.current());assertFalse(session.advance());
    }
    @Test public void retryStaysOnSameCharacter(){
        PracticeSession session=new PracticeSession();session.start("學習");session.completeCurrent();session.advance();
        session.completeCurrent();session.restartCurrent();assertEquals("習",session.current());assertFalse(session.isComplete());
    }
    @Test public void filtersPunctuationAndPreservesSupplementaryHan(){
        PracticeSession session=new PracticeSession();session.start("A 𠀀，永！");
        assertEquals(2,session.size());assertEquals("𠀀",session.current());
        session.completeCurrent();session.advance();assertEquals("永",session.current());
    }
    @Test public void invalidInputDoesNotDestroyCurrentQueue(){
        PracticeSession session=new PracticeSession();session.start("學習");assertFalse(session.start("123!?"));assertEquals("學習",session.text());
    }
    @Test public void restoresPositionAndRequiresCurrentCharacterToBeWrittenAgain(){
        PracticeSession session=new PracticeSession();session.restore("學習中文",2);
        assertEquals("中",session.current());assertEquals(4,session.size());assertFalse(session.isComplete());assertFalse(session.advance());
        session.restore("學習",99);assertEquals("習",session.current());
    }
    @Test public void respectsTwentyCharacterLimitWithoutDeduplication(){
        PracticeSession session=new PracticeSession();session.start("人".repeat(25));assertEquals(20,session.size());
    }
    @Test public void manualNavigationDoesNotRequireCompletingCurrentCharacter(){
        PracticeSession session=new PracticeSession();session.start("人人大");
        assertFalse(session.hasPrevious());assertTrue(session.move(1));assertEquals(1,session.position());assertEquals("人",session.current());
        assertTrue(session.move(1));assertEquals("大",session.current());assertTrue(session.hasPrevious());assertFalse(session.hasNext());
        assertTrue(session.move(-1));assertEquals("人",session.current());assertEquals(1,session.position());
    }
    @Test public void manualJumpClearsCompletionAndBlocksStaleAutoAdvance(){
        PracticeSession session=new PracticeSession();session.start("學習中文");session.completeCurrent();
        assertTrue(session.move(1));assertEquals("習",session.current());assertFalse(session.isComplete());assertFalse(session.advance());
        session.completeCurrent();assertTrue(session.move(-1));assertEquals("學",session.current());assertFalse(session.isComplete());
    }
    @Test public void navigationDoesNotWrapAtEitherBoundary(){
        PracticeSession session=new PracticeSession();session.start("學習");
        assertFalse(session.move(-1));assertEquals("學",session.current());session.move(1);session.completeCurrent();
        assertFalse(session.move(1));assertEquals("習",session.current());assertTrue(session.isComplete());
        session.start("永");assertFalse(session.hasPrevious());assertFalse(session.hasNext());assertFalse(session.move(-1));assertFalse(session.move(1));
    }
}
