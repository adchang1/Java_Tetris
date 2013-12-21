// Piece.java

import java.util.*;

/**
 An immutable representation of a tetris piece in a particular rotation.
 Each piece is defined by the blocks that make up its body.
 
 Typical client code looks like...
 <pre>
 Piece pyra = new Piece(PYRAMID_STR);		// Create piece from string
 int width = pyra.getWidth();			// 3
 Piece pyra2 = pyramid.computeNextRotation(); // get rotation, slow way
 
 Piece[] pieces = Piece.getPieces();	// the array of root pieces
 Piece stick = pieces[STICK];
 int width = stick.getWidth();		// get its width
 Piece stick2 = stick.fastRotation();	// get the next rotation, fast way
 </pre>
*/
public class Piece {
	// Starter code specs out a few basic things, leaving
	// the algorithms to be done.
	private TPoint[] body;				//it's an array of TPoints which represent the shape of the piece
	private int[] skirt;				//derived from the body, so you can just test this to also verify body
	private int width;
	private int height;
	private Piece next; // "next" rotation.  It's a pointer to a premade piece, for quick next-rotation access

	static private Piece[] pieces;	// singleton static array of first rotations, obtained through the Class Method getPieces()

	/**
	 Defines a new piece given a TPoint[] array of its body.
	 Makes its own copy of the array and the TPoints inside it.
	*/
	public Piece(TPoint[] points) {
		this.body = points;
		skirt = calcSkirt(points);
		width = calcWidth(points);
		height = calcHeight(points);
		
	}
	

	/**
	 * Alternate constructor, takes a String with the x,y body points
	 * all separated by spaces, such as "0 0  1 0  2 0	1 1".
	 * (provided)
	 */
	public Piece(String points) {
		this(parsePoints(points));
	}

	/*
	 * Set the "next" pointer of this piece to another Piece
	 * 
	 */
	private void setNext(Piece next){
		this.next = next;	
	}
	
	/*
	 * Given a TPoint array representing the body of a piece, calculates the skirt, returns the int array representing the skirt
	 */
	private int[] calcSkirt(TPoint[] body) {
		int bodyXwidth = Piece.calcWidth(body);		//first figure out the width of this body; that will be the length of the skirt array.		
		int[] skirtResult = new int[bodyXwidth];	//In Java, int arrays initialized to 0, but...
		for(int n=0;n<bodyXwidth;n++){
			skirtResult[n]=-1;					  //initialize the array to an impossible number so that we can know if it is untouched or not
		}
		for(int i=0;i<body.length;i++){				//parse over all TPoint entries
			int TPointXval = body[i].x;				//first retrieve the x and y value of the body point we are looking at (since they are not in order)
			int TPointYval= body[i].y;		

			if(skirtResult[TPointXval]<0){        //check if this x-val has never been encountered before (skirtResult of that val contains -1 still
				skirtResult[TPointXval]=TPointYval;	//if this is the first time encountering this x-val,set the skirt value equal to the TPoint's y-value
			}
			else if(skirtResult[TPointXval]>TPointYval){								//if you've encountered it before, you'll need to do a y-comparison to see which is the lower y
				skirtResult[TPointXval]=TPointYval;								//in the case that you encounter a lower Y-value in the TPoint currently being examined....then that becomes the new skirt value
			}
		}
		return skirtResult;
	}
	
	
	/*
	 * Given a TPoint array representing the body of a piece, returns the width
	 */
	private static int calcWidth(TPoint[] body) {
		int bodyXlength = body.length;
		int minX=bodyXlength;			//set to unattainable values to guarantee they will be updated
		int maxX=-1;
		for(int i=0;i<bodyXlength;i++){			//parse thru body list
			if(body[i].x<minX){					//search for the lowest valued X
				minX=body[i].x;					//update if you found a lower one
			}
			if(body[i].x>maxX){					//search for the lowest valued X
				maxX=body[i].x;					//update if you found a lower one
			}		
		}
		return (maxX-minX+1);
	}
	
