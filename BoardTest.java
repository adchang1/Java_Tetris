import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.*;

public class BoardTest {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated;

	// This shows how to build things in setUp() to re-use
	// across tests.
	
	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	@Before
	public void setUp() throws Exception {
		b = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		b.place(pyr1, 0, 0);
	}
	
	// Check the basic width/height/max after the one placement
	@Test
	public void testSample1() {
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(1, b.getColumnHeight(2));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}
	
	// Place sRotated into the board, then check some measures
	@Test
	public void testSample2() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
		
		//recheck all board params now that we added another piece
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(4, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(2, b.getRowWidth(2));
		assertEquals(false, b.getCommitted());  //becomes uncommitted after modification
		
		//check if the board is committed...it shouldn't be...
		
		b.undo();  //undo and then make sure all board params are as they were before
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
		assertEquals(true, b.getCommitted());  //should return to committed state
		
		//try adding a piece, committing, and then undoing.  The check board params before and after undo - they should not change
		//because the undo should be useless. 
		result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		b.commit();
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
		b.undo(); 
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
		
	}
	
	// What happens if we try to place a piece where it shouldn't be(overlap or out of bounds) or when we complete a row?
	@Test
	public void testSample3() {
		b.commit();
		int result = b.place(sRotated, 0, 1);
		assertEquals(Board.PLACE_BAD, result);
		b.undo();
		result = b.place(sRotated, b.getWidth()-1, 0 );		
		assertEquals(Board.PLACE_OUT_BOUNDS, result);
		b.undo();
		//check params are as they were originally
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
	}
	
	// Check clearRows
	@Test
	public void testSample4() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		b.clearRows();
		assertEquals(0, b.getColumnHeight(0));
		assertEquals(3, b.getColumnHeight(1));
		assertEquals(2, b.getColumnHeight(2));
		assertEquals(3, b.getMaxHeight());
		assertEquals(2, b.getRowWidth(0));
		assertEquals(2, b.getRowWidth(1));
		assertEquals(1, b.getRowWidth(2));
	}
	
	// Check dropHeight
	@Test
	public void testSample5() {
		assertEquals(2,b.dropHeight(sRotated, 0));
		assertEquals(1,b.dropHeight(sRotated, 1));
		assertEquals(2,b.dropHeight(s, 0));
		assertEquals(2,b.dropHeight(pyr2, 0));
		assertEquals(1,b.dropHeight(pyr2, 1));
	}
	
	// Check multiple pieces stacked
		@Test
		public void testSample6() {
			b.commit();
			int result = b.place(pyr4, 0, 1);
			assertEquals(Board.PLACE_OK, result);
			assertEquals(4, b.getColumnHeight(0));
			assertEquals(3, b.getColumnHeight(1));
			assertEquals(1, b.getColumnHeight(2));
			assertEquals(4, b.getMaxHeight());
			assertEquals(3, b.getRowWidth(0));
			assertEquals(2, b.getRowWidth(1));
			assertEquals(2, b.getRowWidth(2));
			assertEquals(1, b.getRowWidth(3));
			assertEquals(0, b.getRowWidth(4));	
			b.commit();
			int result2 = b.place(pyr3,0,3);
			assertEquals(Board.PLACE_ROW_FILLED, result2);
			assertEquals(5, b.getColumnHeight(0));
			assertEquals(5, b.getColumnHeight(1));
			assertEquals(5, b.getColumnHeight(2));
			assertEquals(5, b.getMaxHeight());
			assertEquals(3, b.getRowWidth(0));
			assertEquals(2, b.getRowWidth(1));
			assertEquals(2, b.getRowWidth(2));
			assertEquals(2, b.getRowWidth(3));
			assertEquals(3, b.getRowWidth(4));
			b.clearRows();
			assertEquals(3, b.getColumnHeight(0));
			assertEquals(3, b.getColumnHeight(1));
			assertEquals(0, b.getColumnHeight(2));
			assertEquals(3, b.getMaxHeight());
			assertEquals(2, b.getRowWidth(0));
			assertEquals(2, b.getRowWidth(1));
			assertEquals(2, b.getRowWidth(2));
			assertEquals(0, b.getRowWidth(3));
			assertEquals(0, b.getRowWidth(4));
			b.undo();
			assertEquals(4, b.getColumnHeight(0));
			assertEquals(3, b.getColumnHeight(1));
			assertEquals(1, b.getColumnHeight(2));
			assertEquals(4, b.getMaxHeight());
			assertEquals(3, b.getRowWidth(0));
			assertEquals(2, b.getRowWidth(1));
			assertEquals(2, b.getRowWidth(2));
			assertEquals(1, b.getRowWidth(3));
			assertEquals(0, b.getRowWidth(4));	
			
		}
	
	// Check piece placement high up in the air
	@Test
	public void testSample7() {
		b.commit();
		int result = b.place(pyr3, 0, 3);
		assertEquals(Board.PLACE_ROW_FILLED, result);
		assertEquals(5, b.getColumnHeight(0));
		assertEquals(5, b.getColumnHeight(1));
		assertEquals(5, b.getColumnHeight(2));
		assertEquals(5, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
		assertEquals(1, b.getRowWidth(3));
		assertEquals(3, b.getRowWidth(4));	
	}
	
	//check simple grid point return function
	@Test
	public void testGrid1() {		
		assertEquals(true, b.getGrid(0, 0));
		assertEquals(true, b.getGrid(1, 0));
		assertEquals(true, b.getGrid(2, 0));
		assertEquals(false, b.getGrid(0, 1));
		assertEquals(true, b.getGrid(1, 1));
		assertEquals(false, b.getGrid(2, 1));
	}
	
	
}
