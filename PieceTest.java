import static org.junit.Assert.*;
import java.util.*;

import org.junit.*;

/*
  Unit test for Piece class -- starter shell.
 */
public class PieceTest {
	// You can create data to be used in the your
	// test cases like this. For each run of a test method,
	// a new PieceTest object is created and setUp() is called
	// automatically by JUnit.
	// For example, the code below sets up some
	// pyramid and s pieces in instance variables
	// that can be used in tests.
	private Piece pyr1, pyr2, pyr3, pyr4;
	private Piece s, sRotated;
	private Piece sqr, sqrRotated;

	@Before
	public void setUp() throws Exception {
		
		pyr1 = new Piece(Piece.PYRAMID_STR);  //Piece.PYRAMIND_STR returns the static variable PYRAMID_STR inside the Piece class...which is the "01 11 00 00" type string representation of the TBody.
		pyr2 = pyr1.computeNextRotation();	  //recall that compute returns a new Piece
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		sqr = new Piece(Piece.SQUARE_STR);
		sqrRotated = sqr.computeNextRotation();
		
		
	}
	
	// Here are some sample tests to get you started
	
	
	@Test
	public void testSampleSize() {
		// Check size of pyr piece
		assertEquals(3, pyr1.getWidth());
		assertEquals(2, pyr1.getHeight());
		
		//Check size of square piece
		assertEquals(2, sqr.getWidth());
		assertEquals(2, sqr.getHeight());
		
		// Now try after rotation
		// Effectively we're testing size and rotation code here
		assertEquals(2, pyr2.getWidth());
		assertEquals(3, pyr2.getHeight());
		
		assertEquals(2, sqrRotated.getWidth());
		assertEquals(2, sqrRotated.getHeight());
		
		
		// Now try with some other piece, made a different way
		Piece l = new Piece(Piece.STICK_STR);
		assertEquals(1, l.getWidth());
		assertEquals(4, l.getHeight());
	}
	
	
	// Test the skirt returned by a few pieces
	@Test
	public void testSampleSkirt() {
		// Note must use assertTrue(Arrays.equals(... as plain .equals does not work
		// right for arrays.
		assertTrue(Arrays.equals(new int[] {0, 0, 0}, pyr1.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0, 1}, pyr3.getSkirt()));
		
		assertTrue(Arrays.equals(new int[] {0, 0, 1}, s.getSkirt()));
		assertTrue(Arrays.equals(new int[] {1, 0}, sRotated.getSkirt()));
	}
	
	// Test the fastRotation of pieces
		@Test
		public void testRotations() {
			Piece[] pieceArray = Piece.getPieces();
			Piece newPyr1 = pieceArray[Piece.PYRAMID];
			Piece newPyr2 = newPyr1.fastRotation();
			Piece newPyr3 = newPyr2.fastRotation();
			Piece newPyr4 = newPyr3.fastRotation();
			Piece anotherPyr1 = newPyr4.fastRotation();
			assertEquals(pyr2,newPyr2);
			assertEquals(pyr3,newPyr3);
			assertEquals(pyr1,newPyr1);
			assertEquals(pyr4,newPyr4);
			assertEquals(pyr1,anotherPyr1);
		}
		
		// Test rotation of pieces that only have 1 or 2 possible rotations
		@Test
		public void testRotationsFew() {
			Piece[] pieceArray = Piece.getPieces();
			Piece newSq1 = pieceArray[Piece.SQUARE];
			Piece newSq2 = newSq1.fastRotation();
			Piece newSq3 = newSq2.fastRotation();
			
			assertEquals(newSq2,newSq1);
			assertEquals(newSq2,newSq3);
			
			Piece newS1_1 = pieceArray[Piece.S1];
			Piece newS1_2 = newS1_1.fastRotation();
			Piece newS1_3 = newS1_2.fastRotation();
			
			assertEquals(newS1_1,s);
			assertEquals(newS1_2,sRotated);
			assertEquals(newS1_3,newS1_1);
		}		

}