	/*
	 * Given a TPoint array representing the body of a piece, returns the height
	 */
	private int calcHeight(TPoint[] body) {
		int bodyXlength = body.length;
		int minY=body[0].y;				//set to first value for starters
		int maxY=body[0].y;					
		for(int i=1;i<bodyXlength;i++){			//parse thru body list
			if(body[i].y<minY){					//search for the lowest valued X
				minY=body[i].y;					//update if you found a lower one
			}
			if(body[i].y>maxY){					//search for the lowest valued X
				maxY=body[i].y;					//update if you found a lower one
			}		
		}
		return (maxY-minY+1);
	}
	
	
	
	
	/**
	 Returns the width of the piece measured in blocks.
	*/
	public int getWidth() {
		return width;
	}

	/**
	 Returns the height of the piece measured in blocks.
	*/
	public int getHeight() {
		return height;
	}

	/**
	 Returns a pointer to the piece's body. The caller
	 should not modify this array.
	*/
	public TPoint[] getBody() {
		return body;
	}

	/**
	 Prints out the piece's body. 
	*/
	public void printBody() {
		int bodylistLength = body.length;
		for(int i=0; i<bodylistLength; i++){
			System.out.println("Body at index "+i+ " is " + body[i]);
		}
	}
	
	
	
	
	/**
	 Returns a pointer to the piece's skirt. For each x value
	 across the piece, the skirt gives the lowest y value in the body.
	 This is useful for computing where the piece will land.
	 The caller should not modify this array.
	*/
	public int[] getSkirt() {

		return skirt;
	}

	
	/**
	 Returns a new piece that is 90 degrees counter-clockwise
	 rotated from the receiver.
	 */
	public Piece computeNextRotation() {
		TPoint[] testBody = this.getBody();
		int height = this.getHeight();
		TPoint[] newBody = new TPoint[this.getBody().length];
		for(int i=0;i<this.getBody().length;i++){					//parse through all Tpoints of original body array, create new rotated TPoints from these, store them in new TPoint array
			newBody[i] = new TPoint(-testBody[i].y+(height-1),testBody[i].x);  //CC rotation algorithm: newpoint = (-y,x) of old point (rotation maxtrix transformation for theta = 90deg).  Then shift all x-values by height-1 of original body. 
		}		
		Piece newPiece = new Piece(newBody);		//now construct a Piece out of the new TPoint Array
		return newPiece;
	}

	/**
	 Returns a pre-computed piece that is 90 degrees counter-clockwise
	 rotated from the receiver.	 Fast because the piece is pre-computed.
	 This only works on pieces set up by makeFastRotations(), and otherwise
	 just returns null.
	*/	
	public Piece fastRotation() {

		if(Piece.pieces != null){				//the piece associated with the method call NEEDS to be from the pieces[] array, or else it can't do a fast rotation
				return next;		
		}
		else{
			System.out.println("This piece was not created from the pieces array, and thus cannot use fastRotation");
		}
		return null;
		
	}
	


	/**
	 Returns true if two pieces are the same --
	 their bodies contain the same points.
	 Interestingly, this is not the same as having exactly the
	 same body arrays, since the points may not be
	 in the same order in the bodies. Used internally to detect
	 if two rotations are effectively the same.
	*/
	public boolean equals(Object obj) {
		// standard equals() technique 1
		if (obj == this) return true;
		
		// standard equals() technique 2
		// (null will be false)
		if (!(obj instanceof Piece)) return false;
		
		Piece other = (Piece)obj;
		int testlength=other.getBody().length;
		if(this.body.length != testlength) return false;			//make sure body lists are same length
		List<TPoint> testPieceList = new ArrayList<TPoint>(testlength);
		List<TPoint> thisPieceList = new ArrayList<TPoint>(testlength);
		for(int i=0;i<testlength;i++){         //add each element of the body arrays to corresponding List
			testPieceList.add(other.getBody()[i]);
			thisPieceList.add(this.getBody()[i]);
		}	        
		if(thisPieceList.containsAll(testPieceList)) return true;  //check if all items contained in one list exists in the other
		else return false;	
	}


	// String constants for the standard 7 tetris pieces
	public static final String STICK_STR	= "0 0	0 1	 0 2  0 3";
	public static final String L1_STR		= "0 0	0 1	 0 2  1 0";
	public static final String L2_STR		= "0 0	1 0 1 1	 1 2";
	public static final String S1_STR		= "0 0	1 0	 1 1  2 1";
	public static final String S2_STR		= "0 1	1 1  1 0  2 0";
	public static final String SQUARE_STR	= "0 0  0 1  1 0  1 1";
	public static final String PYRAMID_STR	= "0 0  1 0  1 1  2 0";
	
	// Indexes for the standard 7 pieces in the pieces array
	public static final int STICK = 0;
	public static final int L1	  = 1;
	public static final int L2	  = 2;
	public static final int S1	  = 3;
	public static final int S2	  = 4;
	public static final int SQUARE	= 5;
	public static final int PYRAMID = 6;
	
	/**
	 Returns an array containing the first rotation of
	 each of the 7 standard tetris pieces in the order
	 STICK, L1, L2, S1, S2, SQUARE, PYRAMID.
	 The next (counterclockwise) rotation can be obtained
	 from each piece with the {@link #fastRotation()} message.
	 In this way, the client can iterate through all the rotations
	 until eventually getting back to the first rotation.
	 (provided code)
	*/
	public static Piece[] getPieces() {
		// lazy evaluation -- create static array if needed
		if (Piece.pieces==null) {
			// use makeFastRotations() to compute all the rotations for each piece
			Piece.pieces = new Piece[] {
				makeFastRotations(new Piece(STICK_STR)),
				makeFastRotations(new Piece(L1_STR)),
				makeFastRotations(new Piece(L2_STR)),
				makeFastRotations(new Piece(S1_STR)),
				makeFastRotations(new Piece(S2_STR)),
				makeFastRotations(new Piece(SQUARE_STR)),
				makeFastRotations(new Piece(PYRAMID_STR)),
			};
		}
		
		
		return Piece.pieces;
	}
	


	/**
	 Given the "first" root rotation of a piece, computes all
	 the other rotations and links them all together
	 in a circular list. The list loops back to the root as soon
	 as possible. Returns the root piece. fastRotation() relies on the
	 pointer structure setup here.
	*/
	/*
	 Implementation: uses computeNextRotation()
	 and Piece.equals() to detect when the rotations have gotten us back
	 to the first piece.
	*/
	private static Piece makeFastRotations(Piece root) {
		Piece nextRot = root.computeNextRotation();		//sets up the first rotation
		if(nextRot.equals(root)){						//special case for SQUARE where the next rotation is itself...
			root.setNext(root);
			return root;
		}
		else{									//at this point, the next rotation is definitely different from the root
			root.setNext(nextRot);
			Piece currentPiece = root;			//create a Piece representing the current Piece being worked on	
			while(!nextRot.equals(root)){		//as long as the next rotation isn't the same as the original root (in which case we should just set next to root), set next to the computed rotation, calculate the rotation for that next one, rinse and repeat. 				 			
				currentPiece.setNext(nextRot);			//set the next ptr for the current Piece
				currentPiece = nextRot;					//the old next becomes the current
				nextRot = currentPiece.computeNextRotation();   //update nextRot to be the next computed rotation					
			}
			currentPiece.setNext(root);							//last rotation should go back to root
			return root;    //should return the original root
		}
		
	}
	
	

	/**
	 Given a string of x,y pairs ("0 0	0 1 0 2 1 0"), parses
	 the points into a TPoint[] array.
	 (Provided code)
	*/
	private static TPoint[] parsePoints(String string) {
		List<TPoint> points = new ArrayList<TPoint>();
		StringTokenizer tok = new StringTokenizer(string);
		try {
			while(tok.hasMoreTokens()) {
				int x = Integer.parseInt(tok.nextToken());
				int y = Integer.parseInt(tok.nextToken());
				
				points.add(new TPoint(x, y));
			}
		}
		catch (NumberFormatException e) {
			throw new RuntimeException("Could not parse x,y string:" + string);
		}
		
		// Make an array out of the collection
		TPoint[] array = points.toArray(new TPoint[0]);
		return array;
	}

	


}
